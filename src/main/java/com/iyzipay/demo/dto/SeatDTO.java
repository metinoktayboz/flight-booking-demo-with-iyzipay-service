package com.iyzipay.demo.dto;

//Seat Data Transfer Object
public class SeatDTO {
    private Long id;
    private boolean availability=true;
    private int seatNumber;
    private String passengerId;
    private Long flight_id;

    public SeatDTO(Long id, boolean availability, int seatNumber, String passengerId, Long flight_id) {
        this.id = id;
        this.availability = availability;
        this.seatNumber = seatNumber;
        this.passengerId = passengerId;
        this.flight_id = flight_id;
    }

    public SeatDTO() {
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

    public Long getFlight_id() {
        return flight_id;
    }

    public void setFlight_id(Long flight_id) {
        this.flight_id = flight_id;
    }

    @Override
    public String toString() {
        return "SeatDTO{" +
                "id=" + id +
                ", availability=" + availability +
                ", seatNumber=" + seatNumber +
                ", passengerId='" + passengerId + '\'' +
                ", flight_id=" + flight_id +
                '}';
    }
}
