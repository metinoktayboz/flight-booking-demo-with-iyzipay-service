package com.iyzipay.demo.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//Flight Database Object
@Entity
@Table(
        name = "flights"
)
public class Flight {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int totalAvailableSeats;

    //related column with Seat Entity
    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Seat> seats = new ArrayList<>();

    public Flight(String name, String description, BigDecimal price, int totalAvailableSeats, List<Seat> seats) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.totalAvailableSeats = totalAvailableSeats;
        this.seats = seats;
    }

    public Flight() {
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

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

}
