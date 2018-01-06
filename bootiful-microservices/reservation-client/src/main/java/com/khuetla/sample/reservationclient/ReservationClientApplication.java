package com.khuetla.sample.reservationclient;

import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Collectors;

@EnableFeignClients
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