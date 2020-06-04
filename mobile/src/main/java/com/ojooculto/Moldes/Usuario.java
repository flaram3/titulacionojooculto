package com.ojooculto.Moldes;

public class Usuario {

    private String id;
    private String fName;
    private String email;
    private String phone;
    private String img;
    private int code;
    private String password;
    private boolean conexion;
    private long time;
    private String numeroAlerta;

    public Usuario() {
    }

    public Usuario(String id, String fName, String email, String phone, String img, int code, String password, boolean conexion, long time, String numeroAlerta) {
        this.id = id;
        this.fName = fName;
        this.email = email;
        this.phone = phone;
        this.img = img;
        this.code = code;
        this.password = password;
        this.conexion = conexion;
        this.time = time;
        this.numeroAlerta = numeroAlerta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isConexion() {
        return conexion;
    }

    public void setConexion(boolean conexion) {
        this.conexion = conexion;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getNumeroAlerta() {
        return numeroAlerta;
    }

    public void setNumeroAlerta(String numeroAlerta) {
        this.numeroAlerta = numeroAlerta;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", fName='" + fName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", img='" + img + '\'' +
                ", code=" + code +
                ", password='" + password + '\'' +
                ", conexion=" + conexion +
                ", time=" + time +
                ", numeroAlerta='" + numeroAlerta + '\'' +
                '}';
    }
}
