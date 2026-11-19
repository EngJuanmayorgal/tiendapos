package com.tiendapos.api.service;

import com.tiendapos.api.model.VentaRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class VentaService {

    private final JdbcTemplate jdbc;

    public VentaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public Map<String, Object> registrarVenta(VentaRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("RN-10: La venta debe contener al menos un producto.");
        }
        if (req.getPago() == null) {
            throw new IllegalArgumentException("RN-15: Toda venta completada requiere un pago.");
        }

        // 1. Insertar cabecera de venta con total 0
        Integer idVenta = jdbc.queryForObject(
                "INSERT INTO venta (id_empleado, total, estado) VALUES (?, 0, 'completada') RETURNING id_venta",
                Integer.class,
                req.getIdEmpleado()
        );

        // 2. Insertar cada línea (si falla la línea 2 por stock/inactivo, hace ROLLBACK de todo)
        for (VentaRequest.ItemVenta item : req.getItems()) {
            BigDecimal precioAplicar = obtenerPrecioVigente(item.getIdProducto());

            jdbc.update(
                    "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)",
                    idVenta, item.getIdProducto(), item.getCantidad(), precioAplicar
            );
        }

        // 3. Calcular y actualizar el total de la venta (RN-13)
        BigDecimal totalVenta = jdbc.queryForObject(
                "SELECT COALESCE(SUM(subtotal), 0) FROM detalle_venta WHERE id_venta = ?",
                BigDecimal.class,
                idVenta
        );
        jdbc.update("UPDATE venta SET total = ? WHERE id_venta = ?", totalVenta, idVenta);

        // 4. Registrar el pago (RN-15 a RN-18)
        VentaRequest.PagoRequest p = req.getPago();
        BigDecimal cambio = null;
        if ("efectivo".equalsIgnoreCase(p.getMetodo()) && p.getValorRecibido() != null) {
            cambio = p.getValorRecibido().subtract(totalVenta);
        }

        jdbc.update("""
                INSERT INTO pago (id_venta, metodo, monto, valor_recibido, cambio, tipo_tarjeta, numero_aprobacion, referencia_bre_b)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                idVenta, p.getMetodo(), totalVenta, p.getValorRecibido(),
                cambio, p.getTipoTarjeta(), p.getNumeroAprobacion(), p.getReferenciaBreB()
        );

        return Map.of(
                "mensaje", "Venta registrada con éxito",
                "id_venta", idVenta,
                "total", totalVenta,
                "cambio", cambio != null ? cambio : BigDecimal.ZERO
        );
    }

    // RF-20: Busca precio promocional vigente hoy; si no hay, usa precio_venta normal
    private BigDecimal obtenerPrecioVigente(Integer idProducto) {
        List<BigDecimal> promo = jdbc.queryForList("""
                SELECT pp.precio_promocional
                FROM producto_promocion pp
                JOIN promocion pr ON pr.id_promocion = pp.id_promocion
                WHERE pp.id_producto = ?
                  AND CURRENT_DATE BETWEEN pr.fecha_inicio AND pr.fecha_fin
                """, BigDecimal.class, idProducto);

        if (!promo.isEmpty()) {
            return promo.get(0);
        }

        return jdbc.queryForObject(
                "SELECT precio_venta FROM producto WHERE id_producto = ?",
                BigDecimal.class,
                idProducto
        );
    }
}