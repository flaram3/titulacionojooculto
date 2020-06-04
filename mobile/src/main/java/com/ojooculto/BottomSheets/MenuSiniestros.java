package com.ojooculto.BottomSheets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.ojooculto.Dialogos.DialogoCode;
import com.ojooculto.MapsActivity;
import com.ojooculto.R;

public class MenuSiniestros extends BottomSheetDialogFragment {


    public MenuSiniestros() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_siniestros,container);

        ((ImageButton) view.findViewById(R.id.btn_cerrar))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });

        ((MaterialButton) view.findViewById(R.id.btnPerfil))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SiniestrosBottomSheet dialogoCode = new SiniestrosBottomSheet();
                        dialogoCode.show(getFragmentManager(),dialogoCode.getTag());
                        dismiss();
                    }
                });

        ((MaterialButton) view.findViewById(R.id.btnNombre))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getContext(), MapsActivity.class));
                        dismiss();
                    }
                });

        return view;
    }
}
