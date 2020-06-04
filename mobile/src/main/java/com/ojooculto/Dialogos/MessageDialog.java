package com.ojooculto.Dialogos;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.ojooculto.R;

public class MessageDialog extends Dialog {

    private String title;
    private String message;
    private String positiveButtonText;
    private String negativeButtonText;

    private View.OnClickListener positiveButtonListener;
    private View.OnClickListener negativeButtonListener;

    private Button btnPositive;
    private Button btnNegative;

    private boolean isCancelable;

    public MessageDialog(@NonNull Activity activity, @NonNull MessageDialogBuilder builder) {
        super(activity);
        title = builder.getTitle();
        message = builder.getMessage();
        positiveButtonText = builder.getPositiveButtonText();
        negativeButtonText = builder.getNegativeButtonText();
        positiveButtonListener = builder.getPositiveButtonListener();
        negativeButtonListener = builder.getNegativeButtonListener();
        isCancelable = builder.isCancelable();
    }

    public MessageDialog(@NonNull Context context, @NonNull MessageDialogBuilder builder) {
        super(context);
        title = builder.getTitle();
        message = builder.getMessage();
        positiveButtonText = builder.getPositiveButtonText();
        negativeButtonText = builder.getNegativeButtonText();
        positiveButtonListener = builder.getPositiveButtonListener();
        negativeButtonListener = builder.getNegativeButtonListener();
        isCancelable = builder.isCancelable();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_mensaje_dialog);
        TextView tvTitle = findViewById(R.id.tv_dialog_title);
        TextView tvMessage = findViewById(R.id.tv_dialog_message);
        btnPositive = findViewById(R.id.btn_dialog_primary);
        btnPositive.setText(positiveButtonText);
        if(positiveButtonListener != null) {
            btnPositive.setOnClickListener(positiveButtonListener);
        }
        btnNegative = findViewById(R.id.btn_dialog_secondary);
        if(negativeButtonText != null) {
            if (!negativeButtonText.isEmpty()) {
                btnNegative.setText(negativeButtonText);
                if (negativeButtonListener != null) {
                    btnNegative.setOnClickListener(negativeButtonListener);
                }
            } else {
                btnNegative.setVisibility(View.GONE);
            }
        } else {
            btnNegative.setVisibility(View.GONE);
        }
        tvTitle.setText(title);
        tvMessage.setText(message);
        setCancelable(isCancelable);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void setPositiveButtonListener(View.OnClickListener listener) {
        btnPositive.setOnClickListener(listener);
    }

    public void setNegativeButtonListener(View.OnClickListener listener) {
        btnNegative.setOnClickListener(listener);
    }
}