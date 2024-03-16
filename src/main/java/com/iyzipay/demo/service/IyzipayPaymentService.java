package com.iyzipay.demo.service;

import com.iyzipay.demo.dto.FlightDTO;
import com.iyzipay.demo.dto.SeatDTO;
import com.iyzipay.demo.exception.PaymentNotAcceptedException;
import com.iyzipay.demo.exception.SeatNotAvailableException;
import com.iyzipay.demo.model.PaymentRequest;
import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreatePaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/* Payment Service for Flight Booking System */
@Service
public class IyzipayPaymentService {
    private Logger logger = LoggerFactory.getLogger(IyzipayPaymentService.class);

    private final String apiKey = "sandbox-bu2xXaGH29vvvYCFnvcoSpbM5qfvcaDk";

    private final String secretKey = "sandbox-YoDHHR922RLhU0wBa7RM1S9esIthuV3i";

    private final String baseUrl = "https://sandbox-api.iyzipay.com";

    private FlightService flightService;
    private SeatService seatService;

    public IyzipayPaymentService(FlightService flightService, SeatService seatService) {
        this.flightService = flightService;
        this.seatService = seatService;
    }

    public void makePayment(PaymentRequest paymentRequest) {
            List<BasketItem> basketItems = new ArrayList<>();

            BigDecimal totalPrice = new BigDecimal("0");
            BigDecimal percentage = new BigDecimal("0.20");
            BigDecimal paidPrice;

            CreatePaymentRequest request = new CreatePaymentRequest();

            for (Long selectedSeat_id : paymentRequest.getSeat_ids()) {
                SeatDTO seat = seatService.findById(selectedSeat_id);  /* Getting seat details from database to check availability */
                if (!seat.getPassengerId().equals(paymentRequest.getBuyer().getId())) {    /* in case of someone took the seat in booking time */
                    throw new SeatNotAvailableException("Seat no:"+ selectedSeat_id +" is booked by someone else !");
                }

                FlightDTO flight = flightService.findByIdDTO(seat.getFlight_id()); /* Calculating total price */
                totalPrice = totalPrice.add(flight.getPrice());

                BasketItem basketItem = new BasketItem();
                basketItem.setId(String.valueOf(seat.getId()));
                basketItem.setName(flight.getName());
                basketItem.setCategory1(String.valueOf(seat.getSeatNumber()));
                basketItem.setItemType(BasketItemType.VIRTUAL.name());
                basketItem.setPrice(flight.getPrice());
                basketItems.add(basketItem);
            }

        try {
            Options options = new Options();
            options.setApiKey(apiKey);
            options.setSecretKey(secretKey);
            options.setBaseUrl(baseUrl);
            request.setBasketItems(basketItems);

            request.setLocale(Locale.TR.getValue());
            request.setConversationId("123456789");
            request.setCurrency(Currency.TRY.name());
            request.setInstallment(1);
            request.setBasketId(paymentRequest.getBaskedId());
            request.setPaymentChannel(PaymentChannel.WEB.name());
            request.setPaymentGroup(PaymentGroup.PRODUCT.name());

            request.setPrice(totalPrice);
            paidPrice = totalPrice.add(totalPrice.multiply(percentage));
            request.setPaidPrice(paidPrice);

            request.setBuyer(paymentRequest.getBuyer());
            request.setPaymentCard(paymentRequest.getPaymentCard());

            request.setBillingAddress(paymentRequest.getAddress());

            Payment payment = Payment.create(request, options);
            if (payment.getStatus().equals("failure")){
                seatService.unSelectSeats(paymentRequest.getSeat_ids()); /* if payment cannot be performed, selected seats will be empty again */
                logger.error(payment.getErrorMessage());
                throw new PaymentNotAcceptedException(payment.getErrorMessage());
            }
            logger.info("Payment Successfull.");
        }catch (Exception e){
            seatService.unSelectSeats(paymentRequest.getSeat_ids()); /* if payment cannot be performed, selected seats will be empty again */
            logger.error(e.getMessage());
            throw new PaymentNotAcceptedException(e.getMessage());
        }
    }


}
