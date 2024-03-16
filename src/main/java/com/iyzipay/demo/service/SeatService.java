package com.iyzipay.demo.service;

import com.iyzipay.demo.converter.dtoToEntity.SeatDtoToEntity;
import com.iyzipay.demo.converter.entityToDto.SeatEntityToDto;
import com.iyzipay.demo.dto.SeatDTO;
import com.iyzipay.demo.entity.Seat;
import com.iyzipay.demo.exception.ResourceNotFoundException;
import com.iyzipay.demo.exception.SeatNotAvailableException;
import com.iyzipay.demo.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class SeatService {
    private Logger logger = LoggerFactory.getLogger(SeatService.class);

    private SeatRepository seatRepository;
    private SeatEntityToDto seatEntityToDto;
    private SeatDtoToEntity seatDtoToEntity;
    private FlightService flightService;

    //@Lazy notation is used to handle recursions
    public SeatService(@Lazy SeatRepository seatRepository,
                       @Lazy SeatEntityToDto seatEntityToDto,
                       @Lazy SeatDtoToEntity seatDtoToEntity,
                       @Lazy FlightService flightService) {
        this.seatRepository = seatRepository;
        this.seatEntityToDto = seatEntityToDto;
        this.seatDtoToEntity = seatDtoToEntity;
        this.flightService = flightService;
    }

    public SeatDTO save(SeatDTO seatDTO) throws RuntimeException{
        isFlightValid(seatDTO.getFlight_id());
        Seat seat = seatDtoToEntity.convert(seatDTO);
        seat = seatRepository.save(seat);
        flightService.updateAvailableSeat(seat.getFlight().getId());
        return seatEntityToDto.convert(seat);
    }

    @Transactional
    public SeatDTO findById(Long id){
        return seatRepository.findById(id)
                .map(seatEntityToDto::convert)
                .orElseThrow(()-> new ResourceNotFoundException(String.valueOf(id))
                );
    }

    public void delete(Long id){
        seatRepository.deleteById(id);
    }

    /* Select seat before payment to anyone can select */
    public SeatDTO selectSeat(Long selectedSeat_id, String buyer_id) throws SeatNotAvailableException {
            SeatDTO selectedSeat = findById(selectedSeat_id); /* Getting seat details from database again to check availability */
            if (!selectedSeat.isAvailability()) {             /* in case of someone took the seat in booking time */
                System.out.println("Seat:" + selectedSeat_id + " is not available for " + buyer_id + "!");
                logger.error("Seat: "+ selectedSeat_id +" is booked by someone else!");
                throw new SeatNotAvailableException("Seat: "+ selectedSeat_id +" is booked by someone else!");
            }

            selectedSeat.setAvailability(false);
            selectedSeat.setPassengerId(buyer_id); /* Closing the seat for buyer */
            System.out.println("Seat:" + selectedSeat_id + " is booked for " + buyer_id + "!");
            logger.info("Seat: "+ selectedSeat_id +" is booked by " + buyer_id + "!");
            return save(selectedSeat);
    }

    public void unSelectSeats(List<Long> selectedSeat_ids){
        selectedSeat_ids.forEach(seat_id -> {
            SeatDTO seat = findById(seat_id);
            seat.setAvailability(true);
            seat.setPassengerId(null);
            save(seat);
        });
        System.out.println("Seats are unselected now!");
    }

    public void isFlightValid(Long flight_id){
        try {
            flightService.findById(flight_id);
        }catch (ResourceNotFoundException e){
            logger.error("Seat can not be added because flight: "+flight_id+" does not exist!");
            throw new ResourceNotFoundException("Seat can not be added because flight: "+flight_id+" does not exist!");
        }
    }
}
