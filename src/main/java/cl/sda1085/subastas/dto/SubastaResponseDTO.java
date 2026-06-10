package cl.sda1085.subastas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modelo de respuesta con los datos consolidados de la subasta")


public class SubastaResponseDTO {

    //DTO de salida (respuesta)
    //No existen las anotaciones de validación

    @Schema(description = "Identificador único autogenerado en la base de datos de subastas", example = "1")
    private Long id;  //ID generado en la base de datos

    @Schema(description = "ID del producto asociado", example = "1")
    private Long idProducto;

    @Schema(description = "ID del vendedor", example = "10")
    private Long idVendedor;

    @Schema(description = "Precio base establecido", example = "150000.00")
    private BigDecimal precioBase;

    @Schema(description = "Fecha de inicio de la subasta", example = "2026-06-15T10:00:00")
    private LocalDateTime fechaInicio;

    @Schema(description = "Fecha de término de la subasta", example = "2026-06-22T18:00:00")
    private LocalDateTime fechaTermino;

    @Schema(description = "Estado actual de la subasta", example = "ABIERTA")
    private String estado;

    @Schema(description = "ID del usuario comprador que ganó la subasta (nulo si sigue activa o quedó desierta)", example = "5", nullable = true)
    private Long idGanador;  //Se llena al finalizar la subasta
}
