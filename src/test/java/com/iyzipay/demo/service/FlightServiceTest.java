package com.iyzipay.demo.service;

import com.iyzipay.demo.converter.dtoToEntity.FlightDtoToEntity;
import com.iyzipay.demo.converter.entityToDto.FlightEntityToDto;
import com.iyzipay.demo.dto.FlightDTO;
import com.iyzipay.demo.entity.Flight;
import com.iyzipay.demo.entity.Seat;
import com.iyzipay.demo.exception.ResourceNotFoundException;
import com.iyzipay.demo.repository.FlightRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@EnableAutoConfiguration
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class FlightServiceTest { //FlightService tests with mock data. Each test should run itself
    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightDtoToEntity flightDtoToEntity;

    @Mock
    private FlightEntityToDto flightEntityToDto;

    @InjectMocks
    private FlightService flightService;

    @Test
    public void testSave() {
        // Prepare test data
        FlightDTO flightDTO = new FlightDTO();
        Flight flight = new Flight();

        // Set up mock behavior
        Mockito.when(flightDtoToEntity.convert(flightDTO)).thenReturn(flight);
        Mockito.when(flightRepository.save(flight)).thenReturn(flight);
        Mockito.when(flightEntityToDto.convert(flight)).thenReturn(flightDTO);

        // Call the method to test
        FlightDTO result = flightService.save(flightDTO);

        // Verify the result
        Assert.assertEquals(flightDTO, result);
        Mockito.verify(flightDtoToEntity).convert(flightDTO);
        Mockito.verify(flightRepository).save(flight);
        Mockito.verify(flightEntityToDto).convert(flight);
    }

    @Test
    public void testFindById() {
        // Prepare test data
        Long id = 1L;
        Flight flight = new Flight();

        // Set up mock behavior
        Mockito.when(flightRepository.findById(id)).thenReturn(Optional.of(flight));

        // Call the method to test
        Flight result = flightService.findById(id);

        // Verify the result
        Assert.assertEquals(flight, result);
        Mockito.verify(flightRepository).findById(id);
    }

    @Test
    public void testFindByIdDTO() {
        // Prepare test data
        Long id = 1L;
        Flight flight = new Flight();
        FlightDTO flightDTO = new FlightDTO();

        // Set up mock behavior
        Mockito.when(flightRepository.findById(id)).thenReturn(Optional.of(flight));
        Mockito.when(flightEntityToDto.convert(flight)).thenReturn(flightDTO);

        // Call the method to test
        FlightDTO result = flightService.findByIdDTO(id);

        // Verify the result
        Assert.assertEquals(flightDTO, result);
        Mockito.verify(flightRepository).findById(id);
        Mockito.verify(flightEntityToDto).convert(flight);
    }

    @Test
    public void testDelete_Success() {
        // Prepare test data
        Long id = 1L;

        // Call the method to test
        flightService.delete(id);

        // Verify the behavior
        Mockito.verify(flightRepository).deleteById(id);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDelete_Failure() {
        // Prepare test data
        Long id = 1L;

        // Set up mock behavior
        Mockito.doThrow(ResourceNotFoundException.class).when(flightRepository).deleteById(id);

        // Call the method to test
        flightService.delete(id);

        // Verify the behavior
        Mockito.verify(flightRepository).deleteById(id);
    }

    @Test
    public void testFindAll() {
        // Prepare test data
        Flight flight1 = new Flight();
        Flight flight2 = new Flight();
        List<Flight> flights = Arrays.asList(flight1, flight2);
        FlightDTO flightDTO1 = new FlightDTO();
        FlightDTO flightDTO2 = new FlightDTO();
        List<FlightDTO> expectedDTOs = Arrays.asList(flightDTO1, flightDTO2);

        // Set up mock behavior
        Mockito.when(flightRepository.findAll()).thenReturn(flights);
        Mockito.when(flightEntityToDto.convert(flight1)).thenReturn(flightDTO1);
        Mockito.when(flightEntityToDto.convert(flight2)).thenReturn(flightDTO2);

        // Call the method to test
        List<FlightDTO> result = flightService.findAll();

        // Verify the result
        Assert.assertEquals(expectedDTOs, result);
        Mockito.verify(flightRepository).findAll();
        Mockito.verify(flightEntityToDto).convert(flight1);
        Mockito.verify(flightEntityToDto).convert(flight2);
    }

    @Test
    public void testUpdateAvailableSeat() {
        // Prepare test data
        Long flightId = 1L;
        Flight flight = new Flight();
        List<Seat> seats = new ArrayList<>();
        seats.add(new Seat(true));
        seats.add(new Seat(false));
        flight.setSeats(seats);

        // Set up mock behavior
        Mockito.when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));

        // Call the method to test
        flightService.updateAvailableSeat(flightId);

        // Verify the result
        Assert.assertEquals(1, flight.getTotalAvailableSeats());
        Mockito.verify(flightRepository).findById(flightId);
        Mockito.verify(flightRepository).save(flight);
    }
}
