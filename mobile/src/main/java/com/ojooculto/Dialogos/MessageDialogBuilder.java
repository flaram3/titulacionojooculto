package com.ojooculto.Dialogos;

import android.view.View;

public class MessageDialogBuilder {
    private String title;
    private String message;
    private String positiveButtonText;
    private String negativeButtonText;

    private View.OnClickListener positiveButtonListener;
    private View.OnClickListener negativeButtonListener;

    private boolean isCancelable;

    public MessageDialogBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public MessageDialogBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    String getPositiveButtonText() {
        return positiveButtonText;
    }

    public MessageDialogBuilder setPositiveButtonText(String positiveButtonText) {
        this.positiveButtonText = positiveButtonText;
        return this;
    }

    String getNegativeButtonText() {
        return negativeButtonText;
    }

    public MessageDialogBuilder setNegativeButtonText(String negativeButtonText) {
        this.negativeButtonText = negativeButtonText;
        return this;
    }

    View.OnClickListener getPositiveButtonListener() {
        return positiveButtonListener;
    }

    public MessageDialogBuilder setPositiveButtonListener(View.OnClickListener positiveButtonListener) {
        this.positiveButtonListener = positiveButtonListener;
        return this;
    }

    View.OnClickListener getNegativeButtonListener() {
        return negativeButtonListener;
    }

    public MessageDialogBuilder setNegativeButtonListener(View.OnClickListener negativeButtonListener) {
        this.negativeButtonListener = negativeButtonListener;
        return this;
    }

    boolean isCancelable() {
        return isCancelable;
    }

    public MessageDialogBuilder setCancelable(boolean cancellable) {
        isCancelable = cancellable;
        return this;
    }
}
