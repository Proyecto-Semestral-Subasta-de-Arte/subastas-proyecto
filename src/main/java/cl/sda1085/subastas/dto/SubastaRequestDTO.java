package cl.sda1085.subastas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modelo de petición para registrar o crear una nueva subasta.")

public class SubastaRequestDTO {

    //DTO de entrada
    //No es necesario el ID, se genera automáticamente

    @NotNull(message = "El ID del producto es obligatorio.")
    @Schema(description = "Identificador único del producto proveniente del microservicio 'productos'.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idProducto;

    @NotNull(message = "El ID del vendedor es obligatorio.")
    @Schema(description = "Identificador único del usuario vendedor.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idVendedor;

    @NotNull(message = "El precio base es obligatorio.")
    @DecimalMin(value = "0.01", message = "El precio base debe ser mayor a cero.")
    @Schema(description = "Precio inicial con el que se abre la subasta.", example = "150000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal precioBase;

    @NotNull(message = "La fecha de inicio es obligatoria.")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o una fecha futura.")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de término es obligatoria.")
    @Future(message = "La fecha de término debe ser una fecha futura.")
    @Schema(description = "Fecha y hora límite para recibir pujas/ofertas.", example = "2026-06-22T18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime fechaTermino;

    @NotBlank(message = "El estado no debe estar vacío.")
    @Schema(description = "Estado inicial del flujo de la subasta.", example = "PROGRAMADA", allowableValues = {"PROGRAMADA", "ABIERTA", "CERRADA", "CANCELADA"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String estado;
}
