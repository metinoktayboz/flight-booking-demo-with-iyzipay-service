package com.iyzipay.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzipay.demo.dto.FlightDTO;
import com.iyzipay.demo.dto.SeatDTO;
import com.iyzipay.demo.exception.PaymentNotAcceptedException;
import com.iyzipay.demo.exception.SeatNotAvailableException;
import com.iyzipay.demo.model.PaymentRequest;
import com.iyzipay.demo.model.SelectSeatRequest;
import com.iyzipay.demo.model.sample.SampleAdress;
import com.iyzipay.demo.model.sample.SampleBuyer;
import com.iyzipay.demo.model.sample.SamplePaymentCard;
import com.iyzipay.model.Buyer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.AopTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
public class IyzipayPaymentServiceTest { //paymentService and concurrent book request tests. Each test should run itself

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private IyzipayPaymentService paymentService;

    //spy on the real service, so the concurrency test can see the outcome of every selection made by the listener
    @SpyBean
    private SeatService seatService;

    @Autowired
    private FlightService flightService;

    //creates a flight with the given number of seats and returns the seat ids, so each test uses its own data
    private List<Long> createSeats(int count) {
        FlightDTO flight = new FlightDTO();
        flight.setName("Test Flight");
        flight.setDescription("Test");
        flight.setPrice(new BigDecimal("100"));
        flight = flightService.save(flight);

        List<Long> seatIds = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            SeatDTO seat = new SeatDTO();
            seat.setSeatNumber(i);
            seat.setFlight_id(flight.getId());
            seatIds.add(seatService.save(seat).getId());
        }
        return seatIds;
    }

    private void assertSeats(List<Long> seatIds, boolean availability, String passengerId) {
        for (Long seatId : seatIds) {
            SeatDTO seat = seatService.findById(seatId);
            Assert.assertEquals(availability, seat.isAvailability());
            Assert.assertEquals(passengerId, seat.getPassengerId());
        }
    }

    //the listener's consumer uses the default auto-offset-reset (latest), so messages sent before it has its partition would be skipped
    private void waitForSeatTopicAssignment() throws Exception {
        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            String[] topics = container.getContainerProperties().getTopics();
            if (topics != null && Arrays.asList(topics).contains("seat-topic")) {
                ContainerTestUtils.waitForAssignment(container, 1);
            }
        }
    }

    @Test
    public void purchaseSeat_ConcurrentRequests() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Long seatId = createSeats(1).get(0);

        //record what happened to each buyer's request: null if the seat was booked, otherwise the exception
        Map<String, RuntimeException> outcomes = Collections.synchronizedMap(new HashMap<>());
        CountDownLatch processed = new CountDownLatch(2);
        SeatService seatServiceSpy = AopTestUtils.getUltimateTargetObject(seatService);
        Mockito.doAnswer(invocation -> {
            String buyerId = invocation.getArgument(1);
            try {
                Object result = invocation.callRealMethod();
                outcomes.put(buyerId, null);
                return result;
            } catch (RuntimeException e) {
                outcomes.put(buyerId, e);
                throw e;
            } finally {
                processed.countDown();
            }
        }).when(seatServiceSpy).selectSeat(Mockito.eq(seatId), Mockito.anyString());

        waitForSeatTopicAssignment();

        //both requests are published before either is processed, as two simultaneous HTTP requests would be
        kafkaTemplate.send("seat-topic", mapper.writeValueAsString(new SelectSeatRequest(seatId, "buyer1"))).get(10, TimeUnit.SECONDS);
        kafkaTemplate.send("seat-topic", mapper.writeValueAsString(new SelectSeatRequest(seatId, "buyer2"))).get(10, TimeUnit.SECONDS);

        Assert.assertTrue("both selection requests should be processed", processed.await(30, TimeUnit.SECONDS));

        SeatDTO seat = seatService.findById(seatId);
        Assert.assertFalse(seat.isAvailability());
        Assert.assertTrue(Arrays.asList("buyer1", "buyer2").contains(seat.getPassengerId()));
        String otherBuyer = seat.getPassengerId().equals("buyer1") ? "buyer2" : "buyer1";

        Assert.assertEquals(2, outcomes.size());
        Assert.assertNull(outcomes.get(seat.getPassengerId()));
        Assert.assertNotNull(outcomes.get(otherBuyer));
        Assert.assertEquals(SeatNotAvailableException.class, outcomes.get(otherBuyer).getClass());
        Assert.assertEquals("Seat: " + seatId + " is booked by someone else!", outcomes.get(otherBuyer).getMessage());
    }


    @Test
    public void makePayment_ValidPaymentRequest_SuccessfulPayment(){
        List<Long> s = createSeats(2);
        s.forEach(seatId -> seatService.selectSeat(seatId,"moboz7"));
        PaymentRequest paymentRequest = new PaymentRequest();
        SampleAdress sampleAdress = new SampleAdress();
        SampleBuyer sampleBuyer = new SampleBuyer();
        Buyer buyer = sampleBuyer.getSampleBuyer();
        buyer.setId("moboz7");
        SamplePaymentCard samplePaymentCard = new SamplePaymentCard();

        paymentRequest.setPaymentCard(samplePaymentCard.getSamplePaymentCard());
        paymentRequest.setAddress(sampleAdress.getSampleAddress());
        paymentRequest.setBuyer(buyer);
        paymentRequest.setBaskedId("B123");
        paymentRequest.setSeat_ids(s);


        paymentService.makePayment(paymentRequest);

        //a successful payment keeps the seats for the buyer
        assertSeats(s, false, "moboz7");
    }

    @Test
    public void makePayment_SeatNotAvailable_ThrowsSeatNotAvailableException(){
        List<Long> s = createSeats(2);
        s.forEach(seatId -> seatService.selectSeat(seatId,"moboz7"));
        PaymentRequest paymentRequest = new PaymentRequest();
        SampleAdress sampleAdress = new SampleAdress();
        SampleBuyer sampleBuyer = new SampleBuyer();
        Buyer buyer = sampleBuyer.getSampleBuyer();
        buyer.setId("differentId");
        SamplePaymentCard samplePaymentCard = new SamplePaymentCard();

        paymentRequest.setPaymentCard(samplePaymentCard.getSamplePaymentCard());
        paymentRequest.setAddress(sampleAdress.getSampleAddress());
        paymentRequest.setBuyer(buyer);
        paymentRequest.setBaskedId("B123");
        paymentRequest.setSeat_ids(s);


        try {
            paymentService.makePayment(paymentRequest);
            Assert.fail("Expected SeatNotAvailableException");
        } catch (SeatNotAvailableException e) {
            //the seats stay with the buyer who selected them
            assertSeats(s, false, "moboz7");
        }
    }

    @Test
    public void makePayment_PaymentFailure_ThrowsPaymentNotAcceptedException(){
        List<Long> s = createSeats(2);
        s.forEach(seatId -> seatService.selectSeat(seatId,"moboz7"));
        PaymentRequest paymentRequest = new PaymentRequest();
        SampleAdress sampleAdress = new SampleAdress();
        SampleBuyer sampleBuyer = new SampleBuyer();
        Buyer buyer = sampleBuyer.getSampleBuyer();
        buyer.setId("differentId");
        SamplePaymentCard samplePaymentCard = new SamplePaymentCard();

        paymentRequest.setPaymentCard(samplePaymentCard.getSamplePaymentCard());
        paymentRequest.setAddress(sampleAdress.getSampleAddress());
        buyer.setId("moboz7");
        buyer.setCity(""); //missing parameter
        paymentRequest.setBuyer(buyer);
        paymentRequest.setBaskedId("B123");
        paymentRequest.setSeat_ids(s);


        try {
            paymentService.makePayment(paymentRequest);
            Assert.fail("Expected PaymentNotAcceptedException");
        } catch (PaymentNotAcceptedException e) {
            //a failed payment releases the seats again
            assertSeats(s, true, null);
        }
    }

}
