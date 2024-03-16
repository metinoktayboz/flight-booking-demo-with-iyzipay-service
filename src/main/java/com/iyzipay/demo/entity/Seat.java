package com.iyzipay.demo.entity;

import javax.persistence.*;

//Seat Database Object
@Entity
@Table(
        name = "seats"
)
public class Seat {
    @Id
    @GeneratedValue
    private Long id;
    private boolean availability=true;
    private int seatNumber;
    private String passengerId;

    //related column with Flight Entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    private Flight flight;

    public Seat(boolean availability, int seatNumber, String passengerId, Flight flight) {
        this.availability = availability;
        this.seatNumber = seatNumber;
        this.passengerId = passengerId;
        this.flight = flight;
    }

    public Seat() {
    }

    public Seat(boolean availability) {
        this.availability = availability;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

}
