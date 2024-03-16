package com.iyzipay.demo.model;

public class SelectSeatRequest {
    private Long seat_id;
    private String buyer_id;

    public SelectSeatRequest() {
    }

    public SelectSeatRequest(Long seat_id, String buyer_id) {
        this.seat_id = seat_id;
        this.buyer_id = buyer_id;
    }

    public Long getSeat_id() {
        return seat_id;
    }

    public void setSeat_id(Long seat_id) {
        this.seat_id = seat_id;
    }

    public String getBuyer_id() {
        return buyer_id;
    }

    public void setBuyer_id(String buyer_id) {
        this.buyer_id = buyer_id;
    }

    @Override
    public String toString() {
        return "SelectSeatRequest{" +
                "seat_id=" + seat_id +
                ", buyer_id='" + buyer_id + '\'' +
                '}';
    }
}
