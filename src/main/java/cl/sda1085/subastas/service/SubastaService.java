package cl.sda1085.subastas.service;

import cl.sda1085.subastas.dto.SubastaRequestDTO;
import cl.sda1085.subastas.dto.SubastaResponseDTO;
import cl.sda1085.subastas.model.Subasta;
import cl.sda1085.subastas.repository.SubastaRepository;
import cl.sda1085.subastas.webclient.ProductoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubastaService {

    private final SubastaRepository subastaRepository;
    private final ProductoClient productoClient;

    //Método de apoyo para encapsulamiento de datos
    private SubastaResponseDTO mapToResponseDTO(Subasta subasta){
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

    //Método que convierte 'DTO' en una entidad
    private Subasta mapToEntity(SubastaRequestDTO dto) {
        Subasta subasta = new Subasta();
        subasta.setIdProducto(dto.getIdProducto());
        subasta.setIdVendedor(dto.getIdVendedor());
        subasta.setPrecioBase(dto.getPrecioBase());
        subasta.setFechaInicio(dto.getFechaInicio());
        subasta.setFechaTermino(dto.getFechaTermino());
        subasta.setEstado("PROGRAMADA");

        return subasta;
    }


    //------------------------------
    //CRUD estándar
    //------------------------------

    //Obtener todas las subastas
    public List<SubastaResponseDTO> obtenerTodas(){
        log.info("Buscando la lista completa de subastas en el sistema.");

        return subastaRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    //Obtener por ID
    public SubastaResponseDTO obtenerPorId(Long id){
        log.info("Buscando subasta por ID exacto: {}", id);

        return subastaRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> {
                    log.warn("Búsqueda fallida: No existe ninguna subasta con el ID: {}", id);
                    return new RuntimeException("Subasta no encontrada con el ID: " + id + ".");
                });
    }

    //Crear (guardar) con validación de fechas y verificación de existencia del producto
    public SubastaResponseDTO guardar(SubastaRequestDTO dto){
        log.info("Iniciando registro de subasta local para el producto ID: {}", dto.getIdProducto());

        if (dto.getFechaInicio().isAfter(dto.getFechaTermino())){
            log.warn("Validación de fechas rechazada: Fecha de inicio {} es posterior a la de término {}", dto.getFechaInicio(), dto.getFechaTermino());
            throw new RuntimeException("La fecha de inicio no puede ser posterior a la de término.");
        }

        //Validar que el producto exista consumiendo el WebClient antes de guardar
        log.info("Verificando existencia del producto ID: {} a través de ProductoClient...", dto.getIdProducto());
        productoClient.obtenerProductoPorId(dto.getIdProducto());
        log.info("Producto verificado con éxito de forma distribuida.");

        Subasta subasta = new Subasta();
        subasta.setIdProducto(dto.getIdProducto());
        subasta.setIdVendedor(dto.getIdVendedor());
        subasta.setPrecioBase(dto.getPrecioBase());
        subasta.setFechaInicio(dto.getFechaInicio());
        subasta.setFechaTermino(dto.getFechaTermino());
        subasta.setEstado("PROGRAMADA");  //Estado inicial por defecto

        Subasta guardada = subastaRepository.save(subasta);
        log.info("Subasta guardada exitosamente bajo el ID: {}", guardada.getId());

        return mapToResponseDTO(guardada);
    }

    //Actualizar subasta existente
    @Transactional
    public SubastaResponseDTO actualizar(Long id, SubastaRequestDTO dto){
        log.info("Iniciando actualización para la subasta con ID: {}", id);

        return subastaRepository.findById(id).map(subastaExistente -> {

            //Validar que el nuevo ID de producto exista antes de actualizar el registro
            log.info("Verificando existencia del nuevo producto ID: {} para actualización...", dto.getIdProducto());
            productoClient.obtenerProductoPorId(dto.getIdProducto());

            subastaExistente.setIdProducto(dto.getIdProducto());
            subastaExistente.setIdVendedor(dto.getIdVendedor());
            subastaExistente.setPrecioBase(dto.getPrecioBase());
            subastaExistente.setFechaInicio(dto.getFechaInicio());
            subastaExistente.setFechaTermino(dto.getFechaTermino());
            subastaExistente.setEstado(dto.getEstado());

            Subasta subastaActualizada = subastaRepository.save(subastaExistente);
            log.info("Subasta ID: {} actualizada exitosamente en la base de datos", id);
            return mapToResponseDTO(subastaActualizada);

        }).orElseThrow(() -> {
            log.warn("Actualización fallida: Imposible modificar. La subasta con ID: {} no existe", id);
            return new RuntimeException("No se puede actualizar. Subasta no encontrada con el ID: " + id + ".");
        });
    }

    //Eliminar subasta
    @Transactional
    public void eliminar(Long id){
        log.info("Solicitud recibida para eliminar la subasta con ID: {}", id);

        if (!subastaRepository.existsById(id)) {
            log.warn("Eliminación fallida: La subasta con ID: {} no existe en el sistema", id);
            throw new RuntimeException("No se puede eliminar. Subasta no encontrada con el ID: " + id + ".");
        }

        subastaRepository.deleteById(id);
        log.info("Subasta con ID: {} eliminada correctamente del sistema", id);
    }


    //------------------------------
    //CRUD personalizado
    //------------------------------

    //Buscar subastas por estado
    public List<SubastaResponseDTO> obtenerPorEstado(String estado){
        log.info("Filtrando subastas por estado actual: '{}'", estado);

        List<SubastaResponseDTO> resultados = subastaRepository.findByEstado(estado).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        if (resultados.isEmpty()) {
            log.warn("Filtro vacío: No se encontraron subastas en estado '{}'", estado);
            throw new RuntimeException("No se encontraron subastas registradas con el estado: " + estado + ".");
        }
        return resultados;
    }

    //Buscar subastas de un producto específico
    public List<SubastaResponseDTO> obtenerPorIdProducto(Long idProducto){
        log.info("Buscando el historial de subastas asociadas al producto ID: {}", idProducto);

        List<SubastaResponseDTO> resultados = subastaRepository.findByIdProducto(idProducto).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        if (resultados.isEmpty()) {
            log.warn("Consulta vacía: El producto ID {} no registra subastas asociadas", idProducto);
            throw new RuntimeException("No se encontraron subastas para el producto con ID: " + idProducto + ".");
        }
        return resultados;
    }

    //Buscar subastas que finalizan antes de una fecha
    public List<SubastaResponseDTO> obtenerSubastasPorVencer(LocalDateTime fecha){
        log.info("Buscando subastas activas cuya fecha de término sea previa a: {}", fecha);

        List<SubastaResponseDTO> resultados = subastaRepository.findByFechaTerminoBefore(fecha).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        if (resultados.isEmpty()) {
            log.info("No se encontraron subastas próximas a vencer antes del umbral solicitado.");
        }
        return resultados;
    }

    //Buscar subasta activa de un producto
    public SubastaResponseDTO obtenerSubastaActivaProducto(Long idProducto){
        log.info("Buscando subasta con estado 'ABIERTA' para el producto ID: {}", idProducto);

        return subastaRepository.findByIdProductoAndEstado(idProducto, "ABIERTA")
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> {
                    log.warn("Consulta vacía: No hay ninguna subasta ABIERTA actualmente para el producto ID: {}", idProducto);
                    return new RuntimeException("No existe una subasta activa (ABIERTA) para el producto ID: " + idProducto + ".");
                });
    }

    //Verificar si un vendedor ya tiene una subasta activa
    public boolean vendedorTieneSubastaActiva(Long idVendedor){
        log.info("Verificando si el vendedor ID: {} posee actualmente subastas en estado ABIERTA", idVendedor);
        boolean tieneActiva = subastaRepository.existsByIdVendedorAndEstado(idVendedor, "ABIERTA");

        log.info("Resultado de verificación para vendedor ID {}: {}", idVendedor, tieneActiva);
        return tieneActiva;
    }

    //Encontrar la subasta que terminará más pronto
    public SubastaResponseDTO obtenerSubastaMasUrgente(){
        log.info("Consultando la subasta activa con el tiempo de cierre más próximo.");

        return subastaRepository.findTopByEstadoOrderByFechaTerminoAsc("ABIERTA")
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> {
                    log.warn("Consulta vacía: No existen subastas en estado 'ABIERTA' en este momento.");
                    return new RuntimeException("No se pudo determinar la subasta más urgente porque no hay salas abiertas.");
                });
    }

    //Verificar si un producto ya está registrado en el sistema
    public boolean productoYaTieneSubasta(Long idProducto){
        log.info("Verificando de seguridad: ¿El producto ID {} ya tiene una subasta creada?", idProducto);
        return subastaRepository.existsByIdProducto(idProducto);
    }

    //Tarea programada (ejecución cada un minuto) que busca subastas abiertas cuya fecha de término ya pasó y las cierra
    @Scheduled(fixedRate = 60000)  //Scheduled indica que el metodo debe ejecutarse automaticamente contando desde el inicio de la última ejecución, fixedRate es el tiempo en milisegundos
    public void cerrarSubastasVencidas(){
        LocalDateTime fechaInicio = LocalDateTime.now();
        log.info("[Cron Scheduler] Iniciando revisión automática de subastas vencidas a las {}", fechaInicio);

        //Buscar las subastas que deberían haber terminado
        List<Subasta> vencidas = subastaRepository.findByFechaTerminoBefore(fechaInicio);
        int contadorCierres = 0;

        for (Subasta subasta : vencidas) {  //Sólo cierra las que aún figuran como estado abiertas o programadas
            if (!subasta.getEstado().equals("CERRADA")) {
                subasta.setEstado("CERRADA");
                subastaRepository.save(subasta);

                log.info("[Cron Scheduler] Subasta ID {} marcada como CERRADA por cumplimiento de tiempo", subasta.getId());
                contadorCierres++;
            }
        }
        log.info("[Cron Scheduler] Revisión finalizada de forma exitosa. Subastas clausuradas en esta tanda: {}", contadorCierres);
    }

    //Registrar subasta a partir de WebClient
    @Transactional
    public SubastaResponseDTO registrarSubasta(SubastaRequestDTO dto){
        log.info("Iniciando solicitud distribuida de registro de subasta vía WebClient para producto ID: {}", dto.getIdProducto());

        if (dto.getFechaInicio().isAfter(dto.getFechaTermino())){  //Validación de fechas
            log.warn("Fallo de validación temporal: Fecha de inicio es posterior a la de término.");
            throw new RuntimeException("La fecha de inicio no puede ser posterior a la fecha de término.");
        }

        log.info("Llamando a microservicio 'productos' (puerto 8082) para validar existencia del ítem...");
        productoClient.obtenerProductoPorId(dto.getIdProducto());  //Validación de producto (WebClient)

        log.info("Comunicación exitosa. El producto existe y está habilitado.");
        Subasta subasta = mapToEntity(dto);  //Mapeo único de la entidad

        if (subasta.getEstado() == null){
            subasta.setEstado("PROGRAMADA");  //Asegurar el estado por defecto
        }

        Subasta subastaGuardada = subastaRepository.save(subasta);  //Guardar en la base de datos
        log.info("Subasta distribuida registrada impecablemente con el ID asignado: {}", subastaGuardada.getId());

        return mapToResponseDTO(subastaGuardada);
    }
}
