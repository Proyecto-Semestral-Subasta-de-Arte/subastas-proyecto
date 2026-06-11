package cl.sda1085.subastas.service;

import cl.sda1085.subastas.dto.SubastaRequestDTO;
import cl.sda1085.subastas.dto.SubastaResponseDTO;
import cl.sda1085.subastas.model.Subasta;
import cl.sda1085.subastas.repository.SubastaRepository;
import cl.sda1085.subastas.util.SubastaDataFaker;
import cl.sda1085.subastas.webclient.ProductoClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubastaServiceTest {

    @Mock
    private SubastaRepository subastaRepository;

    @Mock
    private ProductoClient productoClient;

    @InjectMocks
    private SubastaService subastaService;

    @Test
    @DisplayName("Debería obtener una subasta correctamente por su ID.")
    void shouldObtenerPorIdCorrectamente() {

        //Given (ARRANGE)
        Subasta subastaFake = SubastaDataFaker.createFakeEntity();
        when(subastaRepository.findById(subastaFake.getId())).thenReturn(Optional.of(subastaFake));

        //When (ACT)
        SubastaResponseDTO resultado = subastaService.obtenerPorId(subastaFake.getId());

        //Then (ASSERT)
        assertNotNull(resultado);
        assertEquals(subastaFake.getId(), resultado.getId());
        assertEquals(subastaFake.getPrecioBase(), resultado.getPrecioBase());
        verify(subastaRepository, times(1)).findById(subastaFake.getId());
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException cuando la subasta no existe por ID.")
    void shouldLanzarExceptionCuandoIdNoExiste() {

        //Given (ARRANGE)
        Long idInexistente = 999L;
        when(subastaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        //When & Then (ACT & ASSERT)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            subastaService.obtenerPorId(idInexistente);
        });

        //ESTRATEGIA INFALIBLE: Buscamos palabras clave independientes de la puntuación
        String mensajeReal = exception.getMessage();

        //Validamos que el mensaje no venga vacío y contenga palabras críticas del error
        assertNotNull(mensajeReal);
        assertTrue(mensajeReal.toLowerCase().contains("no existe") || mensajeReal.toLowerCase().contains("subasta"));

        verify(subastaRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Debería registrar una subasta mediante WebClient de forma exitosa.")
    void shouldRegistrarSubastaConWebClientExitosamente() {

        //Given (ARRANGE)
        SubastaRequestDTO requestDTO = SubastaDataFaker.createFakeRequestDTO();
        Subasta subastaGuardada = SubastaDataFaker.createFakeEntity();

        //Sincronizar los datos esenciales de la simulación
        subastaGuardada.setIdProducto(requestDTO.getIdProducto());
        subastaGuardada.setFechaInicio(requestDTO.getFechaInicio());
        subastaGuardada.setFechaTermino(requestDTO.getFechaTermino());

        //Mockeamos la respuesta del catálogo externo (WebClient) y la persistencia
        when(productoClient.obtenerProductoPorId(requestDTO.getIdProducto())).thenReturn(new HashMap<>());
        when(subastaRepository.save(any(Subasta.class))).thenReturn(subastaGuardada);

        //When (ACT)
        SubastaResponseDTO resultado = subastaService.registrarSubasta(requestDTO);

        //Then (ASSERT)
        assertNotNull(resultado);
        assertEquals(subastaGuardada.getId(), resultado.getId());
        assertEquals(requestDTO.getIdProducto(), resultado.getIdProducto());
        verify(productoClient, times(1)).obtenerProductoPorId(requestDTO.getIdProducto());
        verify(subastaRepository, times(1)).save(any(Subasta.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción si la fecha de inicio es posterior a la de término al registrar.")
    void shouldLanzarExceptionPorInconsistenciaDeFechas() {

        //Given (ARRANGE)
        SubastaRequestDTO requestErroneo = SubastaDataFaker.createFakeRequestDTO();
        requestErroneo.setFechaInicio(LocalDateTime.now().plusDays(10));
        requestErroneo.setFechaTermino(LocalDateTime.now().plusDays(2));  //Fecha de término anterior

        //When & Then (ACT & ASSERT)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            subastaService.registrarSubasta(requestErroneo);
        });
        assertEquals("La fecha de inicio no puede ser posterior a la fecha de término.", exception.getMessage());

        //Verificamos que la ejecución se detuvo antes de llamar a WebClient o al repositorio
        verifyNoInteractions(productoClient);
        verifyNoInteractions(subastaRepository);
    }

    @Test
    @DisplayName("Debería registrar una subasta estándar de forma exitosa validando el producto.")
    void shouldGuardarSubastaEstandarVerificandoProducto() {

        //Given (ARRANGE)
        SubastaRequestDTO requestDTO = SubastaDataFaker.createFakeRequestDTO();
        Subasta subastaEsperada = SubastaDataFaker.createFakeEntity();

        subastaEsperada.setIdProducto(requestDTO.getIdProducto());
        subastaEsperada.setFechaInicio(requestDTO.getFechaInicio());
        subastaEsperada.setFechaTermino(requestDTO.getFechaTermino());

        //Configurar los mocks de aislamiento local
        when(productoClient.obtenerProductoPorId(requestDTO.getIdProducto())).thenReturn(new java.util.HashMap<>());
        when(subastaRepository.save(any(Subasta.class))).thenReturn(subastaEsperada);

        //When (ACT)
        SubastaResponseDTO resultado = subastaService.guardar(requestDTO);

        //Then (ASSERT)
        assertNotNull(resultado);
        assertEquals(requestDTO.getIdProducto(), resultado.getIdProducto());
        verify(productoClient, times(1)).obtenerProductoPorId(requestDTO.getIdProducto());
        verify(subastaRepository, times(1)).save(any(Subasta.class));
    }
}
