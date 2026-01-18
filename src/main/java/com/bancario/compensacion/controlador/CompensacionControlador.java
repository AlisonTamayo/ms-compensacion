package com.bancario.compensacion.controlador;

import com.bancario.compensacion.dto.ArchivoDTO;
import com.bancario.compensacion.dto.CicloDTO;
import com.bancario.compensacion.dto.PosicionDTO;
import com.bancario.compensacion.servicio.CompensacionServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/compensacion")
@RequiredArgsConstructor
@Tag(name = "Microservicio de Compensación (G4)", description = "Gestión de Clearing, Settlement y Continuidad")
public class CompensacionControlador {

    private final CompensacionServicio service;

    @GetMapping("/ciclos")
    @Operation(summary = "Listar ciclos", description = "Obtiene el historial de todos los ciclos operativos.")
    public ResponseEntity<List<CicloDTO>> listarCiclos() {
        return ResponseEntity.ok(service.listarCiclos());
    }

    @GetMapping("/ciclos/{cicloId}/posiciones")
    @Operation(summary = "Obtener detalle de posiciones", description = "Ver acumulados netos por banco")
    public ResponseEntity<List<PosicionDTO>> obtenerPosiciones(@PathVariable Integer cicloId) {
        return ResponseEntity.ok(service.obtenerPosicionesCiclo(cicloId));
    }

    @PostMapping("/ciclos/{cicloId}/acumular")
    @Operation(summary = "INTERNAL: Acumular movimiento (Deprecated)", description = "Use el endpoint sin ID para autodetectar ciclo.")
    public ResponseEntity<Void> acumular(
            @PathVariable Integer cicloId,
            @RequestParam String bic,
            @RequestParam BigDecimal monto,
            @RequestParam boolean esDebito) {

        service.acumularTransaccion(cicloId, bic, monto, esDebito);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/acumular")
    @Operation(summary = "INTERNAL: Acumular movimiento (Auto-Ciclo)", description = "Registra débitos/créditos en el ciclo ABIERTO actual.")
    public ResponseEntity<Void> acumularAuto(
            @RequestParam String bic,
            @RequestParam BigDecimal monto,
            @RequestParam boolean esDebito) {

        service.acumularEnCicloAbierto(bic, monto, esDebito);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ciclos/{cicloId}/cierre")
    @Operation(summary = "EJECUTAR CIERRE DIARIO (Settlement)", description = "1. Valida Suma Cero. 2. Genera XML. 3. Firma Digital (JWS). 4. Cierra el ciclo actual. 5. Abre el siguiente ciclo arrastrando saldos (Continuidad).")
    public ResponseEntity<ArchivoDTO> cerrarCiclo(@PathVariable Integer cicloId) {
        return ResponseEntity.ok(service.realizarCierreDiario(cicloId));
    }
}
