package com.ojooculto.ojooculto;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**Extender WearableListenerService**/
public class MessageService extends WearableListenerService {

@Override
public void onMessageReceived(MessageEvent messageEvent) {

/**Si la ruta del mensaje es igual a "/my_path"...**/
if (messageEvent.getPath().equals("/my_path2"))
{
        /**... recupera el mensaje**/
    final String message = new String(messageEvent.getData());
    Intent messageIntent = new Intent();
    messageIntent.setAction(Intent.ACTION_SEND);
    messageIntent.putExtra("message", message);

    /**Transmitir los mensajes recibidos de la capa de datos localmente**/
    LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
}
else
{
    super.onMessageReceived(messageEvent);
}
}
}
