package com.Tingeso.backend.Service;

import com.Tingeso.backend.Entity.ReservaEntity;
import com.Tingeso.backend.Repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*; // Importa when, verify, etc.
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {
    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaService reservaService;

    // Formato estándar para las fechas/horas en los tests
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @BeforeEach
    void setUp() {

    }

    @Test
    void crearReserva() {
        // Arrange: Preparamos los datos y mocks
        String fechaHora = "2025-07-15T15:00"; // Martes a las 3 PM (válido)
        int tipoReserva = 1; // Normal (30 min)
        int cantPersonas = 4;
        int cantCumple = 1;
        String nombre = "Usuario Test";
        String rut = "12345678-9";
        String email = "test@test.com";

        // Simulamos que no hay reservas existentes que se superpongan
        when(reservaRepository.findAll()).thenReturn(new ArrayList<>()); // Devuelve lista vacía

        // Simulamos que el método save funciona y devuelve la reserva guardada (podemos añadir un ID simulado)
        // Usamos any(ReservaEntity.class) porque no sabemos exactamente la instancia que se creará dentro
        when(reservaRepository.save(any(ReservaEntity.class))).thenAnswer(invocation -> {
            ReservaEntity reservaGuardada = invocation.getArgument(0);
            // reservaGuardada.setId(1L); // Opcional: Simular que se le asigna un ID
            return reservaGuardada;
        });

        // Act: Ejecutamos el método a probar
        ReservaEntity resultado = reservaService.crearReserva(fechaHora, tipoReserva, cantPersonas, cantCumple, nombre, rut, email);

        // Assert: Verificamos los resultados
        assertNotNull(resultado); // No debería ser nulo
        assertEquals(fechaHora, resultado.getFechahora());
        assertEquals(tipoReserva, resultado.getTiporeserva());
        assertEquals(cantPersonas, resultado.getCantidadpersonas());
        assertEquals(cantCumple, resultado.getCantidadcumple());
        assertEquals(nombre, resultado.getNombreusuario());
        assertEquals(rut, resultado.getRutusuario());
        assertEquals(email, resultado.getEmail());

        // Verificamos que el método save del repositorio fue llamado exactamente 1 vez
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
        // Verificamos que se llamó a findAll para chequear superposiciones
        verify(reservaRepository, times(1)).findAll();
    }

    @Test
    void testCrearReserva_Falla_Superposicion() {
        // Arrange: Preparamos datos y una reserva existente que cause conflicto
        String fechaHoraNueva = "2025-07-15T16:00"; // Martes 4 PM (Normal, 30 min -> hasta 16:30)
        int tipoReservaNueva = 1;
        int cantPersonasNueva = 2;

        // Creamos una reserva existente simulada
        ReservaEntity existente = new ReservaEntity(0, 5, "otro@test.com", "2025-07-15T16:15", "Otro User", "98765432-1", 2); // Extendida (35 min -> hasta 16:50)
        List<ReservaEntity> listaExistentes = new ArrayList<>();
        listaExistentes.add(existente);

        // Simulamos que findAll devuelve la reserva existente
        when(reservaRepository.findAll()).thenReturn(listaExistentes);

        // Act & Assert: Esperamos que se lance una IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(fechaHoraNueva, tipoReservaNueva, cantPersonasNueva, 0, "User Sup", "11111111-1", "sup@test.com");
        });

        // Verificamos el mensaje de la excepción
        assertTrue(exception.getMessage().contains("La reserva se superpone con una existente"));

        // Verificamos que save NUNCA fue llamado
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
        // Verificamos que findAll SÍ fue llamado
        verify(reservaRepository, times(1)).findAll();
    }

    @Test
    void testCrearReserva_Falla_FueraDeHorario_Semana() {
        // Arrange
        String fechaHora = "2025-07-15T13:00"; // Martes a la 1 PM (antes de las 14:00)

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(fechaHora, 1, 2, 0, "User Temprano", "22222222-2", "temp@test.com");
        });
        assertTrue(exception.getMessage().contains("La hora de inicio está fuera del horario de atención"));
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
        // findAll no se llama si falla antes la validación de horario
        verify(reservaRepository, never()).findAll();
    }

    @Test
    void testCrearReserva_Falla_ExcedeCierre() {
        // Arrange
        String fechaHora = "2025-07-15T21:45"; // Martes 21:45
        int tipoReserva = 3; // Premium (40 min) -> Terminaría 22:25

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(fechaHora, tipoReserva, 2, 0, "User Tarde", "33333333-3", "tarde@test.com");
        });
        assertTrue(exception.getMessage().contains("La reserva excede el horario de cierre"));
        verify(reservaRepository, never()).save(any(ReservaEntity.class));
        verify(reservaRepository, never()).findAll(); // No llega a chequear superposición
    }


    @Test
    void testObtenerReservasUsuario() {
        // Arrange
        String rut = "12345678-9";
        ReservaEntity res1 = new ReservaEntity(0, 2, "a@a.com", "2025-01-10T10:00", "User A", rut, 1);
        ReservaEntity res2 = new ReservaEntity(1, 4, "b@b.com", "2025-02-15T15:00", "User B", rut, 2);
        List<ReservaEntity> mockLista = List.of(res1, res2);
        when(reservaRepository.findAllByRutusuario(rut)).thenReturn(mockLista);

        // Act
        List<ReservaEntity> resultado = reservaService.obtenerReservasUsuario(rut);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(mockLista, resultado);
        verify(reservaRepository, times(1)).findAllByRutusuario(rut);
    }

    @Test
    void testCalcularPrecioInicial_Tipo1_Semana() {
        // Arrange
        long idReserva = 1L;
        // Martes, tipo 1, 3 personas
        ReservaEntity reserva = new ReservaEntity(0, 3, "c@c.com", "2025-07-15T18:00", "User C", "111", 1);
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reserva));
        float precioEsperado = 15000 * 3; // Sin descuentos de día

        // Act
        float precioCalculado = reservaService.calcularprecioinicial(idReserva);

        // Assert
        assertEquals(precioEsperado, precioCalculado, 0.01); // Usar delta para floats
        verify(reservaRepository, times(1)).findById(idReserva);
    }

    @Test
    void testCalcularPrecioInicial_Tipo2_Feriado() {
        // Arrange
        long idReserva = 2L;
        // 1 Mayo (Feriado), tipo 2, 2 personas
        ReservaEntity reserva = new ReservaEntity(0, 2, "d@d.com", "2025-05-01T11:00", "User D", "222", 2);
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.of(reserva));
        float precioBase = 20000 * 2;
        float precioEsperado = precioBase - (0.25f * precioBase); // 25% descuento feriado

        // Act
        float precioCalculado = reservaService.calcularprecioinicial(idReserva);

        // Assert
        assertEquals(precioEsperado, precioCalculado, 0.01);
        verify(reservaRepository, times(1)).findById(idReserva);
    }

    @Test
    void testCalcularPrecioInicial_NotFound() {
        // Arrange
        long idReserva = 99L;
        when(reservaRepository.findById(idReserva)).thenReturn(Optional.empty()); // Simula no encontrar la reserva

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.calcularprecioinicial(idReserva);
        });
        assertTrue(exception.getMessage().contains("Reserva no encontrada"));
        verify(reservaRepository, times(1)).findById(idReserva);
    }


    @Test
    void testCalcularDescuentoGrupo() {
        // Arrange
        float precioBase = 100000f;

        // Act & Assert
        assertEquals(0f, reservaService.calcularDescuentoGrupo(1, precioBase), 0.01);
        assertEquals(0f, reservaService.calcularDescuentoGrupo(2, precioBase), 0.01);
        assertEquals(10000f, reservaService.calcularDescuentoGrupo(3, precioBase), 0.01); // 10%
        assertEquals(10000f, reservaService.calcularDescuentoGrupo(5, precioBase), 0.01); // 10%
        assertEquals(20000f, reservaService.calcularDescuentoGrupo(6, precioBase), 0.01); // 20%
        assertEquals(20000f, reservaService.calcularDescuentoGrupo(10, precioBase), 0.01); // 20%
        assertEquals(30000f, reservaService.calcularDescuentoGrupo(11, precioBase), 0.01); // 30%
        assertEquals(30000f, reservaService.calcularDescuentoGrupo(15, precioBase), 0.01); // 30%
        assertEquals(0f, reservaService.calcularDescuentoGrupo(16, precioBase), 0.01); // 0%
    }



    @Test
    void testDescuentoPorCumpleano() {
        // Arrange
        float precioInicial = 60000f;
        int cantPersonas = 4; // Grupo 3-5
        float tarifa = precioInicial / cantPersonas;

        // Act & Assert
        assertEquals(0, reservaService.descuentoporcumpleano(2, 30000, 1), 0.01);
    }
}