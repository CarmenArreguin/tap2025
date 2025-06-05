package com.example.tap2025.modelos;

public class Reservacion {
    private int idReservacion;
    private int idCliente;
    private String nombreCliente;
    private int personas;
    private String fecha;
    private String hora;
    private int mesa;

    public Reservacion(int idReservacion, int idCliente, String nombreCliente, int personas, String fecha, String hora, int mesa) {
        this.idReservacion = idReservacion;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.personas = personas;
        this.fecha = fecha;
        this.hora = hora;
        this.mesa = mesa;
    }

    public int getIdReservacion() {
        return idReservacion;
    }
    public void setIdReservacion(int idReservacion) {
        this.idReservacion = idReservacion;
    }

    public int getIdCliente() {
        return idCliente;
    }
    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public int getPersonas() {
        return personas;
    }
    public void setPersonas(int personas) {
        this.personas = personas;
    }

    public String getFecha() {
        return fecha;
    }
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }
    public void setHora(String hora) {
        this.hora = hora;
    }

    public int getMesa() {
        return mesa;
    }
    public void setMesa(int mesa) {
        this.mesa = mesa;
    }
}
