package com.ojooculto.Adaptadores;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ojooculto.ImageActivity;
import com.ojooculto.Moldes.AuxSiniestro;
import com.ojooculto.R;

import java.util.ArrayList;

public class AdaptadorImagenes extends RecyclerView.Adapter<AdaptadorImagenes.ViewHolder> {

    private Context context;
    private ArrayList<AuxSiniestro> auxSiniestros;

    public AdaptadorImagenes(Context context, ArrayList<AuxSiniestro> auxSiniestros) {
        this.context = context;
        this.auxSiniestros = auxSiniestros;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_adaptador_imagenes,parent,false);
        return new AdaptadorImagenes.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final AuxSiniestro a = auxSiniestros.get(position);

        Glide.with(context)
                .load(a.getImg())
                .into(holder.imgFoto);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ImageActivity.class);
                intent.putExtra("url",a.getImg());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return auxSiniestros.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgFoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgFoto = itemView.findViewById(R.id.imgItemGallery);
        }
    }
}
