package com.tiendapos.api.model;

import java.math.BigDecimal;
import java.util.List;

public class VentaRequest {
    private Integer idEmpleado;
    private List<ItemVenta> items;
    private PagoRequest pago;

    public Integer getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Integer idEmpleado) { this.idEmpleado = idEmpleado; }
    public List<ItemVenta> getItems() { return items; }
    public void setItems(List<ItemVenta> items) { this.items = items; }
    public PagoRequest getPago() { return pago; }
    public void setPago(PagoRequest pago) { this.pago = pago; }

    public static class ItemVenta {
        private Integer idProducto;
        private BigDecimal cantidad;

        public Integer getIdProducto() { return idProducto; }
        public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }
        public BigDecimal getCantidad() { return cantidad; }
        public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
    }

    public static class PagoRequest {
        private String metodo; // efectivo, tarjeta, transferencia_bre_b
        private BigDecimal valorRecibido;
        private String tipoTarjeta; // debito, credito
        private String numeroAprobacion;
        private String referenciaBreB;

        public String getMetodo() { return metodo; }
        public void setMetodo(String metodo) { this.metodo = metodo; }
        public BigDecimal getValorRecibido() { return valorRecibido; }
        public void setValorRecibido(BigDecimal valorRecibido) { this.valorRecibido = valorRecibido; }
        public String getTipoTarjeta() { return tipoTarjeta; }
        public void setTipoTarjeta(String tipoTarjeta) { this.tipoTarjeta = tipoTarjeta; }
        public String getNumeroAprobacion() { return numeroAprobacion; }
        public void setNumeroAprobacion(String numeroAprobacion) { this.numeroAprobacion = numeroAprobacion; }
        public String getReferenciaBreB() { return referenciaBreB; }
        public void setReferenciaBreB(String referenciaBreB) { this.referenciaBreB = referenciaBreB; }
    }
}