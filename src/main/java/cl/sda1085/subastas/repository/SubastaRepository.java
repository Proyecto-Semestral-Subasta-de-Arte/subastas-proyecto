package cl.sda1085.subastas.repository;

import cl.sda1085.subastas.model.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubastaRepository extends JpaRepository<Subasta, Long> {

    //------------------------------
    //CRUD personalizado
    //------------------------------

    //Busca subastas activas por su estado
    List<Subasta> findByEstado(String estado);

    //Busca subastas de un producto específico
    List<Subasta> findByIdProducto(Long idProducto);

    //Busca subastas que finalizan antes de una fecha/hora específica
    List<Subasta> findByFechaTerminoBefore(LocalDateTime fecha);

    //Busca la subasta activa de un producto específico
    Optional<Subasta> findByIdProductoAndEstado(Long idProducto, String estado);

    //Verifica si un vendedor ya tiene una subasta activa de un producto específico
    boolean existsByIdVendedorAndEstado(Long idVendedor, String estado);

    //Encuentra la subasta que terminará más pronto (la más urgente)
    Optional<Subasta> findTopByEstadoOrderByFechaTerminoAsc(String estado);

    //Verifica si un producto ya está registrado en alguna subasta, sin importar el estado
    boolean existsByIdProducto(Long idProducto);
}
