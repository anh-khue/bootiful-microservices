package com.khuetla.sample.reservationclient;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableFeignClients
@EnableCircuitBreaker
@EnableZuulProxy // Technically unnecessary
@SpringBootApplication
public class ReservationClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationClientApplication.class, args);
    }
}


@FeignClient("reservation-service")
interface ReservationReader {

    @GetMapping("/reservations")
    Collection<Reservation> readReservations();
}


@RestController
@RequestMapping("/reservations")
class ReservationApiAdapterRestController {

    private final ReservationReader reservationReader;

    ReservationApiAdapterRestController(ReservationReader reservationReader) {
        this.reservationReader = reservationReader;
    }

    Collection<String> fallback() {
        return new ArrayList<>();
    }

    @HystrixCommand(fallbackMethod = "fallback")
    @GetMapping("/names")
    Collection<String> names() {
        return this.reservationReader
                .readReservations()
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