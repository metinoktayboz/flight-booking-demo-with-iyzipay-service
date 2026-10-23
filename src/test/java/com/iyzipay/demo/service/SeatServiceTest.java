package com.iyzipay.demo.service;

import com.iyzipay.demo.converter.dtoToEntity.SeatDtoToEntity;
import com.iyzipay.demo.converter.entityToDto.SeatEntityToDto;
import com.iyzipay.demo.dto.SeatDTO;
import com.iyzipay.demo.entity.Flight;
import com.iyzipay.demo.entity.Seat;
import com.iyzipay.demo.exception.ResourceNotFoundException;
import com.iyzipay.demo.exception.SeatNotAvailableException;
import com.iyzipay.demo.repository.SeatRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@EnableAutoConfiguration
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class SeatServiceTest { //SeatService tests with mock data. Each test should run itself
    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatEntityToDto seatEntityToDto;

    @Mock
    private SeatDtoToEntity seatDtoToEntity;

    @Mock
    private FlightService flightService;

    @InjectMocks
    private SeatService seatService;

    @Test
    public void testSave() {
        // Prepare test data
        SeatDTO seatDTO = new SeatDTO();
        Seat seat = new Seat();
        Flight flight = new Flight();
        flight.setId(1L);
        seat.setFlight(flight);

        // Set up mock behavior
        Mockito.when(seatDtoToEntity.convert(seatDTO)).thenReturn(seat);
        Mockito.when(seatRepository.save(seat)).thenReturn(seat);
        Mockito.doNothing().when(flightService).updateAvailableSeat(flight.getId());
        Mockito.when(seatEntityToDto.convert(seat)).thenReturn(seatDTO);

        // Call the method to test
        SeatDTO result = seatService.save(seatDTO);

        // Verify the result
        Assert.assertEquals(seatDTO, result);
        Mockito.verify(seatDtoToEntity).convert(seatDTO);
        Mockito.verify(seatRepository).save(seat);
        Mockito.verify(flightService).updateAvailableSeat(flight.getId());
        Mockito.verify(seatEntityToDto).convert(seat);
    }

    @Test
    public void testFindById() {
        // Prepare test data
        Long id = 1L;
        Seat seat = new Seat();
        SeatDTO seatDTO = new SeatDTO();

        // Set up mock behavior
        Mockito.when(seatRepository.findById(id)).thenReturn(Optional.of(seat));
        Mockito.when(seatEntityToDto.convert(seat)).thenReturn(seatDTO);

        // Call the method to test
        SeatDTO result = seatService.findById(id);

        // Verify the result
        Assert.assertEquals(seatDTO, result);
        Mockito.verify(seatRepository).findById(id);
        Mockito.verify(seatEntityToDto).convert(seat);
    }

    @Test
    public void testDelete() {
        // Prepare test data
        Long id = 1L;

        // Call the method to test
        seatService.delete(id);

        // Verify the behavior
        Mockito.verify(seatRepository).deleteById(id);
    }

    @Test
    public void testSelectSeat_AvailableSeat() throws Exception {
        // Prepare test data
        Long selectedSeatId = 1L;
        String buyerId = "buyer123";
        Flight flight = new Flight();
        flight.setId(1L);
        Seat seat = new Seat();
        seat.setId(selectedSeatId);
        seat.setAvailability(true);
        seat.setFlight(flight);

        // Set up mock behavior: the converters copy the fields like the real ones, the repository returns what it saves
        Mockito.when(seatRepository.findById(selectedSeatId)).thenReturn(Optional.of(seat));
        Mockito.when(seatEntityToDto.convert(Mockito.any(Seat.class))).thenAnswer(invocation -> {
            Seat s = invocation.getArgument(0);
            return new SeatDTO(s.getId(), s.isAvailability(), s.getSeatNumber(), s.getPassengerId(), s.getFlight().getId());
        });
        Mockito.when(seatDtoToEntity.convert(Mockito.any(SeatDTO.class))).thenAnswer(invocation -> {
            SeatDTO dto = invocation.getArgument(0);
            Seat s = new Seat(dto.isAvailability(), dto.getSeatNumber(), dto.getPassengerId(), flight);
            s.setId(dto.getId());
            return s;
        });
        Mockito.when(seatRepository.save(Mockito.any(Seat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Call the method to test
        SeatDTO result = seatService.selectSeat(selectedSeatId, buyerId);

        // Verify the result
        Assert.assertFalse(result.isAvailability());
        Assert.assertEquals(buyerId, result.getPassengerId());

        ArgumentCaptor<Seat> savedSeat = ArgumentCaptor.forClass(Seat.class);
        Mockito.verify(seatRepository).findById(selectedSeatId);
        Mockito.verify(seatRepository).save(savedSeat.capture());
        Assert.assertEquals(selectedSeatId, savedSeat.getValue().getId());
        Assert.assertFalse(savedSeat.getValue().isAvailability());
        Assert.assertEquals(buyerId, savedSeat.getValue().getPassengerId());
        Mockito.verify(flightService).updateAvailableSeat(flight.getId());
    }

    @Test(expected = SeatNotAvailableException.class)
    public void testSelectSeat_UnavailableSeat() throws Exception {
        // Prepare test data
        Long selectedSeatId = 1L;
        String buyerId = "buyer123";
        Flight flight = new Flight();
        flight.setId(1L);
        Seat seat = new Seat();
        seat.setId(selectedSeatId);
        seat.setAvailability(false);
        seat.setFlight(flight);

        SeatDTO selectedSeat = new SeatDTO();
        selectedSeat.setId(selectedSeatId);
        selectedSeat.setAvailability(false);

        // Set up mock behavior
        Mockito.when(seatRepository.findById(selectedSeatId)).thenReturn(Optional.of(seat));
        Mockito.when(seatEntityToDto.convert(seat)).thenReturn(selectedSeat);

        // Call the method to test
        seatService.selectSeat(selectedSeatId, buyerId);
    }

    @Test
    public void testUnSelectSeats() {
        // Prepare test data
        Long seatId1 = 1L;
        Long seatId2 = 2L;
        List<Long> selectedSeatIds = Arrays.asList(seatId1, seatId2);
        Flight flight = new Flight();
        flight.setId(1L);

        SeatDTO seat1 = new SeatDTO();
        seat1.setId(seatId1);
        seat1.setAvailability(false);
        seat1.setPassengerId("passenger1");
        seat1.setFlight_id(flight.getId());
        Seat seatEntity1 = new Seat();
        seatEntity1.setFlight(flight);

        SeatDTO seat2 = new SeatDTO();
        seat2.setId(seatId2);
        seat2.setAvailability(false);
        seat2.setPassengerId("passenger2");
        seat2.setFlight_id(flight.getId());
        Seat seatEntity2 = new Seat();
        seatEntity2.setFlight(flight);

        // Set up mock behavior
        Mockito.when(seatRepository.findById(seatId1)).thenReturn(Optional.of(seatEntity1));
        Mockito.when(seatRepository.findById(seatId2)).thenReturn(Optional.of(seatEntity2));
        Mockito.when(seatEntityToDto.convert(seatEntity1)).thenReturn(seat1);
        Mockito.when(seatEntityToDto.convert(seatEntity2)).thenReturn(seat2);
        Mockito.when(seatDtoToEntity.convert(seat1)).thenReturn(seatEntity1);
        Mockito.when(seatDtoToEntity.convert(seat2)).thenReturn(seatEntity2);
        Mockito.when(seatRepository.save(seatEntity1)).thenReturn(seatEntity1);
        Mockito.when(seatRepository.save(seatEntity2)).thenReturn(seatEntity2);

        // Call the method to test
        seatService.unSelectSeats(selectedSeatIds);

        // Verify the behavior
        Mockito.verify(seatRepository).findById(seatId1);
        Mockito.verify(seatRepository).findById(seatId2);
        Mockito.verify(seatRepository).save(seatEntity1);
        Mockito.verify(seatRepository).save(seatEntity2);

        Assert.assertTrue(seat1.isAvailability());
        Assert.assertNull(seat1.getPassengerId());
        Assert.assertTrue(seat2.isAvailability());
        Assert.assertNull(seat2.getPassengerId());
    }

    @Test
    public void testIsFlightValid_ExistingFlight() {
        // Mock the behavior of flightService.findById to return a Flight object
        Long flightId = 1L;
        Mockito.when(flightService.findById(flightId)).thenReturn(new Flight());

        // Call the method to test
        seatService.isFlightValid(flightId);

        // Verify that flightService.findById was called with the correct argument
        Mockito.verify(flightService).findById(flightId);
        // No exception should be thrown
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testIsFlightValid_NonExistingFlight() {
        // Mock the behavior of flightService.findById to throw a ResourceNotFoundException
        Long flightId = 1L;
        Mockito.when(flightService.findById(flightId)).thenThrow(new ResourceNotFoundException("Flight: " + flightId + "does not exist."));

        // Call the method to test
        seatService.isFlightValid(flightId);

        // Verify that flightService.findById was called with the correct argument
        Mockito.verify(flightService).findById(flightId);
        // ResourceNotFoundException should be thrown
    }
}
