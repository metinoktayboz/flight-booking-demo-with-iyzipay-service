package com.iyzipay.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzipay.demo.KafkaListeners;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
public class IyzipayPaymentServiceTest { //paymentService and concurrent book request tests. Each test should run itself

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaListeners kafkaListeners;

    @Autowired
    private IyzipayPaymentService paymentService;

    @Autowired
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

    @Test
    public void purchaseSeat_ConcurrentRequests() throws InterruptedException {
        ObjectMapper mapper = new ObjectMapper();

        Long seatId = createSeats(1).get(0);


        // Create and start the first thread
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    kafkaTemplate.send("seat-topic",mapper.writeValueAsString(new SelectSeatRequest(seatId,"buyer1")));
                    Thread.sleep(5000);
                    if (kafkaListeners.isExceptionOccurred()){
                        throw new SeatNotAvailableException(kafkaListeners.getExceptionMessage());
                    }
                } catch (JsonProcessingException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread1.start();

        // Create and start the second thread
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    kafkaTemplate.send("seat-topic",mapper.writeValueAsString(new SelectSeatRequest(seatId,"buyer2")));
                    Thread.sleep(5000);
                    if (kafkaListeners.isExceptionOccurred()){
                        throw new SeatNotAvailableException(kafkaListeners.getExceptionMessage());
                    }
                } catch (JsonProcessingException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread2.start();

        Thread.sleep(5000);
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
    }

    @Test(expected = SeatNotAvailableException.class)
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


        paymentService.makePayment(paymentRequest);
    }

    @Test(expected = PaymentNotAcceptedException.class)
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


        paymentService.makePayment(paymentRequest);
    }

}
