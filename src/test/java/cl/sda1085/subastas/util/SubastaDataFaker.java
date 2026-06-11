package cl.sda1085.subastas.util;

import cl.sda1085.subastas.dto.SubastaRequestDTO;
import cl.sda1085.subastas.dto.SubastaResponseDTO;
import cl.sda1085.subastas.model.Subasta;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

public class SubastaDataFaker {

    //Configurado en español para coherencia regional si fuera necesario
    private static final Faker faker = new Faker(new Locale("es"));

    public static Subasta createFakeEntity() {
        return new Subasta(
                faker.number().randomNumber(5, false),
                faker.number().randomNumber(3, false),
                faker.number().randomNumber(3, false),
                BigDecimal.valueOf(faker.number().randomDouble(2, 10000, 500000)),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                faker.options().option("PROGRAMADA", "ABIERTA"),
                null
        );
    }

    public static SubastaRequestDTO createFakeRequestDTO() {
        return new SubastaRequestDTO(
                faker.number().randomNumber(3, false),
                faker.number().randomNumber(3, false),
                BigDecimal.valueOf(faker.number().randomDouble(2, 10000, 500000)),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5),
                "PROGRAMADA"
        );
    }

    public static SubastaResponseDTO createFakeResponseDTO(Subasta subasta) {
        return new SubastaResponseDTO(
                subasta.getId(),
                subasta.getIdProducto(),
                subasta.getIdVendedor(),
                subasta.getPrecioBase(),
                subasta.getFechaInicio(),
                subasta.getFechaTermino(),
                subasta.getEstado(),
                subasta.getIdGanador()
        );
    }
}
