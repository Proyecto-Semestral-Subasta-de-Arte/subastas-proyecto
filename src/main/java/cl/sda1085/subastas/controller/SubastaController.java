package cl.sda1085.subastas.controller;

import cl.sda1085.subastas.dto.SubastaRequestDTO;
import cl.sda1085.subastas.dto.SubastaResponseDTO;
import cl.sda1085.subastas.service.SubastaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subastas")
@Tag(name = "Gestión de Subastas", description = "Endpoints para el control, consulta y registro distribuido de subastas de productos")

public class SubastaController {

    private final SubastaService subastaService;


    //------------------------------
    //CRUD estándar
    //------------------------------

    @GetMapping
    @Operation(summary = "Obtener todas las subastas", description = "Retorna el listado histórico y actual de todas las subastas registradas en el sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de subastas obtenida con éxito")
    public ResponseEntity<List<SubastaResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(subastaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener subasta por ID", description = "Busca una subasta específica utilizando su identificador único de base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subasta encontrada de manera exitosa"),
            @ApiResponse(responseCode = "400", description = "Subasta no encontrada o ID inválido", content = @Content(schema = @Schema(implementation = Map.class)))
    })

    public ResponseEntity<SubastaResponseDTO> obtenerPorId(
            @Parameter(description = "ID numérico de la subasta a consultar", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(subastaService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear subasta estándar", description = "Crea un registro de subasta básico directo en la base de datos sin validaciones externas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subasta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Errores de validación en los datos de entrada", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<SubastaResponseDTO> crear(
            @Valid @RequestBody SubastaRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subastaService.guardar(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una subasta existente", description = "Modifica los valores de una subasta mediante su ID de base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subasta actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o inconsistencia en las fechas", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "La subasta con el ID proporcionado no existe en el sistema", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<SubastaResponseDTO> actualizar(
            @Parameter(description = "ID de la subasta que se desea modificar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody SubastaRequestDTO dto) {

        return ResponseEntity.ok(subastaService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una subasta", description = "Remueve permanentemente una subasta del almacenamiento utilizando su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "24", description = "Subasta eliminada exitosamente (Sin contenido en la respuesta)"),
            @ApiResponse(responseCode = "404", description = "No se encontró la subasta a eliminar", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la subasta que se desea dar de baja", example = "1")
            @PathVariable Long id) {
        subastaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }


    //------------------------------
    //CRUD personalizado
    //------------------------------

    //Busca subastas activas por su estado
    //Ruta: GET /api/subastas/estado/ABIERTA
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SubastaResponseDTO>> buscarPorEstado(
            @Parameter(description = "Nombre del estado a consultar", example = "ABIERTA")
            @PathVariable String estado) {
        return ResponseEntity.ok(subastaService.obtenerPorEstado(estado));
    }

    //Busca subastas de un producto específico
    //Ruta: GET /api/subastas/producto/{idProducto}
    @GetMapping("/producto/{idProducto}")
    @Operation(summary = "Buscar subastas de un producto", description = "Recupera el historial completo de subastas asociadas a un identificador único de producto.")
    @ApiResponse(responseCode = "200", description = "Búsqueda de subastas por producto realizada con éxito")
    public ResponseEntity<List<SubastaResponseDTO>> buscarPorIdProducto(
            @Parameter(description = "ID del producto consultado", example = "5")
            @PathVariable Long idProducto) {

        return ResponseEntity.ok(subastaService.obtenerPorIdProducto(idProducto));
    }

    //Busca subastas que finalizan antes de una fecha/hora específica
    //Ruta: GET /api/subastas/vencimiento?fecha=2026-05-17T21:00:00
    @GetMapping("/vencimiento")
    @Operation(summary = "Buscar subastas próximas a vencer", description = "Lista las subastas que tienen planificado finalizar antes de la fecha y hora enviada por parámetro de consulta.")
    @ApiResponse(responseCode = "200", description = "Consulta temporal procesada exitosamente")
    public ResponseEntity<List<SubastaResponseDTO>> buscarPorVencimiento(
            @Parameter(description = "Fecha límite en formato ISO (yyyy-MM-ddTHH:mm:ss)", example = "2026-05-17T21:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        return ResponseEntity.ok(subastaService.obtenerSubastasPorVencer(fecha));
    }

    //Busca la subasta activa de un producto específico
    //Ruta: GET /api/subastas/producto/{idProducto}/activa
    @GetMapping("/producto/{idProducto}/activa")
    @Operation(summary = "Obtener subasta activa de un producto", description = "Busca si existe un proceso de subasta actualmente en estado 'ABIERTA' para un ítem en particular.")
    @ApiResponse(responseCode = "200", description = "Consulta realizada (puede retornar los datos de la subasta o error controlado si no existe)")
    public ResponseEntity<SubastaResponseDTO> buscarActivaPorProducto(
            @Parameter(description = "ID del producto a verificar", example = "1")
            @PathVariable Long idProducto) {
        return ResponseEntity.ok(subastaService.obtenerSubastaActivaProducto(idProducto));
    }

    //Verifica si un vendedor ya tiene una subasta activa
    //Ruta: GET /api/subastas/vendedor/{idVendedor}/activa
    @GetMapping("/vendedor/{idVendedor}/activa")
    @Operation(summary = "Verificar si un vendedor posee subastas activas", description = "Comprueba mediante un booleano si el usuario vendedor ya cuenta con subastas en estado 'ABIERTA'.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    public ResponseEntity<Boolean> verificarVendedorActivo(
            @Parameter(description = "ID único del usuario vendedor", example = "10")
            @PathVariable Long idVendedor) {
        return ResponseEntity.ok(subastaService.vendedorTieneSubastaActiva(idVendedor));
    }

    //Encuentra la subasta que terminará más pronto (la más urgente)
    //Ruta: GET /api/subastas/urgente
    @GetMapping("/urgente")
    @Operation(summary = "Obtener la subasta más urgente", description = "Recupera la subasta activa cuyo tiempo de cierre o fecha de término está más cercana al momento actual.")
    @ApiResponse(responseCode = "200", description = "Subasta urgente obtenida")
    public ResponseEntity<SubastaResponseDTO> obtenerMasUrgente() {
        return ResponseEntity.ok(subastaService.obtenerSubastaMasUrgente());
    }

    //Verifica si un producto ya está registrado en alguna subasta
    //Ruta: GET /api/subastas/producto/{idProducto}/registrado
    @GetMapping("/producto/{idProducto}/registrado")
    @Operation(summary = "Verificar si un producto ya está en alguna subasta", description = "Retorna verdadero o falso si el identificador del producto ya se encuentra registrado en el sistema, omitiendo su estado actual.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    public ResponseEntity<Boolean> verificarProductoRegistrado(
            @Parameter(description = "ID del producto a chequear en el histórico", example = "1")
            @PathVariable Long idProducto) {
        return ResponseEntity.ok(subastaService.productoYaTieneSubasta(idProducto));
    }

    //Llama a la lógica que consulta al microservicio de Productos
    //Ruta: POST /api/subastas/registrar
    @PostMapping("/registrar")
    @Operation(summary = "Registrar subasta verificada (WebClient)", description = "Lógica de negocio avanzada. Conecta síncronamente con el microservicio externo de Productos (puerto 8082) para verificar que el producto exista antes de guardar la subasta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validación externa aprobada y subasta registrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Lógica inválida (ej. fecha término anterior a inicio) o el producto no existe en el catálogo externo", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<SubastaResponseDTO> registrarSubasta(@Valid @RequestBody SubastaRequestDTO dto){
        return ResponseEntity.status(201).body(subastaService.registrarSubasta(dto));
    }
}
