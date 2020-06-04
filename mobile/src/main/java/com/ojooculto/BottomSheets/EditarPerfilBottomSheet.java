package com.ojooculto.BottomSheets;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ojooculto.Dialogos.MessageDialog;
import com.ojooculto.Dialogos.MessageDialogBuilder;
import com.ojooculto.MainActivity;
import com.ojooculto.Moldes.Usuario;
import com.ojooculto.R;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfilBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetBehavior bottomSheetBehavior;

    private boolean isError;

    private String correo;

    private MainActivity mainActivity;

    public EditarPerfilBottomSheet(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
    }

    private ValueEventListener valueEventListener;
    private DatabaseReference reference;


    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_editar_perfil_bottom_sheet, null);
        bottomSheet.setContentView(view);
        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if(BottomSheetBehavior.STATE_HIDDEN == i) dismiss();
                if(BottomSheetBehavior.STATE_DRAGGING == i) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override public void onSlide(@NonNull View view, float v) { }
        });

        final TextInputLayout lytNombre = view.findViewById(R.id.lytNombre);
        final TextInputLayout lytNumero = view.findViewById(R.id.lytNumero);
        final TextInputLayout lytNumeroAlerta = view.findViewById(R.id.lytNumeroAlerta);
        final TextInputLayout lytCorreo = view.findViewById(R.id.lytEmail);

        final TextInputEditText txtNombre = view.findViewById(R.id.txtNombre);
        final TextInputEditText txtNumero = view.findViewById(R.id.txtNumero);
        final TextInputEditText txtNumeroAlerta = view.findViewById(R.id.txtNumeroAlerta);
        final TextInputEditText txtCorreo = view.findViewById(R.id.txtEmail);

        final CircleImageView imgPerfil = view.findViewById(R.id.imgPerfil);

        imgPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MessageDialog dialog = new MessageDialog(getContext(), new MessageDialogBuilder()
                        .setTitle("Alerta")
                        .setMessage("¿Desea cambiar su foto de perfil?")
                        .setPositiveButtonText("Cambiar")
                        .setNegativeButtonText("Más tarde")
                );
                dialog.show();
                dialog.setPositiveButtonListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mainActivity.cargarImagen();
                        dialog.dismiss();
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

        //TODO Referencia para obtener la informacion del usuario
        reference =  FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        valueEventListener = reference
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return;
                        }
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        txtNombre.setText(usuario.getfName());
                        txtCorreo.setText(usuario.getEmail());
                        txtNumero.setText(usuario.getPhone());
                        txtNumeroAlerta.setText(usuario.getNumeroAlerta());
                        correo = usuario.getEmail();
                        if (usuario.getImg().equals("default")) {
                            imgPerfil.setImageResource(R.drawable.ic_person_black_24dp);
                        } else {
                            Glide.with(getContext())
                                    .load(usuario.getImg())
                                    .into(imgPerfil);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        ((ImageButton) view.findViewById(R.id.btn_cerrar))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });

        ((Button) view.findViewById(R.id.btnGuardar))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!validaCampo(lytNombre,txtNombre,"Ingrese el nombre")
                                | !validaCampo(lytNumeroAlerta,txtNumeroAlerta,"Ingrese el numero de alerta")
                                | !validaCampo(lytNumero,txtNumero,"Ingrese el numero telefonico")
                                | !validaCampo(lytCorreo,txtCorreo,"Ingrese el correo electronico")) {
                            return;
                        }

                        if (correo != null) {
                            if (!correo.equals(txtCorreo)) {
                                FirebaseAuth.getInstance().getCurrentUser()
                                        .updateEmail(txtCorreo.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Map<String,Object> edited = new HashMap<>();
                                        edited.put("email",txtCorreo.getText().toString().trim());
                                        edited.put("fName",txtNombre.getText().toString().trim());
                                        edited.put("phone",txtNumero.getText().toString().trim());
                                        edited.put("numeroAlerta",txtNumeroAlerta.getText().toString().trim());
                                        FirebaseDatabase.getInstance().getReference("Usuarios")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .updateChildren(edited)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                                                            dismiss();
                                                        } else {
                                                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                        Toast.makeText(getContext(), "Email is changed.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(),   e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            Map<String,Object> edited = new HashMap<>();
                            edited.put("fName",txtNombre.getText().toString().trim());
                            edited.put("phone",txtNumero.getText().toString().trim());
                            edited.put("numeroAlerta",txtNumeroAlerta.getText().toString().trim());
                            FirebaseDatabase.getInstance().getReference("Usuarios")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .updateChildren(edited)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                                                dismiss();
                                            } else {
                                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });

        ((MaterialButton) view.findViewById(R.id.btnCambioContraseña))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CodeBottomSheet codeBottomSheet = new CodeBottomSheet();
                        codeBottomSheet.show(getChildFragmentManager(),codeBottomSheet.getTag());
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final Toolbar toolbar = view.findViewById(R.id.toolbar);
            NestedScrollView nestedScrollView = view.findViewById(R.id.nested_scroll_ventas);
            nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if(scrollY == 0) {
                        toolbar.setElevation(0);
                    } else {
                        toolbar.setElevation(8);
                    }
                }
            });
        }
        setCancelable(false);
        return bottomSheet;
    }

    @Override
    public void onStart() {
        super.onStart();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        reference.removeEventListener(valueEventListener);
    }

    private boolean validaCampo(TextInputLayout lyt, TextInputEditText txt, String error) {
        if (txt.getText().toString().isEmpty()) {
            lyt.setError(error);
            if (!isError) {
                txt.requestFocus();
                isError = true;
            }
            return false;
        }

        isError = false;
        lyt.setError(null);
        return true;
    }


}