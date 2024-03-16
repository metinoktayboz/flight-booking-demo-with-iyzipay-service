package com.iyzipay.demo.converter.dtoToEntity;

import com.iyzipay.demo.dto.FlightDTO;
import com.iyzipay.demo.dto.SeatDTO;
import com.iyzipay.demo.entity.Flight;
import com.iyzipay.demo.entity.Seat;
import com.iyzipay.demo.exception.ConverterNotExecutedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//Manuel mapping to convert entities and dtos
@Component
public class FlightDtoToEntity implements Converter<FlightDTO, Flight> {
    private Logger logger = LoggerFactory.getLogger(FlightDtoToEntity.class);
    private SeatDtoToEntity seatDtoToEntity;

    public FlightDtoToEntity(@Lazy SeatDtoToEntity seatDtoToEntity) {
        this.seatDtoToEntity = seatDtoToEntity;
    }

    @Override
    public Flight convert(FlightDTO flightDTO) {
        try {
            List<SeatDTO> seatDTOs = flightDTO.getSeats();
            List<Seat> seats = seatDTOs != null
                    ? seatDTOs.stream()
                    .map(seatDtoToEntity::convert)
                    .collect(Collectors.toList())
                    : new ArrayList<>();

            Flight flight = new Flight();

            flight.setId(flightDTO.getId());
            flight.setName(flightDTO.getName());
            flight.setDescription(flightDTO.getDescription());
            flight.setPrice(flightDTO.getPrice());
            flight.setTotalAvailableSeats(flightDTO.getTotalAvailableSeats());
            flight.setSeats(seats);

            return flight;
        }catch (RuntimeException e){
            logger.error("FlightDtoToEntity converter is down!");
            throw new ConverterNotExecutedException("FlightDtoToEntity converter is down!");
        }
    }
}
