package com.ojooculto.BottomSheets;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ojooculto.Info.Encriptar;
import com.ojooculto.Moldes.Usuario;
import com.ojooculto.R;

public class CodeBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetBehavior bottomSheetBehavior;

    private TextInputLayout lytPassword;
    private TextInputLayout lytPasswordNew;
    private TextInputLayout lytPasswordRepeat;

    private TextInputEditText txtPassword;
    private TextInputEditText txtPasswordNew;
    private TextInputEditText txtPasswordRepeat;

    public CodeBottomSheet()
    {
    }


    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_code_bottom_sheet, null);
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

        lytPassword = view.findViewById(R.id.lytPassword);
        lytPasswordNew = view.findViewById(R.id.lytPasswordNew);
        lytPasswordRepeat = view.findViewById(R.id.lytPasswordRepeat);

        txtPassword = view.findViewById(R.id.txtPassword);
        txtPasswordNew = view.findViewById(R.id.txtPasswordNew);
        txtPasswordRepeat = view.findViewById(R.id.txtPasswordRepeat);

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
                        if (!isValidPassword() | !isValidPasswordNew() | !isValidPasswordRepeat()) {
                            return;
                        }

                        if (!txtPasswordNew.getText().toString().equals(txtPasswordRepeat.getText().toString())) {
                            lytPasswordRepeat.setError("Las contraseñas no coinciden");
                            return;
                        }

                        lytPasswordRepeat.setError(null);

                        final DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("Usuarios")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Usuario u = dataSnapshot.getValue(Usuario.class);
                                try {
                                    if (!Encriptar.desencriptar(u.getPassword()).equals(txtPassword.getText().toString())) {
                                        lytPassword.setError("La constraseña no coincide");
                                        return;
                                    }
                                    lytPassword.setError(null);
                                    FirebaseAuth.getInstance().getCurrentUser().updatePassword(txtPasswordNew.getText().toString())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    try {
                                                        refUser.child("password").setValue(Encriptar.encriptar(txtPasswordNew.getText().toString()))
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Toast.makeText(getContext(), "Contraseña modificada de manera exitosa", Toast.LENGTH_SHORT).show();
                                                                        dismiss();
                                                                    }
                                                                });
                                                    } catch (Exception ignored) {

                                                    }
                                                    Toast.makeText(getContext(), "Contraseña restablecida.", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), "Contraseña no restablecida.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } catch (Exception ignored) {

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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

    private boolean isValidPassword() {
        if (txtPassword.getText().toString().isEmpty()) {
            lytPassword.setError("Ingrese la contraseña");
            return false;
        }

        lytPassword.setError(null);
        return true;
    }

    private boolean isValidPasswordNew() {
        if (txtPasswordNew.getText().toString().isEmpty()) {
            lytPasswordNew.setError("Ingrese la contraseña");
            return false;
        }

        if (txtPasswordNew.getText().toString().length() < 5) {
            lytPasswordNew.setError("La contraseña debe de medir 6 o más caracteres");
            return false;
        }

        lytPasswordNew.setError(null);
        return true;
    }

    private boolean isValidPasswordRepeat() {
        if (txtPasswordRepeat.getText().toString().isEmpty()) {
            lytPasswordRepeat.setError("Ingrese la contraseña");
            return false;
        }

        if (txtPasswordRepeat.getText().toString().length() < 5 ) {
            lytPasswordRepeat.setError("La contraseña debe de medir 6 o más caracteres");
            return false;
        }

        lytPasswordRepeat.setError(null);
        return true;
    }
}

