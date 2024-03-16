package com.iyzipay.demo.controller;

import com.iyzipay.demo.dto.FlightDTO;
import com.iyzipay.demo.exception.ResourceNotFoundException;
import com.iyzipay.demo.model.FlightRequest;
import com.iyzipay.demo.service.FlightService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Flight CRUD services
@RestController
@RequestMapping("/api/v1/flight")
public class FlightController {

    private FlightService flightService;

    //flight service autowired
    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    //Adding service
    @PostMapping(value = "/af", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addFlight(@RequestBody FlightRequest flight) {
        FlightDTO flightDTO = new FlightDTO();
        flightDTO.setName(flight.getName());
        flightDTO.setDescription(flight.getDescription());
        flightDTO.setPrice(flight.getPrice());
        try {
            return ResponseEntity.ok().body(flightService.save(flightDTO));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Updating service
    @PostMapping(value = "/uf/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateFlight(@RequestBody FlightRequest flight, @PathVariable Long id) {
        FlightDTO flightDTO = new FlightDTO();
        flightDTO.setName(flight.getName());
        flightDTO.setDescription(flight.getDescription());
        flightDTO.setPrice(flight.getPrice());
        flightDTO.setId(id);
        try {
            return ResponseEntity.ok().body(flightService.save(flightDTO));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Removing service
    @DeleteMapping(value = "/df/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteFlight(@PathVariable Long id) {
        try {
            flightService.delete(id);
        }catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Flight can not be deleted because flight: " + id + " does not exist!");
        }
        return ResponseEntity.ok().body("Flight deleted successfully!");
    }

    //Getting service
    @GetMapping(value = "/gf/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getFlight(@PathVariable Long id) {
        FlightDTO flightDTO;
        try {
            flightDTO = flightService.findByIdDTO(id);
        }catch (ResourceNotFoundException e){
            throw new ResourceNotFoundException("Flight " + e.getMessage() + " does not exist!");
        }
        return ResponseEntity.ok().body(flightDTO);
    }

    //Flight/Seat listing service which returns flight name, description, available seats and price.
    @GetMapping(value = "/gaf", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllFlights(){
        List<FlightDTO> flights = flightService.findAll();
        if (flights.size()<=0){
            throw new ResourceNotFoundException("No flight found!");
        }
        return ResponseEntity.ok().body(flightService.findAll());

    }

}
