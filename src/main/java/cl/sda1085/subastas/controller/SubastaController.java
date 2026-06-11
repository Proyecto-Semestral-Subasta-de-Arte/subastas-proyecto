package cl.sda1085.subastas.controller;

import cl.sda1085.subastas.assembler.SubastaModelAssembler;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subastas")
@Tag(name = "Gestión de Subastas", description = "Endpoints para el control, consulta y registro distribuido de subastas de productos")

public class SubastaController {

    private final SubastaService subastaService;
    private final SubastaModelAssembler assembler;


    //------------------------------
    //CRUD estándar
    //------------------------------

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Obtener todas las subastas", description = "Retorna el listado histórico y actual de todas las subastas registradas en el sistema.")
    @ApiResponse(responseCode = "200", description = "Lista de subastas obtenida con éxito")
    public ResponseEntity<CollectionModel<SubastaResponseDTO>> obtenerTodas() {
        List<SubastaResponseDTO> subastas = subastaService.obtenerTodas().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<SubastaResponseDTO> model = CollectionModel.of(subastas,
                linkTo(methodOn(SubastaController.class).obtenerTodas()).withSelfRel());

        return ResponseEntity.ok(model);
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Obtener subasta por ID", description = "Busca una subasta específica utilizando su identificador único de base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subasta encontrada de manera exitosa"),
            @ApiResponse(responseCode = "400", description = "Subasta no encontrada o ID inválido", content = @Content(schema = @Schema(implementation = Map.class)))
    })

    public ResponseEntity<SubastaResponseDTO> obtenerPorId(
            @Parameter(description = "ID numérico de la subasta a consultar", example = "1")
            @PathVariable Long id) {

        SubastaResponseDTO dto = subastaService.obtenerPorId(id);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Crear subasta estándar", description = "Crea un registro de subasta básico directo en la base de datos sin validaciones externas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subasta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Errores de validación en los datos de entrada", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<SubastaResponseDTO> crear(
            @Valid @RequestBody SubastaRequestDTO dto) {

        SubastaResponseDTO creado = subastaService.guardar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toModel(creado));
    }

    @PutMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
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

        SubastaResponseDTO actualizado = subastaService.actualizar(id, dto);
        return ResponseEntity.ok(assembler.toModel(actualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una subasta", description = "Remueve permanentemente una subasta del almacenamiento utilizando su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subasta eliminada exitosamente (Sin contenido en la respuesta)"),
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
    @GetMapping(value = "/estado/{estado}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Buscar subastas por estado", description = "Filtra y lista todas las subastas que coincidan con un estado específico utilizando el formato hipermedia HAL JSON.")
    @ApiResponse(responseCode = "200", description = "Listado por estado generado correctamente")
    public ResponseEntity<CollectionModel<SubastaResponseDTO>> buscarPorEstado(
            @Parameter(description = "Nombre del estado a consultar", example = "ABIERTA")
            @PathVariable String estado) {

        List<SubastaResponseDTO> subastas = subastaService.obtenerPorEstado(estado).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<SubastaResponseDTO> model = CollectionModel.of(subastas,
                linkTo(methodOn(SubastaController.class).buscarPorEstado(estado)).withSelfRel());

        return ResponseEntity.ok(model);
    }

    //Busca subastas de un producto específico
    //Ruta: GET /api/subastas/producto/{idProducto}
    @GetMapping(value = "/producto/{idProducto}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Buscar subastas de un producto", description = "Recupera el historial completo de subastas asociadas a un identificador único de producto.")
    @ApiResponse(responseCode = "200", description = "Búsqueda de subastas por producto realizada con éxito")
    public ResponseEntity<CollectionModel<SubastaResponseDTO>> buscarPorIdProducto(
            @Parameter(description = "ID del producto consultado", example = "5")
            @PathVariable Long idProducto) {

        List<SubastaResponseDTO> subastas = subastaService.obtenerPorIdProducto(idProducto).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<SubastaResponseDTO> model = CollectionModel.of(subastas,
                linkTo(methodOn(SubastaController.class).buscarPorIdProducto(idProducto)).withSelfRel());

        return ResponseEntity.ok(model);
    }

    //Busca subastas que finalizan antes de una fecha/hora específica
    //Ruta: GET /api/subastas/vencimiento?fecha=2026-05-17T21:00:00
    @GetMapping(value = "/vencimiento", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Buscar subastas próximas a vencer", description = "Lista las subastas que tienen planificado finalizar antes de la fecha y hora enviada por parámetro de consulta.")
    @ApiResponse(responseCode = "200", description = "Consulta temporal procesada exitosamente")
    public ResponseEntity<CollectionModel<SubastaResponseDTO>> buscarPorVencimiento(
            @Parameter(description = "Fecha límite en formato ISO (yyyy-MM-ddTHH:mm:ss)", example = "2026-05-17T21:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {

        List<SubastaResponseDTO> subastas = subastaService.obtenerSubastasPorVencer(fecha).stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<SubastaResponseDTO> model = CollectionModel.of(subastas,
                linkTo(methodOn(SubastaController.class).buscarPorVencimiento(fecha)).withSelfRel());

        return ResponseEntity.ok(model);
    }

    //Busca la subasta activa de un producto específico
    //Ruta: GET /api/subastas/producto/{idProducto}/activa
    @GetMapping(value = "/producto/{idProducto}/activa", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Obtener subasta activa de un producto", description = "Busca si existe un proceso de subasta actualmente en estado 'ABIERTA' para un ítem en particular.")
    @ApiResponse(responseCode = "200", description = "Consulta realizada (puede retornar los datos de la subasta o error controlado si no existe)")
    public ResponseEntity<SubastaResponseDTO> buscarActivaPorProducto(
            @Parameter(description = "ID del producto a verificar", example = "1")
            @PathVariable Long idProducto) {

        SubastaResponseDTO dto = subastaService.obtenerSubastaActivaProducto(idProducto);
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    //Verifica si un vendedor ya tiene una subasta activa
    //Ruta: GET /api/subastas/vendedor/{idVendedor}/activa
    @GetMapping(value = "/vendedor/{idVendedor}/activa", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Verificar si un vendedor posee subastas activas", description = "Comprueba mediante un booleano si el usuario vendedor ya cuenta con subastas en estado 'ABIERTA'.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    public ResponseEntity<Boolean> verificarVendedorActivo(
            @Parameter(description = "ID único del usuario vendedor", example = "10")
            @PathVariable Long idVendedor) {

        return ResponseEntity.ok(subastaService.vendedorTieneSubastaActiva(idVendedor));
    }

    //Encuentra la subasta que terminará más pronto (la más urgente)
    //Ruta: GET /api/subastas/urgente
    @GetMapping(value = "/urgente", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Obtener la subasta más urgente", description = "Recupera la subasta activa cuyo tiempo de cierre o fecha de término está más cercana al momento actual.")
    @ApiResponse(responseCode = "200", description = "Subasta urgente obtenida")
    public ResponseEntity<SubastaResponseDTO> obtenerMasUrgente() {
        SubastaResponseDTO dto = subastaService.obtenerSubastaMasUrgente();
        return ResponseEntity.ok(assembler.toModel(dto));
    }

    //Verifica si un producto ya está registrado en alguna subasta
    //Ruta: GET /api/subastas/producto/{idProducto}/registrado
    @GetMapping(value = "/producto/{idProducto}/registrado", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Verificar si un producto ya está en alguna subasta.", description = "Retorna verdadero o falso si el identificador del producto ya se encuentra registrado en el sistema, omitiendo su estado actual.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa.")
    public ResponseEntity<Boolean> verificarProductoRegistrado(
            @Parameter(description = "ID del producto a chequear en el histórico", example = "1")
            @PathVariable Long idProducto) {

        return ResponseEntity.ok(subastaService.productoYaTieneSubasta(idProducto));
    }

    //Llama a la lógica que consulta al microservicio de Productos
    //Ruta: POST /api/subastas/registrar
    @PostMapping(value = "/registrar", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Registrar subasta verificada (WebClient)", description = "Lógica de negocio avanzada. Conecta síncronamente con el microservicio 'productos' (puerto 8082) para verificar que el producto exista antes de guardar la subasta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Validación externa aprobada y subasta registrada correctamente."),
            @ApiResponse(responseCode = "400", description = "Lógica inválida (ej. fecha término anterior a inicio) o el producto no existe en el catálogo externo", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<SubastaResponseDTO> registrarSubasta(@Valid @RequestBody SubastaRequestDTO dto){
        SubastaResponseDTO registrado = subastaService.registrarSubasta(dto);
        return ResponseEntity.status(201).body(assembler.toModel(registrado));
    }
}
