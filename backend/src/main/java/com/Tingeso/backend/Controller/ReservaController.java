package com.Tingeso.backend.Controller;

import com.Tingeso.backend.DTO.DescuentoCumpleDTO;
import com.Tingeso.backend.DTO.DescuentoEspecialDTO;
import com.Tingeso.backend.DTO.DescuentoGrupoDTO;
import com.Tingeso.backend.Entity.ReservaEntity;
import com.Tingeso.backend.Service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/reservas")
public class ReservaController {
    @Autowired
    public ReservaService reservaService;

    // Controller CrearReserva
    @PostMapping("/crear")
    public ReservaEntity crearReserva(@RequestBody ReservaEntity reserva) {
        String fechahora = reserva.getFechahora();
        int tiporeserva = reserva.getTiporeserva();
        int cantidadpersonas = reserva.getCantidadpersonas();
        Long iduser = reserva.getIduser();
        int cantidadcumple = reserva.getCantidadcumple();
        return reservaService.crearReserva(fechahora, tiporeserva, cantidadpersonas, iduser, cantidadcumple);
    }
    // Controller ObtenerReserva
    @GetMapping("/obtenerReservas/{id}")
    public List<ReservaEntity> obtenerReservas(@PathVariable Long iduser) {
        return reservaService.obtenerReservasUsuario(iduser);
    }

    // Controller calcularprecioinicial
    @GetMapping("/calcularprecioinicial/{id}")
    public float calcularPrecioInicial(@PathVariable Long idreserva) {
        return reservaService.calcularprecioinicial(idreserva);
    }

    @GetMapping("/descuentogrupo/{id}")
    public float calcularDescuentoGrupo(@RequestBody DescuentoGrupoDTO descuentoGrupoDTO) {
        return reservaService.calcularDescuentoGrupo(descuentoGrupoDTO.getCantidadpersonas(), descuentoGrupoDTO.getPrecioinicial());
    }

    @GetMapping("/descuentoespecial")
    public float calcularDescuentoEspecial(@RequestBody DescuentoEspecialDTO descuentoEspecialDTO) {
        return reservaService.calcularDescuentoEspecial(descuentoEspecialDTO.getIduser(), descuentoEspecialDTO.getPrecioinicial());
    }

    @GetMapping("/descuentocumple")
    public float descuentoporcumpleano(@RequestBody DescuentoCumpleDTO descuentoCumpleDTO){
        return reservaService.descuentoporcumpleano(descuentoCumpleDTO.getCantidadpersonas(), descuentoCumpleDTO.getPrecioinicial(), descuentoCumpleDTO.getCantidadcumple());
    }
}
