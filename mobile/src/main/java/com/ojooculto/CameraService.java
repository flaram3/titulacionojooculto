package com.ojooculto;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.ojooculto.Moldes.Usuario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CameraService extends Service {

    protected static final String TAG = "myLog";
    protected static final int CAMERACHOICE = CameraCharacteristics.LENS_FACING_BACK;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession session;
    protected ImageReader imageReader;

    private ArrayList<String> direccioneFotos;
    private int contadorFotos;
    private int contadorUbicacion;

    private LocationManager locationManager;

    private boolean isClose;

    private String idRef;

    public interface CameraServiceListener {
        void onLocationListener(String ref);
    }

    public static CameraServiceListener LISTENER;

    public static void setCameraServiceListener(CameraServiceListener L) {
        LISTENER = L;
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
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public String getCamera(CameraManager manager) {
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation != CAMERACHOICE) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
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
        Log.e(TAG, "onCreate service");
        contadorFotos = 0;
        isClose = false;
        direccioneFotos = new ArrayList<>();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        super.onCreate();
    }

    public void actOnReadyCameraDevice() {
        try {
            cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), sessionStateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        if (!isClose) {
            try {
                session.abortCaptures();
            } catch (CameraAccessException e) {
                Log.e(TAG, e.getMessage());
            }
            session.close();
        }
        //TODO envia el mensaje, igual ya funciona no? para mañana pagarlo y calarlo
        //TODO no estoy seguro del metodo este
    }


    private void processImage(Image image) {
        //Process image data
        ByteBuffer buffer;
        byte[] bytes;
        boolean success = false;
        String id = UUID.randomUUID().toString();
        File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/" + id + ".jpg");
        FileOutputStream output = null;

        if (image.getFormat() == ImageFormat.JPEG) {
            buffer = image.getPlanes()[0].getBuffer();
            bytes = new byte[buffer.remaining()]; // makes byte array large enough to hold image
            buffer.get(bytes); // copies image from buffer to byte array
            try {
                output = new FileOutputStream(file);
                output.write(bytes);
                contadorFotos++;
                direccioneFotos.add(file.getAbsolutePath());
                if (contadorFotos == 9) {
                    isClose = true;
                    stopSelf();
                    try {
                        session.abortCaptures();
                    } catch (CameraAccessException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    session.close();
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        final String ref = FirebaseDatabase.getInstance().getReference("Siniestros")
                                .child(user.getUid()).push().getKey();
                        FirebaseDatabase.getInstance().getReference("Usuarios")
                                .child(user.getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        idRef = ref;
                                        hashMap.put("id", ref);
                                        hashMap.put("time", ServerValue.TIMESTAMP);
                                        hashMap.put("imagenes", contadorFotos);
                                        hashMap.put("audio", false);
                                        hashMap.put("ubicacion",false);
                                        FirebaseDatabase.getInstance().getReference("Siniestros")
                                                .child(user.getUid())
                                                .child(ref)
                                                .updateChildren(hashMap);
                                        for (String foto : direccioneFotos) {
                                            Uri uri = Uri.fromFile(new File(foto));
                                            if (uri != null) {
                                                final StorageReference storageReference = FirebaseStorage.getInstance().getReference("Siniestros")
                                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                        .child(ref)
                                                        .child(System.currentTimeMillis() + "." + obtnerExtension(uri));
                                                StorageTask task = storageReference.putFile(uri);
                                                task.continueWithTask(new Continuation() {
                                                    @Override
                                                    public Object then(@NonNull Task task) throws Exception {
                                                        if (!task.isComplete()) {
                                                            throw task.getException();
                                                        }
                                                        return storageReference.getDownloadUrl();
                                                    }
                                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Uri> task) {
                                                        if (task.isSuccessful()) {
                                                            Uri tmpUri = task.getResult();
                                                            assert tmpUri != null;
                                                            String url = tmpUri.toString();

                                                            HashMap<String, Object> hashMap = new HashMap<>();
                                                            hashMap.put("img", url);

                                                            FirebaseDatabase.getInstance().getReference("AuxSiniestro")
                                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .child(ref)
                                                                    .push()
                                                                    .updateChildren(hashMap);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                        if (usuario.isConexion()) {
                                            if (LISTENER != null) {
                                                LISTENER.onLocationListener(ref);
                                            }
                                        } else {
                                            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                            if (gpsEnabled) {
                                                if (ActivityCompat.checkSelfPermission(CameraService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CameraService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                    return;
                                                }
                                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
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

    //TODO OBJETO QUE MANEJA LA UBICACION
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (idRef != null) {
                if (contadorUbicacion == 5) {
                    locationManager.removeUpdates(locationListener);
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("lat",location.getLatitude());
                    hashMap.put("lon",location.getLongitude());
                    hashMap.put("ubicacion",true);
                    FirebaseDatabase.getInstance().getReference("Siniestros")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(idRef)
                            .updateChildren(hashMap);
                }
                contadorUbicacion++;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }

    };


    private String obtnerExtension(Uri uri) {
        return MimeTypeMap.getFileExtensionFromUrl(uri.toString());
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