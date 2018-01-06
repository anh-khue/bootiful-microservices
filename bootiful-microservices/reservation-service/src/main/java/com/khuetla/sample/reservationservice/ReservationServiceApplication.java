package com.khuetla.sample.reservationservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@SpringBootApplication
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}


@RestController
class ReservationApiRestController {

    private final ReservationRepository reservationRepository;

    ReservationApiRestController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/reservations")
    Collection<Reservation> reservations() {
        return this.reservationRepository.findAll();
    }
}


@Component
class DataInitializer implements ApplicationRunner {

    private final ReservationRepository reservationRepository;

    DataInitializer(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        Stream.of("Servlet", "Hibernate", "Spring",
                "Microservices", "Cloud", "Serverless",
                "BigData", "IoT", "Blockchain")
                .forEach(name -> reservationRepository.save(new Reservation(null, name)));
        reservationRepository.findAll().forEach(System.out::println);
    }
}


interface ReservationRepository extends JpaRepository<Reservation, Long> {

}


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
class Reservation {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
}