package com.iyzipay.demo.converter.entityToDto;

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
public class FlightEntityToDto implements Converter<Flight, FlightDTO>{
    private Logger logger = LoggerFactory.getLogger(FlightEntityToDto.class);
    private SeatEntityToDto seatEntityToDto;

    public FlightEntityToDto(@Lazy SeatEntityToDto seatEntityToDto) {
        this.seatEntityToDto = seatEntityToDto;
    }

    @Override
    public FlightDTO convert(Flight flight) {
        try {
            List<Seat> seats = flight.getSeats();
            List<SeatDTO> seatDTOs = seats != null
                    ? seats.stream()
                    .map(seatEntityToDto::convert)
                    .collect(Collectors.toList())
                    : new ArrayList<>();

            return new FlightDTO(flight.getId(), flight.getName(), flight.getDescription(), flight.getPrice(), flight.getTotalAvailableSeats(), seatDTOs);

        }catch (RuntimeException e){
            logger.error("FlightEntityToDto converter is down!");
            throw new ConverterNotExecutedException("FlightEntityToDto converter is down!");
        }
    }
}
