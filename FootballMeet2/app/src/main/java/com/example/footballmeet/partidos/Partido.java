package com.example.footballmeet.partidos;

public class Partido {
    private String partidoId; // Agregar este campo si aún no lo tienes
    private String userId; // Agregar este campo si aún no lo tienes
    private String fecha;
    private String hora;
    private String descripcion;
    private int capacidad;
    private double precio;
    private String imagenUrl = null; // Nuevo campo para almacenar la URL de la imagen

    private String ubicacion = null; // Agrega el campo de ubicación

    public Partido() {
        // Constructor vacío requerido por Firebase Realtime Database
    }

    public Partido(String partidoId, String userId, String fecha, String hora, String descripcion, int capacidad, double precio, String imagenUrl, String ubicacion) {
        this.partidoId = partidoId;
        this.userId = userId;
        this.fecha = fecha;
        this.hora = hora;
        this.descripcion = descripcion;
        this.capacidad = capacidad;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.ubicacion = ubicacion;
    }
    public String getPartidoId() {
        return partidoId;
    }

    public void setPartidoId(String partidoId) {
        this.partidoId = partidoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
}


