package com.khuetla.sample.reservationclient;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableResourceServer
@IntegrationComponentScan
@EnableBinding(ReservationPublisherChannels.class)
@EnableCircuitBreaker
@EnableFeignClients
@EnableZuulProxy // Technically unnecessary
@SpringBootApplication
public class ReservationClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationClientApplication.class, args);
    }
}


interface ReservationPublisherChannels {

    String CREATE_RESERVATION = "createReservation";

    @Output(CREATE_RESERVATION)
    MessageChannel createReservation();
}


@MessagingGateway
interface ReservationMessagingGateway {

    @Gateway(requestChannel = ReservationPublisherChannels.CREATE_RESERVATION)
    void createReservation(String reservationName);
}


@FeignClient("reservation-service")
interface ReservationClient {

    @GetMapping("/reservations")
    Collection<Reservation> getReservations();
}


@RestController
@RequestMapping("/reservations")
class ReservationApiGateway {

    private final ReservationClient reservationClient;

    private final ReservationMessagingGateway reservationMessagingGateway;

    ReservationApiGateway(ReservationClient reservationClient,
                          ReservationMessagingGateway reservationMessagingGateway) {
        this.reservationClient = reservationClient;
        this.reservationMessagingGateway = reservationMessagingGateway;
    }

    @PostMapping
    void createReservation(@RequestBody Reservation reservation) {
        this.reservationMessagingGateway
                .createReservation(reservation.getName());
    }

    Collection<String> fallback() {
        return new ArrayList<>();
    }

    @HystrixCommand(fallbackMethod = "fallback")
    @GetMapping("/names")
    Collection<String> getReservationsNames() {
        return this.reservationClient
                .getReservations()
                .stream()
                .map(Reservation::getName)
                .collect(Collectors.toList());
    }
}


@Data
class Reservation {

    private Long id;
    private String name;
}