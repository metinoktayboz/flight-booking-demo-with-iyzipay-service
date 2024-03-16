package com.iyzipay.demo.model.sample;

import com.iyzipay.model.PaymentCard;

public class SamplePaymentCard {
    public SamplePaymentCard() {
    }

    public PaymentCard getSamplePaymentCard(){
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardHolderName("John Doe");
        paymentCard.setCardNumber("5528790000000008");
        paymentCard.setExpireMonth("12");
        paymentCard.setExpireYear("2030");
        paymentCard.setCvc("123");
        paymentCard.setRegisterCard(0);

        return paymentCard;
    }
}
