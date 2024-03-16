package com.iyzipay.demo.converter.entityToDto;

import com.iyzipay.demo.dto.SeatDTO;
import com.iyzipay.demo.entity.Seat;
import com.iyzipay.demo.exception.ConverterNotExecutedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

//Manuel mapping to convert entities and dtos
@Component
public class SeatEntityToDto implements Converter<Seat, SeatDTO> {
    private Logger logger = LoggerFactory.getLogger(SeatEntityToDto.class);
    @Override
    public SeatDTO convert(Seat seat) {
        try {
            return new SeatDTO(seat.getId(), seat.isAvailability(), seat.getSeatNumber(), seat.getPassengerId(), seat.getFlight().getId());
        }catch (RuntimeException e){
            logger.error("SeatEntityToDto converter is down!");
            throw new ConverterNotExecutedException("SeatEntityToDto converter is down!");
        }
    }
}
