package com.ojooculto.Info;

import android.util.Base64;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Encriptar {

    public static SecretKey generarKey() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = "ojooculto".getBytes("UTF-8");
        key = sha.digest(key);
        return new SecretKeySpec(key, "AES");
    }

    public static String desencriptar(String datos) throws Exception
    {
        SecretKey secretKey = generarKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE,secretKey);
        byte[] datosDecodificados = Base64.decode(datos, Base64.DEFAULT);
        byte[] datosDecodificadosByte = cipher.doFinal(datosDecodificados);
        return new String(datosDecodificadosByte);
    }

    public static String encriptar(String datos) throws Exception {
        SecretKey secretKey = generarKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE,secretKey);
        byte[] datosEncriptadosBytes = cipher.doFinal(datos.getBytes());
        return Base64.encodeToString(datosEncriptadosBytes, Base64.DEFAULT);
    }

}
