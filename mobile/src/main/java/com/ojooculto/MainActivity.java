package com.ojooculto;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
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
import com.ojooculto.BottomSheets.EditarPerfilBottomSheet;
import com.ojooculto.Dialogos.DialogVerDireccion;
import com.ojooculto.Dialogos.DialogoCode;
import com.ojooculto.Dialogos.MessageDialog;
import com.ojooculto.Dialogos.MessageDialogBuilder;
import com.ojooculto.Moldes.Usuario;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements CameraService.CameraServiceListener {

    //TODO CONTADOR DE INTENTOS DEL BOTON FISICO
    private int contador;
    private boolean isService;
    private String idRef;
    private int contadorUbicacion;

    //TODO PERFIL
    private TextView txtNumeroAlerta;
    private TextView txtNumeroUsuario;
    private TextView txtCorreo;
    private TextView txtNombre;
    private CircleImageView imgPerfil;
    private TextView txtCorreoVerificado;
    private ImageView imgErrorCorreo;

    //TODO USUARIO DE FIREBASE
    private FirebaseUser firebaseUser;

    //TODO UBICACION
    private LinearLayout lytUbicacion;
    private MaterialCardView cardError;
    private TextView txtCoordenadas;
    private TextView txtDireccion;
    private LocationManager locationManager;
    private Location location;
    private MaterialCardView cardErrorGPS;
    private LinearLayout lytGPS;

    //TODO PERMISOS
    private final String[] PERMISOS_UBICACION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private final String[] PERMISOS_IMAGEN = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private final int CODIGO_PERMISOS_UBICACION = 101;
    private final int CODIGO_PERMISOS_IMAGEN = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO CONEXION CON EL SERVICIO
        CameraService.setCameraServiceListener(MainActivity.this);

        //TODO PERFIL
        txtNumeroUsuario = findViewById(R.id.txtNumeroUsuario);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtNumeroAlerta = findViewById(R.id.txtNumeroAlerta);
        txtNombre = findViewById(R.id.txtNombre);
        imgPerfil = findViewById(R.id.imgPerfil);
        txtCorreoVerificado = findViewById(R.id.txtCorreoVerificado);
        imgErrorCorreo = findViewById(R.id.icon3);

        //TODO USUARIO DE FIREBASE
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //TODO UBICACION
        txtCoordenadas = findViewById(R.id.txtCoordenadas);
        txtDireccion = findViewById(R.id.txtDireccion);
        lytUbicacion = findViewById(R.id.lytUbicacion);
        cardError = findViewById(R.id.card_error);
        cardErrorGPS = findViewById(R.id.card_error_gps);
        lytGPS = findViewById(R.id.lytGPS);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //TODO Referencia para obtener la informacion del usuario
        FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return;
                        }
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        txtNombre.setText(usuario.getfName());
                        txtCorreo.setText(usuario.getEmail());
                        txtNumeroUsuario.setText(usuario.getPhone());
                        txtNumeroAlerta.setText(usuario.getNumeroAlerta());
                        if (usuario.getImg().equals("default")) {
                            imgPerfil.setImageResource(R.drawable.ic_person_black_24dp);
                        } else {
                            Glide.with(getApplicationContext())
                                    .load(usuario.getImg())
                                    .into(imgPerfil);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //TODO PROCESO PARA ACTIVAR LA UBICACION
        if (verificarPermisos()) {
            proceso();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISOS_UBICACION, CODIGO_PERMISOS_UBICACION);
        }

        //TODO Boton para cerrar sesion
        ((ImageButton) findViewById(R.id.btnCerrarSesion))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final MessageDialog dialog = new MessageDialog(MainActivity.this, new MessageDialogBuilder()
                                .setTitle("Alerta")
                                .setMessage("¿Esta seguro que desea cerrar sesión?")
                                .setPositiveButtonText("Aceptar")
                                .setNegativeButtonText("Cancelar")
                        );
                        dialog.show();
                        dialog.setPositiveButtonListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FirebaseAuth.getInstance().signOut();
                                dialog.dismiss();
                                startActivity(new Intent(MainActivity.this, Register.class));
                                finish();
                            }
                        });
                        dialog.setNegativeButtonListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });

        //TODO Boton para editar el perfil
        ((ImageButton) findViewById(R.id.btnEditarPerfil))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditarPerfilBottomSheet bottomSheet = new EditarPerfilBottomSheet(MainActivity.this);
                        bottomSheet.show(getSupportFragmentManager(),bottomSheet.getTag());
                    }
                });

        //TODO INVOCA UNA PANTALLA PARA ACTIVAR PERMISOS
        ((TextView) findViewById(R.id.txtPermisos))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = new Uri.Builder()
                                .scheme("package")
                                .opaquePart(getPackageName())
                                .build();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                        startActivity(intent);
                    }
                });

        //TODO INVOCA UN DIALOGO CON UN MAPA DE UBICACION
        ((ImageButton) findViewById(R.id.btnVerEnMapa))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (location != null) {
                            DialogVerDireccion dialogVerDireccion = new DialogVerDireccion(location);
                            dialogVerDireccion.show(getSupportFragmentManager(),dialogVerDireccion.getTag());
                        }
                    }
                });

        //TODO PROCESO PARA MOSTRAR LOS SINIESTROS
        ((MaterialButton) findViewById(R.id.btnSiniestros))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogoCode dialogoCode = new DialogoCode();
                        dialogoCode.show(getSupportFragmentManager(),dialogoCode.getTag());
                    }
                });

        //TODO VERIFICA EL CORREO
        if(!firebaseUser.isEmailVerified()){
            txtCorreoVerificado.setVisibility(View.VISIBLE);
            imgErrorCorreo.setImageResource(R.drawable.ic_baseline_email_error);

            ((ImageButton) findViewById(R.id.btnVerificarCorreo))
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final MessageDialog dialog = new MessageDialog(MainActivity.this, new MessageDialogBuilder()
                                    .setTitle("Alerta")
                                    .setMessage("Para verificar es necesario enviar un correo, ¿Desea enviarlo ahora?")
                                    .setPositiveButtonText("Enviar")
                                    .setNegativeButtonText("Más tarde")
                            );
                            dialog.show();
                            dialog.setPositiveButtonListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MainActivity.this, "El correo de verificacion ha sido enviado.", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("tag", "El correo de verificacion no se envio" + e.getMessage());
                                        }
                                    });
                                }
                            });
                            dialog.setNegativeButtonListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
        } else {
            txtCorreoVerificado.setVisibility(View.GONE);
            imgErrorCorreo.setImageResource(R.drawable.ic_email_black_24dp);
            ((ImageButton) findViewById(R.id.btnVerificarCorreo))
                    .setVisibility(View.GONE);
        }

        /*

        /**                 Reloj               **/
        /**Regístrese para recibir transmisiones locales, que crearemos en el siguiente paso**/
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

    }

    //TODO VERIFICA LOS PERMISOS UBICACION
    private boolean verificarPermisos() {
        for (String permiso : PERMISOS_UBICACION) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permiso) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //TODO VERIFICA LOS PERMISOS UBICACION
    private boolean verificarPermisosImagen() {
        for (String permiso : PERMISOS_IMAGEN) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permiso) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //TODO DESPUES DE PEDIR LOS PERMISOS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISOS_UBICACION) {
            boolean ban = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    ban = false;
                    break;
                }
            }

            if (ban) {
                proceso();
            } else {
                cardError.setVisibility(View.VISIBLE);
                lytUbicacion.setVisibility(View.GONE);
                Snackbar.make(findViewById(R.id.lytMain), "Error al activar los permisos", Snackbar.LENGTH_LONG)
                        .setAction("Activar", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Uri uri = new Uri.Builder()
                                        .scheme("package")
                                        .opaquePart(getPackageName())
                                        .build();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        } else if (requestCode == CODIGO_PERMISOS_IMAGEN) {
            boolean ban = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    ban = false;
                    break;
                }
            }

            if (ban) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .start(MainActivity.this);
            } else {
                lytUbicacion.setVisibility(View.GONE);
                Snackbar.make(findViewById(R.id.lytMain), "Error al activar los permisos", Snackbar.LENGTH_LONG)
                        .setAction("Activar", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Uri uri = new Uri.Builder()
                                        .scheme("package")
                                        .opaquePart(getPackageName())
                                        .build();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    //TODO LO QUE SUCEDE DESPUES DE QUE LOS PERMISOS SE HAN ACEPTADOR
    private void proceso() {
        cardError.setVisibility(View.GONE);
        lytUbicacion.setVisibility(View.VISIBLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            lytGPS.setVisibility(View.GONE);
            cardErrorGPS.setVisibility(View.VISIBLE);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }

    //TODO CICLO DE VIDA DE UN ACTIVITY
    @Override
    protected void onResume() {
        super.onResume();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("time", ServerValue.TIMESTAMP);
        hashMap.put("conexion", true);
        if (firebaseUser != null) {
            FirebaseDatabase.getInstance().getReference("Usuarios")
                    .child(firebaseUser.getUid())
                    .updateChildren(hashMap);
        }
    }

    //TODO CICLO DE VIDA DE UN ACTIVITY
    @Override
    protected void onPause() {
        super.onPause();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("time", ServerValue.TIMESTAMP);
        hashMap.put("conexion", false);
        if (firebaseUser != null) {
            FirebaseDatabase.getInstance().getReference("Usuarios")
                    .child(firebaseUser.getUid())
                    .updateChildren(hashMap);
        }
    }

    //TODO CICLO DE VIDA DE UN ACTIVITY
    @Override
    protected void onRestart() {
        super.onRestart();
        if(verificarPermisos()) {
            proceso();
        }
    }

    //TODO CICLO DE VIDA DE UN ACTIVITY
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    //TODO OBJETO QUE MANEJA LA UBICACION
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            MainActivity.this.location = location;
            String text = "Lat = "
                    + location.getLatitude() + "\nLong = " + location.getLongitude();
            txtCoordenadas.setText(text);
            if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
                try {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!list.isEmpty()) {
                        Address dirCalle = list.get(0);
                        txtDireccion.setText(dirCalle.getAddressLine(0));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (isService && idRef != null && contador == contadorUbicacion) {
                isService = false;
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

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            lytGPS.setVisibility(View.VISIBLE);
            cardErrorGPS.setVisibility(View.GONE);
        }

        @Override
        public void onProviderDisabled(String provider) {
            lytGPS.setVisibility(View.GONE);
            cardErrorGPS.setVisibility(View.VISIBLE);
        }
    };

    //TODO PROCESO PARA CAMBIAR LA IMAGEN DE PERFIL
    public void cargarImagen() {
        if(verificarPermisosImagen()) {
            CropImage.activity()
                    .setAspectRatio(1,1)
                    .start(MainActivity.this);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,PERMISOS_IMAGEN,CODIGO_PERMISOS_IMAGEN);
        }
    }

    //TODO IMAGEN SELECIONADA
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            Uri uri = result.getUri();

            if (uri != null) {

                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Subiendo...");
                progressDialog.show();

                final StorageReference storageReference = FirebaseStorage.getInstance().getReference("ImagenesPerfil")
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

                            FirebaseDatabase.getInstance().getReference("Usuarios")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .updateChildren(hashMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(MainActivity.this, "Actualizacion exitosa", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Ha ocurrido un error, intentelo mas tarde", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });

                        } else {
                            Toast.makeText(MainActivity.this, "Ha ocurrido un error, intentelo mas tarde", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Ha ocurrido un error, intentelo mas tarde", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

            } else {
                Toast.makeText(MainActivity.this, "Ninguna imagen seleccionada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //TODO OBTIENE LA EXTENSION DE UN ARCHIVO
    private String obtnerExtension(Uri uri) {
        return MimeTypeMap.getFileExtensionFromUrl(uri.toString());
    }

    @Override
    public void onLocationListener(String ref) {
        isService = true;
        idRef = ref;
        contadorUbicacion = 0;
    }


    //TODO LO DEMAS QUE NO SE QUE HACE
    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            /**Al recibir una pulsacion del reloj hara lo siguiente**/
            sendSMS();
            startService(new Intent(MainActivity.this, CameraService.class));
        }
    }

    private void abrirmiapp() {
        Intent i = getPackageManager().getLaunchIntentForPackage("com.ojooculto");
        if(i.resolveActivity(getPackageManager()) != null){
            startActivity(i);
        }
    }

    //METODO DE BOTONES FISICOS
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        contador++;
        if (keyCode == event.KEYCODE_VOLUME_UP) {
            if(contador == 3) {
                startService(new Intent(MainActivity.this, CameraService.class));
                Log.e("ALERTA","SERVICIO INICIADO");
                sendSMS();
                contador = 0;
            }else {
                Log.e("ERROR","INTENTELO MAS TARDE");
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == event.KEYCODE_VOLUME_DOWN) {
            startSupportChat();
            //startService(new Intent(MainActivity.this, CameraService.class));
        }
        return super.onKeyDown(keyCode, event);
    }
    //**************************************************************************************************************//
    //TODO FUNCION PARA ENVIAR MENSAJES
    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void sendSMS() {
        String text = "Mi direccion es: " + txtDireccion.getText().toString() + ", coordenadas: " + txtCoordenadas.getText().toString();
        try {
            SmsManager smr = SmsManager.getDefault();
            smr.sendTextMessage(txtNumeroAlerta.getText().toString(), null,"Estoy en peligro!!! \n"+text, null, null);
            //TODO Enviar 10 mensajes con imagen
            //TODO ya la active y nada
            //TODO este bundle con el numero no me convence
            /*Bundle configOverrides = new Bundle();
            configOverrides.putString("destinationAddress",NUMBER);
            if (direccones != null) {
                if (direccones.size() > 0) {
                    for (String url : direccones) {
                        Log.e("urlo",url);
                        try {
                            smr.sendMultimediaMessage(
                                    getApplicationContext(),
                                    Uri.parse(url),
                                    null,
                                    configOverrides,
                                    null
                            );
                        } catch (Exception ex ) {
                            Log.e("Ex", ex.toString());
                        }
                    }

                } else {
                    Log.e("A","Algo fallo");
                }
            } else {
                Log.e("asd","DASDAS");
            }*/
            smr.sendTextMessage(txtNumeroUsuario.getText().toString(), null,"HOLA \nEstoy en peligro!!! \n"+text, null, null);
            Toast.makeText(MainActivity.this, "SMS Enviado Satisfactoriamente", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this, "SMS No enviado, intenta de nuevo!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
//**************************************************************************************************************

    private void startSupportChat() {
        String text = "Mi direccion es: " + txtDireccion.getText().toString() + ", coordenadas: " + txtCoordenadas.getText().toString();
        try {
            //NUMBER = fullName.getText().toString();
            String bodyMessageFormal = "Estoy en peligro!!! \n" +text;// Replace with your message.
            Intent intent = new Intent ( Intent.ACTION_VIEW);
            intent.setData ( Uri.parse ( "https://wa.me/" + txtNumeroAlerta.getText().toString() + "/?text=" + bodyMessageFormal));
            startActivity ( intent );
        } catch (Exception e) {
            e.printStackTrace ();
        }

    }
}
