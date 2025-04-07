package com.Tingeso.backend.Service;

import com.Tingeso.backend.Entity.ComprobanteEntity;
import com.Tingeso.backend.Entity.ReservaEntity;
import com.Tingeso.backend.Repository.ComprobanteRepository;
import com.Tingeso.backend.Repository.ReservaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ComprobanteService {
    @Autowired
    public ComprobanteRepository comprobanteRepository;
    @Autowired
    public ReservaRepository reservaRepository;
    @Autowired
    public ReservaService reservaService;

    @Transactional
    //metodo para crear un comprobante
    public ComprobanteEntity crearcomprobante(long idreserva) {
        Optional<ReservaEntity> reservaop = reservaRepository.findById(idreserva);
        if (reservaop.isEmpty()) {
            throw new IllegalArgumentException("Reserva no encontrada");
        }
        ReservaEntity reserva = reservaop.get();
        float precioinicial = reservaService.calcularprecioinicial(idreserva);
        if (reserva.getCantidadpersonas() <= 0) {
            throw new IllegalArgumentException("La cantidad de personas debe ser mayor que 0");
        }
        if (precioinicial <= 0) {
            throw new IllegalArgumentException("El precio inicial debe ser mayor que 0");
        }
        float tarifabase = precioinicial / reserva.getCantidadpersonas();

        float dctoespecial = reservaService.calcularDescuentoEspecial(reserva.getIduser(), precioinicial);
        float dctogrupo = reservaService.calcularDescuentoGrupo(reserva.getCantidadpersonas(), precioinicial);
        float dctocumple = reservaService.descuentoporcumpleano(reserva.getCantidadpersonas(), precioinicial, reserva.getCantidadcumple());

        List<Float> descuentos = List.of(dctoespecial, dctogrupo, dctocumple);
        float descuentoAplicado = descuentos.stream().max(Float::compare).orElse(0f); // Mayor descuento
        float preciofinal = precioinicial - descuentoAplicado; // Precio con descuento
        float iva = preciofinal * 0.19f;
        float precio = preciofinal + iva;

        ComprobanteEntity comprobante = new ComprobanteEntity(dctocumple,dctoespecial,dctogrupo,idreserva,precioinicial,precio,tarifabase,iva);
        return comprobanteRepository.save(comprobante);
    }

    @Transactional
    public List<Object> reporteportiporeserva(String mesinicio, String mesfin) {
        // Validar que los meses no sean nulos
        if (mesinicio == null || mesfin == null) {
            throw new IllegalArgumentException("Los meses de inicio y fin no pueden ser nulos");
        }

        // Parsear los meses de inicio y fin (formato yyyy-MM)
        DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth inicio;
        YearMonth fin;
        try {
            inicio = YearMonth.parse(mesinicio, formatoMes);
            fin = YearMonth.parse(mesfin, formatoMes);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido, debe ser yyyy-MM");
        }

        // Verificar que el mes de inicio sea anterior o igual al mes de fin
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("El mes de inicio debe ser anterior o igual al mes de fin");
        }

        // Formato para parsear fechahora de las reservas (yyyy-MM-dd'T'HH:mm)
        DateTimeFormatter formatoReserva = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        // Obtener todas las reservas
        List<ReservaEntity> reservas = reservaRepository.findAll();

        // Filtrar reservas dentro del rango de meses
        List<ReservaEntity> reservasFiltradas = reservas.stream()
                .filter(reserva -> {
                    LocalDateTime fechaReserva = LocalDateTime.parse(reserva.getFechahora(), formatoReserva);
                    YearMonth mesReserva = YearMonth.from(fechaReserva);
                    return !mesReserva.isBefore(inicio) && !mesReserva.isAfter(fin);
                })
                .collect(Collectors.toList());

        // Calcular el número de meses en el rango (inclusive)
        int mesesRango = (fin.getYear() - inicio.getYear()) * 12 + fin.getMonthValue() - inicio.getMonthValue() + 1;

        // Crear la lista de resultados
        List<Object> reporte = new ArrayList<>(mesesRango);

        // Iterar sobre cada mes en el rango
        YearMonth mesActual = inicio;
        for (int i = 0; i < mesesRango; i++) {
            // Definir el inicio y fin del mes actual como LocalDateTime
            LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
            LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);

            // Filtrar reservas del mes actual
            List<ReservaEntity> reservasMes = reservasFiltradas.stream()
                    .filter(reserva -> {
                        LocalDateTime fechaReserva = LocalDateTime.parse(reserva.getFechahora(), formatoReserva);
                        return !fechaReserva.isBefore(inicioMes) && !fechaReserva.isAfter(finMes);
                    })
                    .collect(Collectors.toList());

            // Calcular totales por tipo de reserva usando el precio final pagado
            long totalTipo1 = reservasMes.stream()
                    .filter(r -> r.getTiporeserva() == 1)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();
            long totalTipo2 = reservasMes.stream()
                    .filter(r -> r.getTiporeserva() == 2)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();
            long totalTipo3 = reservasMes.stream()
                    .filter(r -> r.getTiporeserva() == 3)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();

            // Crear sublista con los totales
            List<Long> totalesMes = List.of(totalTipo1, totalTipo2, totalTipo3);
            reporte.add(totalesMes);

            // Avanzar al siguiente mes
            mesActual = mesActual.plusMonths(1);
        }

        return reporte;
    }

    @Transactional
    public List<Object> reporteporgrupo(String mesinicio,String mesfin){
        // Validar que los meses no sean nulos
        if (mesinicio == null || mesfin == null) {
            throw new IllegalArgumentException("Los meses de inicio y fin no pueden ser nulos");
        }

        // Parsear los meses de inicio y fin (formato yyyy-MM)
        DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth inicio;
        YearMonth fin;
        try {
            inicio = YearMonth.parse(mesinicio, formatoMes);
            fin = YearMonth.parse(mesfin, formatoMes);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha inválido, debe ser yyyy-MM");
        }

        // Verificar que el mes de inicio sea anterior o igual al mes de fin
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("El mes de inicio debe ser anterior o igual al mes de fin");
        }

        // Formato para parsear fechahora de las reservas (yyyy-MM-dd'T'HH:mm)
        DateTimeFormatter formatoReserva = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        // Obtener todas las reservas
        List<ReservaEntity> reservas = reservaRepository.findAll();

        // Filtrar reservas dentro del rango de meses
        List<ReservaEntity> reservasFiltradas = reservas.stream()
                .filter(reserva -> {
                    LocalDateTime fechaReserva = LocalDateTime.parse(reserva.getFechahora(), formatoReserva);
                    YearMonth mesReserva = YearMonth.from(fechaReserva);
                    return !mesReserva.isBefore(inicio) && !mesReserva.isAfter(fin);
                })
                .collect(Collectors.toList());

        // Calcular el número de meses en el rango (inclusive)
        int mesesRango = (fin.getYear() - inicio.getYear()) * 12 + fin.getMonthValue() - inicio.getMonthValue() + 1;

        // Crear la lista de resultados
        List<Object> reporte = new ArrayList<>(mesesRango);

        // Iterar sobre cada mes en el rango
        YearMonth mesActual = inicio;
        for (int i = 0; i < mesesRango; i++) {
            // Definir el inicio y fin del mes actual como LocalDateTime
            LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
            LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);

            // Filtrar reservas del mes actual
            List<ReservaEntity> reservasMes = reservasFiltradas.stream()
                    .filter(reserva -> {
                        LocalDateTime fechaReserva = LocalDateTime.parse(reserva.getFechahora(), formatoReserva);
                        return !fechaReserva.isBefore(inicioMes) && !fechaReserva.isAfter(finMes);
                    })
                    .collect(Collectors.toList());

            // Calcular totales por cantidad de personas: 1-2, 3-5, 6-10, 11-15
            long totalGrupo1_2 = reservasMes.stream()
                    .filter(r -> r.getCantidadpersonas() >= 1 && r.getCantidadpersonas() <= 2)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();
            long totalGrupo3_5 = reservasMes.stream()
                    .filter(r -> r.getCantidadpersonas() >= 3 && r.getCantidadpersonas() <= 5)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();
            long totalGrupo6_10 = reservasMes.stream()
                    .filter(r -> r.getCantidadpersonas() >= 6 && r.getCantidadpersonas() <= 10)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();
            long totalGrupo11_15 = reservasMes.stream()
                    .filter(r -> r.getCantidadpersonas() >= 11 && r.getCantidadpersonas() <= 15)
                    .mapToLong(r -> {
                        Optional<ComprobanteEntity> comprobante = comprobanteRepository.findByIdreserva(r.getId());
                        return comprobante.map(c -> (long) c.getPreciofinal()).orElse(0L);
                    })
                    .sum();

            // Crear sublista con los totales
            List<Long> totalesMes = List.of(totalGrupo1_2, totalGrupo3_5, totalGrupo6_10, totalGrupo11_15);
            reporte.add(totalesMes);

            // Avanzar al siguiente mes
            mesActual = mesActual.plusMonths(1);
        }

        return reporte;
    }
}