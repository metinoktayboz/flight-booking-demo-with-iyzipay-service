package com.iyzipay.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzipay.demo.entity.Payment;
import com.iyzipay.demo.exception.SeatNotAvailableException;
import com.iyzipay.demo.model.SelectSeatRequest;
import com.iyzipay.demo.repository.PaymentRepository;
import com.iyzipay.demo.service.SeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Component
public class KafkaListeners {
    private Logger logger = LoggerFactory.getLogger(KafkaListeners.class);

    ObjectMapper mapper = new ObjectMapper();
    private boolean exceptionOccurred = false;
    private String exceptionMessage;
    private SeatService seatService;
    private PaymentRepository paymentRepository;

    public KafkaListeners(SeatService seatService, PaymentRepository paymentRepository) {
        this.seatService = seatService;
        this.paymentRepository = paymentRepository;
    }

    @RetryableTopic(
            attempts = "1"
    )
    @KafkaListener(
            topics = "seat-topic",
            groupId = "seatId"
    )
    void listener(String seatRequestString) throws Exception {
        exceptionOccurred = false;
        SelectSeatRequest seatRequest = mapper.readValue(seatRequestString, SelectSeatRequest.class);
        try {
            seatService.selectSeat(seatRequest.getSeat_id(),seatRequest.getBuyer_id());

        }catch (SeatNotAvailableException e){
            exceptionOccurred = true;
            exceptionMessage = e.getMessage();
        }
    }

    public boolean isExceptionOccurred() {
        return exceptionOccurred;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }
}
