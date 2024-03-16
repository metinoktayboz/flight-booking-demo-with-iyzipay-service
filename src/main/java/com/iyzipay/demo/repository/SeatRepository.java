package com.iyzipay.demo.repository;

import com.iyzipay.demo.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
