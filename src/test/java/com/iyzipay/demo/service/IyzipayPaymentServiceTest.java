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

    @Test
    public void purchaseSeat_ConcurrentRequests() throws InterruptedException {
        ObjectMapper mapper = new ObjectMapper();

        FlightDTO flight1 = new FlightDTO();
        flight1.setName("Test Flight");
        flight1.setDescription("Test");
        flight1.setPrice(new BigDecimal("100"));

        flight1 = flightService.save(flight1);


        SeatDTO seat = new SeatDTO();
        seat.setSeatNumber(1);
        seat.setFlight_id(flight1.getId());
        seatService.save(seat);
        flightService.findByIdDTO(flight1.getId());


        // Create and start the first thread
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    kafkaTemplate.send("seat-topic",mapper.writeValueAsString(new SelectSeatRequest(1L,"buyer1")));
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
                    kafkaTemplate.send("seat-topic",mapper.writeValueAsString(new SelectSeatRequest(1L,"buyer2")));
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
        seatService.selectSeat(1L,"moboz7");
        seatService.selectSeat(2L,"moboz7");
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
        List<Long> s = new ArrayList<>();
        s.add(1L);
        s.add(2L);
        paymentRequest.setSeat_ids(s);


        paymentService.makePayment(paymentRequest);
    }

    @Test(expected = SeatNotAvailableException.class)
    public void makePayment_SeatNotAvailable_ThrowsSeatNotAvailableException(){
        seatService.selectSeat(1L,"moboz7");
        seatService.selectSeat(2L,"moboz7");
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
        List<Long> s = new ArrayList<>();
        s.add(1L);
        s.add(2L);
        paymentRequest.setSeat_ids(s);


        paymentService.makePayment(paymentRequest);
    }

    @Test(expected = PaymentNotAcceptedException.class)
    public void makePayment_PaymentFailure_ThrowsPaymentNotAcceptedException(){
        seatService.selectSeat(1L,"moboz7");
        seatService.selectSeat(2L,"moboz7");
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
        List<Long> s = new ArrayList<>();
        s.add(1L);
        s.add(2L);
        paymentRequest.setSeat_ids(s);


        paymentService.makePayment(paymentRequest);
    }

}
