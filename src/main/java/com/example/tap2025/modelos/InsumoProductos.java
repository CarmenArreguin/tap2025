package com.example.tap2025.modelos;

public class InsumoProductos {
    private String nombreInsumo;
    private double cantidad;
    private String unidadMedida;

    public InsumoProductos(String nombreInsumo, double cantidad, String unidadMedida) {
        this.nombreInsumo = nombreInsumo;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
    }

    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }
}
