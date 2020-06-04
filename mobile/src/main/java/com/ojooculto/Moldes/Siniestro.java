package com.ojooculto.Moldes;

public class Siniestro {

    private String id;
    private Long time;
    private int imagenes;
    private boolean audio;
    private boolean ubicacion;
    private double lat;
    private double lon;

    public Siniestro() {
    }

    public Siniestro(String id, Long time, int imagenes, boolean audio, boolean ubicacion, double lat, double lon) {
        this.id = id;
        this.time = time;
        this.imagenes = imagenes;
        this.audio = audio;
        this.ubicacion = ubicacion;
        this.lat = lat;
        this.lon = lon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public int getImagenes() {
        return imagenes;
    }

    public void setImagenes(int imagenes) {
        this.imagenes = imagenes;
    }

    public boolean isAudio() {
        return audio;
    }

    public void setAudio(boolean audio) {
        this.audio = audio;
    }

    public boolean isUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(boolean ubicacion) {
        this.ubicacion = ubicacion;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "Siniestro{" +
                "id='" + id + '\'' +
                ", time=" + time +
                ", imagenes=" + imagenes +
                ", audio=" + audio +
                ", ubicacion=" + ubicacion +
                ", lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}
