package com.iyzipay.demo.dto;

import java.math.BigDecimal;
import java.util.List;

//Flight Data Transfer Object
public class FlightDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int totalAvailableSeats;
    private List<SeatDTO> seats;

    public FlightDTO(Long id, String name, String description, BigDecimal price, int totalAvailableSeats, List<SeatDTO> seats) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.totalAvailableSeats = totalAvailableSeats;
        this.seats = seats;
    }

    public FlightDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getTotalAvailableSeats() {
        return totalAvailableSeats;
    }

    public void setTotalAvailableSeats(int totalAvailableSeats) {
        this.totalAvailableSeats = totalAvailableSeats;
    }

    public List<SeatDTO> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatDTO> seats) {
        this.seats = seats;
    }

    @Override
    public String toString() {
        return "FlightDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", totalAvailableSeats=" + totalAvailableSeats +
                ", seats=" + seats +
                '}';
    }
}
