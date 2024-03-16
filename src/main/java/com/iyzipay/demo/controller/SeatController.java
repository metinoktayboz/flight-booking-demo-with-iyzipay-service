package com.iyzipay.demo.controller;

import com.iyzipay.demo.dto.SeatDTO;
import com.iyzipay.demo.exception.ResourceNotFoundException;
import com.iyzipay.demo.model.SeatRequest;
import com.iyzipay.demo.service.SeatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//Seat CRUD services
@RestController
@RequestMapping("/api/v1/seat")
public class SeatController {

    private SeatService seatService;

    //seat service autowired
    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    //adding service
    @PostMapping(value = "/as", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addSeat(@RequestBody SeatRequest seat) {
        SeatDTO seatDTO = new SeatDTO();
        seatDTO.setFlight_id(seat.getFlight_id());
        seatDTO.setSeatNumber(seat.getSeatNumber());
        try {
            return ResponseEntity.ok().body(seatService.save(seatDTO));
        }catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    //updating service
    @PostMapping(value = "/us/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateSeat(@RequestBody SeatRequest seat, @PathVariable Long id) {
        SeatDTO seatDTO = new SeatDTO();
        seatDTO.setFlight_id(seat.getFlight_id());
        seatDTO.setSeatNumber(seat.getSeatNumber());
        seatDTO.setId(id);
        try {
            return ResponseEntity.ok().body(seatService.save(seatDTO));
        }catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    //removing service
    @DeleteMapping(value = "/ds/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteSeat(@PathVariable Long id) {
        try {
            seatService.delete(id);
        }catch (ResourceNotFoundException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Seat deleted successfully!");
    }

    //getting service
    @GetMapping(value = "/gs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSeat(@PathVariable Long id) {
        try {
            return ResponseEntity.ok().body(seatService.findById(id));
        }catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Seat " + e.getMessage() + " does not exist!");
        }
    }
}
