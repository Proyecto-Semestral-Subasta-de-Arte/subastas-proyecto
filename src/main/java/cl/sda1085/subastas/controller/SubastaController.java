package cl.sda1085.subastas.controller;

import cl.sda1085.subastas.dto.SubastaRequestDTO;
import cl.sda1085.subastas.dto.SubastaResponseDTO;
import cl.sda1085.subastas.service.SubastaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subastas")
public class SubastaController {

    private final SubastaService subastaService;


    //------------------------------
    //CRUD estándar
    //------------------------------

    @GetMapping
    public ResponseEntity<List<SubastaResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(subastaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubastaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(subastaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<SubastaResponseDTO> crear(
            @Valid @RequestBody SubastaRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subastaService.guardar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubastaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SubastaRequestDTO dto) {

        return ResponseEntity.ok(subastaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        subastaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }


    //------------------------------
    //CRUD personalizado
    //------------------------------

    //Busca subastas activas por su estado
    //Ruta: GET /api/subastas/estado/ABIERTA
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SubastaResponseDTO>> buscarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(subastaService.obtenerPorEstado(estado));
    }

    //Busca subastas de un producto específico
    //Ruta: GET /api/subastas/producto/{idProducto}
    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<SubastaResponseDTO>> buscarPorIdProducto(
            @PathVariable Long idProducto) {

        return ResponseEntity.ok(subastaService.obtenerPorIdProducto(idProducto));
    }

    //Busca subastas que finalizan antes de una fecha/hora específica
    //Ruta: GET /api/subastas/vencimiento?fecha=2026-05-17T21:00:00
    @GetMapping("/vencimiento")
    public ResponseEntity<List<SubastaResponseDTO>> buscarPorVencimiento(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        return ResponseEntity.ok(subastaService.obtenerSubastasPorVencer(fecha));
    }

    //Busca la subasta activa de un producto específico
    //Ruta: GET /api/subastas/buscar/producto/{idProducto}/activa
    @GetMapping("/producto/{idProducto}/activa")
    public ResponseEntity<SubastaResponseDTO> buscarActivaPorProducto(@PathVariable Long idProducto) {
        return ResponseEntity.ok(subastaService.obtenerSubastaActivaProducto(idProducto));
    }

    //Verifica si un vendedor ya tiene una subasta activa
    //Ruta: GET /api/subastas/vendedor/{idVendedor}/activa
    @GetMapping("/vendedor/{idVendedor}/activa")
    public ResponseEntity<Boolean> verificarVendedorActivo(@PathVariable Long idVendedor) {
        return ResponseEntity.ok(subastaService.vendedorTieneSubastaActiva(idVendedor));
    }

    //Encuentra la subasta que terminará más pronto (la más urgente)
    //Ruta: GET /api/subastas/urgente
    @GetMapping("/urgente")
    public ResponseEntity<SubastaResponseDTO> obtenerMasUrgente() {
        return ResponseEntity.ok(subastaService.obtenerSubastaMasUrgente());
    }

    //Verifica si un producto ya está registrado en alguna subasta
    //Ruta: GET /api/subastas/producto/{idProducto}/registrado
    @GetMapping("/producto/{idProducto}/registrado")
    public ResponseEntity<Boolean> verificarProductoRegistrado(@PathVariable Long idProducto) {
        return ResponseEntity.ok(subastaService.productoYaTieneSubasta(idProducto));
    }

    //Llama a la lógica que consulta al microservicio de Productos
    //Ruta: POST /api/subastas/registrar
    @PostMapping("/registrar")
    public ResponseEntity<SubastaResponseDTO> registrarSubasta(@Valid @RequestBody SubastaRequestDTO dto){
        return ResponseEntity.status(201).body(subastaService.registrarSubasta(dto));
    }
}
