package com.ojooculto.Dialogos;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ojooculto.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DialogVerDireccion extends DialogFragment {

    private Location location;
    private LatLng latLng;

    public DialogVerDireccion(Location location) {
        this.location = location;
    }

    public DialogVerDireccion(LatLng latLng) {
        this.latLng = latLng;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_ver_direccion, null);
        builder.setView(view);

        final TextView txtDireccion = (TextView) view.findViewById(R.id.txtDVDdireccion);
        Button btnCerrar = (Button) view.findViewById(R.id.btnDVDCerrar);

        final MapView mMapView = (MapView) view.findViewById(R.id.frmDVRMapa);
        MapsInitializer.initialize(getActivity());

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if (location != null) {
                        if (location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
                            try {
                                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                                List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (!list.isEmpty()) {
                                    Address dirCalle = list.get(0);
                                    txtDireccion.setText(dirCalle.getAddressLine(0) + "\nLat: " + location.getLatitude() + ", Long: " + location.getLongitude());
                                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    googleMap.addMarker(new MarkerOptions().title("Dirección").position(latLng).snippet(dirCalle.getAddressLine(0)));
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        return;
                                    }
                                    googleMap.setMyLocationEnabled(true);
                                } else {
                                    Toast.makeText(getContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                }
                            } catch (IOException e) {
                                Toast.makeText(getContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        }
                    }
                    if (latLng != null) {
                        try {
                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                            List<Address> list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                            if (!list.isEmpty()) {
                                Address dirCalle = list.get(0);
                                txtDireccion.setText(dirCalle.getAddressLine(0)  + "\nLat: " + latLng.latitude + ", Long: " + latLng.longitude);
                                Log.e("ads","" + latLng.toString());
                                googleMap.addMarker(new MarkerOptions().title("Dirección").position(latLng).snippet(dirCalle.getAddressLine(0)));
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                googleMap.setMyLocationEnabled(true);
                            } else {
                                Toast.makeText(getContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "Ha ocurrido un error", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    }

                }
            });

        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        setCancelable(false);
        return builder.create();
    }
}
