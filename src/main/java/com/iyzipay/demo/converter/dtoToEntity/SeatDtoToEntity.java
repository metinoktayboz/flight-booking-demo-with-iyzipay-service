package com.iyzipay.demo.converter.dtoToEntity;

import com.iyzipay.demo.dto.SeatDTO;
import com.iyzipay.demo.entity.Seat;
import com.iyzipay.demo.exception.ConverterNotExecutedException;
import com.iyzipay.demo.service.FlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

//Manuel mapping to convert entities and dtos
@Component
public class SeatDtoToEntity implements Converter<SeatDTO, Seat> {
    private Logger logger = LoggerFactory.getLogger(SeatDtoToEntity.class);

    private FlightService flightService;

    public SeatDtoToEntity(@Lazy FlightService flightService) {
        this.flightService = flightService;
    }

    @Override
    public Seat convert(SeatDTO seatDTO) {
        try {
            Seat seat = new Seat();

            seat.setId(seatDTO.getId());
            seat.setSeatNumber(seatDTO.getSeatNumber());
            seat.setAvailability(seatDTO.isAvailability());
            seat.setPassengerId(seatDTO.getPassengerId());
            seat.setFlight(flightService.findById(seatDTO.getFlight_id()));

            return seat;
        }catch (RuntimeException e){
            logger.error("SeatDtoToEntity converter is down!");
            throw new ConverterNotExecutedException("SeatDtoToEntity converter is down!");
        }
    }
}
