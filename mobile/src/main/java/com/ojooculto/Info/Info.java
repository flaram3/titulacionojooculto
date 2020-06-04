package com.ojooculto.Info;

import android.content.Context;

public class Info {

    public static void Ingreasar(Context context, String cuenta) {
        context.getSharedPreferences("cuentas", Context.MODE_PRIVATE).edit().putString("idCuenta",cuenta).apply();
    }

    public static boolean ValidaUsuarioActivo(Context context) {
        return context.getSharedPreferences("cuentas", Context.MODE_PRIVATE).getString("idCuenta",null) != null;
    }

    public static void Eliminar(Context context) {
        context.getSharedPreferences("cuentas",Context.MODE_PRIVATE).edit().clear().apply();
    }

    public static String Obtener(Context context) {
        return context.getSharedPreferences("cuentas",Context.MODE_PRIVATE).getString("idCuenta",null);
    }

}
