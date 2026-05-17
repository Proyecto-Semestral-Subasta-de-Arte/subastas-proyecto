package cl.sda1085.subastas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling  //Interruptor que activa el soporte de Spring para tareas programadas.
@SpringBootApplication
public class SubastasApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubastasApplication.class, args);
	}

}
