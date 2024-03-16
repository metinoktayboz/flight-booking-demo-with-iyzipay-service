package com.iyzipay.demo.service;

import com.iyzipay.demo.converter.dtoToEntity.FlightDtoToEntity;
import com.iyzipay.demo.converter.entityToDto.FlightEntityToDto;
import com.iyzipay.demo.dto.FlightDTO;
import com.iyzipay.demo.entity.Flight;
import com.iyzipay.demo.entity.Seat;
import com.iyzipay.demo.exception.ResourceNotFoundException;
import com.iyzipay.demo.repository.FlightRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlightService {

    private FlightRepository flightRepository;
    private FlightDtoToEntity flightDtoToEntity;
    private FlightEntityToDto flightEntityToDto;

    //@Lazy notation is used to handle recursions
    public FlightService(@Lazy FlightRepository flightRepository,
                         @Lazy FlightDtoToEntity flightDtoToEntity,
                         @Lazy FlightEntityToDto flightEntityToDto) {
        this.flightRepository = flightRepository;
        this.flightDtoToEntity = flightDtoToEntity;
        this.flightEntityToDto = flightEntityToDto;
    }

    public FlightDTO save(FlightDTO flightDTO) throws RuntimeException {
        Flight flight = flightDtoToEntity.convert(flightDTO);
        flight = flightRepository.save(flight);
        return flightEntityToDto.convert(flight);
    }

    @Transactional
    public Flight findById(Long id){
        return flightRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException(String.valueOf(id)));
    }

    @Transactional
    public FlightDTO findByIdDTO(Long id){
        return flightRepository.findById(id)
                .map(flightEntityToDto::convert)
                .orElseThrow(()->new ResourceNotFoundException(String.valueOf(id)));
    }

    public void delete(Long id){
        flightRepository.deleteById(id);
    }

    public List<FlightDTO> findAll() {
        List<Flight> flights = flightRepository.findAll();
        return flights.stream()
                .map(flightEntityToDto::convert)
                .collect(Collectors.toList());
    }

    public void updateAvailableSeat(Long flight_id){
        Flight flight = findById(flight_id);
        int available = (int) flight.getSeats().stream()
                .filter(Seat::isAvailability)
                .count();

        flight.setTotalAvailableSeats(available);
        flightRepository.save(flight);
    }

}
