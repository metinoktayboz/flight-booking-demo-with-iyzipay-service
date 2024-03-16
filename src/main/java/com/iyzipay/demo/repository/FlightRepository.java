package com.iyzipay.demo.repository;

import com.iyzipay.demo.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight, Long> {
}
