package com.iyzipay.demo.model;

import com.iyzipay.model.Address;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.PaymentCard;

import java.util.List;

public class PaymentRequest {
    private List<Long> seat_ids;
    private Buyer buyer;
    private Address address;
    private PaymentCard paymentCard;
    private String baskedId;

    public List<Long> getSeat_ids() {
        return seat_ids;
    }

    public void setSeat_ids(List<Long> seat_ids) {
        this.seat_ids = seat_ids;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public PaymentCard getPaymentCard() {
        return paymentCard;
    }

    public void setPaymentCard(PaymentCard paymentCard) {
        this.paymentCard = paymentCard;
    }

    public String getBaskedId() {
        return baskedId;
    }

    public void setBaskedId(String baskedId) {
        this.baskedId = baskedId;
    }
}
