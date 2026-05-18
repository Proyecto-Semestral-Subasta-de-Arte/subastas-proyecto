package cl.sda1085.subastas.config;

import cl.sda1085.subastas.model.Subasta;
import cl.sda1085.subastas.repository.SubastaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SubastaRepository subastaRepository;

    @Override
    public void run (String... args) {

        // Verificar si existen subastas para no duplicarlas
        if (subastaRepository.count() > 0) {
            log.info("Base de datos de subastas ya contiene datos. Omitiendo inicialización");
            return;
        }
        log.info("Generando 10 subastas de prueba con fechas fijas distribuidas en Mayo 2026...");

        //Subastas ABIERTAS (iniciaron antes del 17 de mayo, terminan la última semana de mayo)

        Subasta s1 = new Subasta(null, 1L, 1L, new BigDecimal("150000.00"),
                LocalDateTime.of(2026, 5, 12, 9, 30),
                LocalDateTime.of(2026, 5, 24, 15, 0), "ABIERTA", null);

        Subasta s2 = new Subasta(null, 2L, 2L, new BigDecimal("300000.00"),
                LocalDateTime.of(2026, 5, 14, 11, 15),
                LocalDateTime.of(2026, 5, 26, 18, 45), "ABIERTA", null);

        Subasta s3 = new Subasta(null, 3L, 3L, new BigDecimal("500000.00"),
                LocalDateTime.of(2026, 5, 15, 14, 0),
                LocalDateTime.of(2026, 5, 28, 21, 30), "ABIERTA", null);

        Subasta s4 = new Subasta(null, 6L, 1L, new BigDecimal("220000.00"),
                LocalDateTime.of(2026, 5, 16, 8, 0),
                LocalDateTime.of(2026, 5, 30, 23, 58), "ABIERTA", null);


        //Subastas PROGRAMADAS (inician en el futuro, finalizan a fin de mayo)

        Subasta s5 = new Subasta(null, 7L, 2L, new BigDecimal("400000.00"),
                LocalDateTime.of(2026, 5, 20, 10, 0),
                LocalDateTime.of(2026, 5, 25, 12, 15), "PROGRAMADA", null);

        Subasta s6 = new Subasta(null, 8L, 3L, new BigDecimal("350000.00"),
                LocalDateTime.of(2026, 5, 22, 16, 30),
                LocalDateTime.of(2026, 5, 29, 20, 0), "PROGRAMADA", null);

        Subasta s7 = new Subasta(null, 10L, 1L, new BigDecimal("180000.00"),
                LocalDateTime.of(2026, 5, 19, 9, 0),
                LocalDateTime.of(2026, 5, 31, 14, 10), "PROGRAMADA", null);


        //Subastas CERRADAS

        Subasta s8 = new Subasta(null, 12L, 2L, new BigDecimal("270000.00"),
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 8, 18, 0), "CERRADA", 4L);

        Subasta s9 = new Subasta(null, 15L, 3L, new BigDecimal("600000.00"),
                LocalDateTime.of(2026, 5, 5, 12, 0),
                LocalDateTime.of(2026, 5, 12, 22, 30), "CERRADA", 5L);

        Subasta s10 = new Subasta(null, 18L, 2L, new BigDecimal("210000.00"),
                LocalDateTime.of(2026, 5, 7, 15, 0),
                LocalDateTime.of(2026, 5, 15, 20, 0), "CERRADA", 6L);

        subastaRepository.saveAll(List.of(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10));
        log.info("¡Base de datos poblada con éxito! Fechas asimétricas listas para pruebas de filtros.");
    }
}
