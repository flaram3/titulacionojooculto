package com.ojooculto.Dialogos;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ojooculto.BottomSheets.MenuSiniestros;
import com.ojooculto.BottomSheets.SiniestrosBottomSheet;
import com.ojooculto.Info.Encriptar;
import com.ojooculto.Moldes.Usuario;
import com.ojooculto.R;

import java.util.HashMap;

public class DialogoCode extends DialogFragment {

    public DialogoCode()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_dialog, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final TextInputEditText txtNombre = view.findViewById(R.id.txt_nombre);
        final TextInputLayout lytNombre = view.findViewById(R.id.til_nombre);

        ((MaterialButton) view.findViewById(R.id.btn_dialog_secondary))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });

        ((MaterialButton) view.findViewById(R.id.btn_dialog_primary))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!validaCampo(lytNombre,txtNombre,"Ingrese la contraseña")) {
                            return;
                        }

                        FirebaseDatabase.getInstance().getReference("Usuarios")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Usuario u = dataSnapshot.getValue(Usuario.class);
                                        try {
                                            if (Encriptar.desencriptar(u.getPassword()).equals(txtNombre.getText().toString())) {
                                                MenuSiniestros siniestrosBottomSheet = new MenuSiniestros();
                                                siniestrosBottomSheet.show(getFragmentManager(),siniestrosBottomSheet.getTag());
                                                dismiss();
                                            } else {
                                                lytNombre.setError("Contraseña incorrecta");
                                                Toast.makeText(getContext(), "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (Exception ignored) {

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                });

        return view;
    }

    private boolean validaCampo(TextInputLayout lyt,TextInputEditText txt, String error) {
        if (txt.getText().toString().isEmpty()) {
            lyt.setError(error);
            return false;
        }

        lyt.setError(null);
        return true;
    }
}