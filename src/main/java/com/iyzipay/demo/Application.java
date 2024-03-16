package com.iyzipay.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//    @Bean
//    CommandLineRunner commandLineRunner(SeatService seatService, FlightService flightService, KafkaTemplate<String, String> kafkaTemplate, IyzipayPaymentService paymentService){
//        return args -> {
//            ObjectMapper mapper = new ObjectMapper();
//            FlightDTO flight1 = new FlightDTO();
//            flight1.setName("f1");
//            flight1.setDescription("mersin to adana");
//            flight1.setPrice(new BigDecimal("100"));
//
//            flight1 = flightService.save(flight1);
//
//            List<SeatDTO> seats = new ArrayList<>();
//            for(int i=0;i<10;i++){
//                SeatDTO seat = new SeatDTO();
//                seat.setSeatNumber(i);
//                seat.setFlight_id(flight1.getId());
//                seat = seatService.save(seat);
//                flight1 = flightService.findByIdDTO(flight1.getId());
//                seats.add(seat);
//            }
//
////                kafkaTemplate.send("seat-topic",new SelectSeatRequest(1L,"by17"));
//            // Create and start the first thread
////            Thread thread1 = new Thread(new Runnable() {
////                @Override
////                public void run() {
////
////                    try {
////                        kafkaTemplate.send("seat-topic",mapper.writeValueAsString(new SelectSeatRequest(1L,"by17")));
////                    } catch (JsonProcessingException e) {
////                        e.printStackTrace();
////                    }
////                }
////            });
////            thread1.start();
////
////            // Create and start the second thread
////            Thread thread2 = new Thread(new Runnable() {
////                @Override
////                public void run() {
////                    try {
////                        kafkaTemplate.send("seat-topic",mapper.writeValueAsString(new SelectSeatRequest(1L,"moboz7")));
////                    } catch (JsonProcessingException e) {
////                        e.printStackTrace();
////                    }
////                }
////            });
////            thread2.start();
//
////            kafkaTemplate.send("seat-topic",mapper.writeValueAsString(new SelectSeatRequest(1L,"moboz7")));
////            kafkaTemplate.send("seat-topic", mapper.writeValueAsString(new SelectSeatRequest(2L,"moboz7")));
//
////            seatService.selectSeat(1L,"moboz7");
////            seatService.selectSeat(2L,"moboz7");
////
////            PaymentRequest paymentRequest = new PaymentRequest();
////            SampleAdress sampleAdress = new SampleAdress();
////            SampleBuyer sampleBuyer = new SampleBuyer();
////            Buyer buyer = sampleBuyer.getSampleBuyer();
////            buyer.setId("moboz7");
////            buyer.setCity("");
////            SamplePaymentCard samplePaymentCard = new SamplePaymentCard();
////
////            paymentRequest.setPaymentCard(samplePaymentCard.getSamplePaymentCard());
////            paymentRequest.setAddress(sampleAdress.getSampleAddress());
////            paymentRequest.setBuyer(buyer);
////            paymentRequest.setBaskedId("B123");
////            List<Long> s = new ArrayList<>();
////            s.add(1L);
////            s.add(2L);
////            paymentRequest.setSeat_ids(s);
////
////            try {
////                paymentService.makePayment(paymentRequest);
////            }
////            catch (PaymentNotAcceptedException e){
////                throw new PaymentNotAcceptedException(e.getMessage());
////            }
//        };
//
//    }
}