package cl.sda1085.subastas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subastas")

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_producto", nullable = false, updatable = true)
    private Long idProducto;  //Relacionado con microservicio 'productos'

    @Column(name = "id_vendedor", nullable = false)
    private Long idVendedor;  //Relacionado con microservicio 'usuarios' (vendedor)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaTermino;

    @Column(nullable = false, updatable = true)
    private String estado;  //Programada, abierta, cerrada o cancelada

    @Column(name = "id_ganador", nullable = true)
    private Long idGanador;  //Al final, guardará quién ganó para que el microservicio 'pagos' lo use
}
