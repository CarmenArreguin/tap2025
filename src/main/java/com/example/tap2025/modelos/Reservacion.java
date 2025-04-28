package com.example.tap2025.modelos;

public class Reservacion {
    private int id;
    private String nombreCliente;
    private int personas;
    private String fecha;
    private String hora;
    private int mesa;

    public Reservacion(int id, String nombreCliente, int personas, String fecha, String hora, int mesa) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.personas = personas;
        this.fecha = fecha;
        this.hora = hora;
        this.mesa = mesa;
    }

    public int getId() {
        return id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public int getPersonas() {
        return personas;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public int getMesa() {
        return mesa;
    }
}
