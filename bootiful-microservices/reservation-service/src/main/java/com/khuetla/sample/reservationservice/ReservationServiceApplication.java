package com.khuetla.sample.reservationservice;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@EnableBinding(ReservationSubscriberChannels.class)
@SpringBootApplication
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}


interface ReservationSubscriberChannels {

    String CREATE_RESERVATION = "createReservation";

    @Input(CREATE_RESERVATION)
    SubscribableChannel createReservation();
}


@MessageEndpoint
class ReservationMessageEndpoint {

    private final ReservationRepository reservationRepository;

    ReservationMessageEndpoint(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @ServiceActivator(inputChannel = ReservationSubscriberChannels.CREATE_RESERVATION)
    public void createReservation(String reservationName) {
        this.reservationRepository.save(new Reservation(reservationName));
    }
}


@RestController
@RefreshScope
class MessageRestController {

    private final String value;

    MessageRestController(@Value("${message}") String value) {
        this.value = value;
    }

    @GetMapping("/message")
    String read() {
        return this.value;
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
        Stream.of("Java", "Servlet", "Spring",
                "Microservices", "Cloud Native", "Serverless",
                "BigData", "IoT", "Data Stream")
                .map(Reservation::new)
                .forEach(reservationRepository::save);

        // Observe messages queue's durability ensured by RabbitMQ
        reservationRepository.findAll()
                .forEach(System.out::println);
    }
}


interface ReservationRepository extends JpaRepository<Reservation, Long> {

}


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
class Reservation {

    @Id
    @GeneratedValue
    private Long id;
    @NonNull
    private String name;
}