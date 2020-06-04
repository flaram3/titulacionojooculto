package com.ojooculto.Adaptadores;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.ojooculto.BottomSheets.ImagenesBotttomSheet;
import com.ojooculto.Dialogos.DialogVerDireccion;
import com.ojooculto.Info.Info;
import com.ojooculto.Moldes.Siniestro;
import com.ojooculto.R;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdaptadorSiniestros extends RecyclerView.Adapter<AdaptadorSiniestros.ViewHolder> {

    private Context context;
    private ArrayList<Siniestro> siniestros;
    private FragmentManager fragmentManager;

    public AdaptadorSiniestros(Context context, ArrayList<Siniestro> siniestros, FragmentManager fragmentManager) {
        this.context = context;
        this.siniestros = siniestros;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_adaptador_siniestros,parent,false);
        return new AdaptadorSiniestros.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Siniestro s = siniestros.get(position);

        Date date = new Date(s.getTime());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.getDefault());

        holder.txtFecha.setText(simpleDateFormat.format(date));

        holder.txtImagenes.setText(s.getImagenes() + " imagenes");

        if (s.isAudio()) {
            holder.txtAudio.setText("Audio disponible");
        } else {
            holder.txtAudio.setText("Audio no disponible");
        }

        if (s.isUbicacion()) {
            holder.txtUbicacion.setText("Ubicación disponible");
            holder.imgVerEnMapa.setVisibility(View.VISIBLE);
            holder.imgVerEnMapa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogVerDireccion dialogVerDireccion = new DialogVerDireccion(new LatLng(s.getLat(),s.getLon()));
                    dialogVerDireccion.show(fragmentManager,dialogVerDireccion.getTag());
                }
            });
        } else {
            holder.txtUbicacion.setText("Ubicación no disponible");
            holder.imgVerEnMapa.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Info.Ingreasar(context,s.getId());
                ImagenesBotttomSheet botttomSheet = new ImagenesBotttomSheet();
                botttomSheet.show(fragmentManager,botttomSheet.getTag());
            }
        });
    }

    @Override
    public int getItemCount() {
        return siniestros.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtFecha;
        private TextView txtImagenes;
        private TextView txtAudio;
        private TextView txtUbicacion;
        private ImageButton imgVerEnMapa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAudio = itemView.findViewById(R.id.txtAudio);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtImagenes = itemView.findViewById(R.id.txtImagenes);
            txtUbicacion = itemView.findViewById(R.id.txtUbicacion);
            imgVerEnMapa = itemView.findViewById(R.id.btnVerEnMapa);
        }
    }
}
