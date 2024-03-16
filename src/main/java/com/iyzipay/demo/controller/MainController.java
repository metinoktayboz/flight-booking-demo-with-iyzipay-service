package com.iyzipay.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzipay.demo.KafkaListeners;
import com.iyzipay.demo.exception.PaymentNotAcceptedException;
import com.iyzipay.demo.exception.SeatNotAvailableException;
import com.iyzipay.demo.model.PaymentRequest;
import com.iyzipay.demo.model.SelectSeatRequest;
import com.iyzipay.demo.service.IyzipayPaymentService;
import com.iyzipay.demo.service.SeatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/main")
public class MainController {

    private IyzipayPaymentService iyzipayPaymentService;
    private SeatService seatService;
    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaListeners kafkaListeners;



    private ObjectMapper mapper = new ObjectMapper();


    public MainController(IyzipayPaymentService iyzipayPaymentService, SeatService seatService, KafkaTemplate<String, String> kafkaTemplate, KafkaListeners kafkaListeners) {
        this.iyzipayPaymentService = iyzipayPaymentService;
        this.seatService = seatService;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaListeners = kafkaListeners;

    }

    @PostMapping(value = "/ss", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> selectSeat(@RequestBody SelectSeatRequest selectSeat) throws InterruptedException, JsonProcessingException, ExecutionException {
        CountDownLatch latch = new CountDownLatch(1);

        kafkaTemplate.send("seat-topic", mapper.writeValueAsString(selectSeat));

        if (kafkaListeners.isExceptionOccurred()) {
            throw new SeatNotAvailableException(kafkaListeners.getExceptionMessage());
        }
        return ResponseEntity.ok().body("Seat is booked by " + selectSeat.getBuyer_id() + " successfully!");
    }

    @PostMapping(value = "/mp", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> makePayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            iyzipayPaymentService.makePayment(paymentRequest);
            return ResponseEntity.ok().body("Payment Successful!");
        }catch (PaymentNotAcceptedException e){
            throw new PaymentNotAcceptedException(e.getMessage());
        }
    }
}
