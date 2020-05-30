package com.ojooculto;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class CameraService extends Service
{

    private static onCameraService LISTENER;

    protected static final String TAG = "myLog";
    protected static final int CAMERACHOICE = CameraCharacteristics.LENS_FACING_BACK;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession session;
    protected ImageReader imageReader;

    private ArrayList<String> direccioneFotos;
    private int contadorFotos;

    private boolean isClose;

    //TODO Interfaz de comunicacion
    public interface onCameraService {
        void onConnectionFailed(@NonNull ConnectionResult connectionResult);

        void getImages(ArrayList<String> direcciones);
    }

    public static void setListener(onCameraService listener) {
        LISTENER = listener;
    }

    //TODO Listener personalizado que se llama cuando la camara se habre
    protected CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e(TAG, "CameraDevice.StateCallback onOpened");
            cameraDevice = camera;
            actOnReadyCameraDevice();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.e(TAG, "CameraDevice.StateCallback onDisconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "CameraDevice.StateCallback onError " + error);
        }
    };

    //TODO Listener persoanlizado que controla la sesion de la camara ¿dejo de jalar? *******
    protected CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onReady(CameraCaptureSession session) {
            CameraService.this.session = session;
            if (!isClose) {
                try {
                    session.setRepeatingRequest(createCaptureRequest(), null, null);
                } catch (CameraAccessException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }


        @Override
        public void onConfigured(CameraCaptureSession session) {

        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };

    //TODO Listener personalizdao que controla cuando la camara detecta una foto
    protected ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.e(TAG, "onImageAvailable");
            Image img = reader.acquireLatestImage();
            if (img != null) {
                processImage(img);
                img.close();
            }
        }
    };

    public void readyCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String pickedCamera = getCamera(manager);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(pickedCamera, cameraStateCallback, null);
            imageReader = ImageReader.newInstance(1920, 1088, ImageFormat.JPEG, 2 /* images buffered */);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
            Log.d(TAG, "imageReader created");
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }

    public String getCamera(CameraManager manager){
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation != CAMERACHOICE) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand flags " + flags + " startId " + startId);

        readyCamera();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.e(TAG,"onCreate service");
        contadorFotos = 0;
        isClose = false;
        direccioneFotos = new ArrayList<>();
        super.onCreate();
    }

    public void actOnReadyCameraDevice()
    {
        try {
            cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), sessionStateCallback, null);
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        if (!isClose) {
            try {
                session.abortCaptures();
            } catch (CameraAccessException e){
                Log.e(TAG, e.getMessage());
            }
            session.close();
        }
        //TODO envia el mensaje, igual ya funciona no? para mañana pagarlo y calarlo
        //TODO no estoy seguro del metodo este
    }


    private void processImage(Image image){
        //Process image data
        ByteBuffer buffer;
        byte[] bytes;
        boolean success = false;
        String id = UUID.randomUUID().toString();
        File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/"+id+".jpg");
        FileOutputStream output = null;

        if(image.getFormat() == ImageFormat.JPEG) {
            buffer = image.getPlanes()[0].getBuffer();
            bytes = new byte[buffer.remaining()]; // makes byte array large enough to hold image
            buffer.get(bytes); // copies image from buffer to byte array
            try {
                output = new FileOutputStream(file);
                output.write(bytes);
                contadorFotos++;
                direccioneFotos.add(file.getAbsolutePath());
                if (contadorFotos == 10) {
                    stopSelf();
                    LISTENER.getImages(direccioneFotos);
                    try {
                        session.abortCaptures();
                    } catch (CameraAccessException e){
                        Log.e(TAG, e.getMessage());
                    }
                    session.close();
                    isClose = true;
                    for (String foto : direccioneFotos) {
                        Log.e("Imagen",foto);
                    }
                    direccioneFotos.clear();
                    //TODO aqui iria la carga al servidor
                    //TODO tardara como 2m en tomar todas, despues de eso se detendra
                    //TODO me aparecio el mensaje de sms enviado satisfactoriamente
                    //Todo envia el mensaje
                } else {
                    Log.e("INFO","Tomando la foto " + contadorFotos);
                }
                Thread.sleep(12000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                image.close(); // close this to free up buffer for other images
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }


    }

    protected CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            builder.addTarget(imageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}