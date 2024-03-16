# Iyzipay Payment Service Demo


The demo is a Java11 + Spring Boot project which uses H2 as the database.


## Flight Booking System

Demo contains flight ticket service.
For flight booking system the REST services which are implemented are listed below.



## Developer Notes

* Flight service [FlightService.java](src/main/java/com/iyzipay/demo/service/FlightService.java) 
* Seat service [SeatService.java](src/main/java/com/iyzipay/demo/service/SeatService.java)
* Iyzico payment integration [IyzipayPaymentService.java](src/main/java/com/iyzipay/demo/service/IyzipayPaymentService.java) 
services implemented in service package.

* IT test for "If there are 2 passengers pay at the same time for the same seat, 
first successful should buy the seat and the 2nd one should fail with an appropriate message" case.
[IyzipayPaymentServiceTest.java](src/test/java/com/iyzipay/demo/service/IyzipayPaymentServiceTest.java)

* Apache Kafka and ZooKeeper are used for solution of concurrent booking management.
* ZooKeeper (bin/zookeeper-server-start.sh config/zookeeper.properties) and
Kafka Server (bin/kafka-server-start.sh config/server.properties)
should be started before run the Application

* Flight and Seat add, remove, update and read endpoints in [FlightController.java](src/main/java/com/iyzipay/demo/controller/FlightController.java) and [SeatController.java](src/main/java/com/iyzipay/demo/controller/SeatController.java)
* Select a seat and making payment endpoints in [MainController.java](src/main/java/com/iyzipay/demo/controller/MainController.java)
* GlobalExpection Handler implemented in [GlobalExceptionHandler.java](src/main/java/com/iyzipay/demo/exception/GlobalExceptionHandler.java)


Reference: [https://dev.iyzipay.com/](https://dev.iyzipay.com/)