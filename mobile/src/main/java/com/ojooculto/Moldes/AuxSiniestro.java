package com.ojooculto.Moldes;

public class AuxSiniestro {

    private String img;

    public AuxSiniestro() {
    }

    public AuxSiniestro(String img) {
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return "AuxSiniestro{" +
                "img='" + img + '\'' +
                '}';
    }
}
