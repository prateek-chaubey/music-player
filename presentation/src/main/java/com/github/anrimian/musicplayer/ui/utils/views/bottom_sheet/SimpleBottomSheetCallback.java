package com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;

public class SimpleBottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {

    private Callback<Integer> onStateChangedCallback;
    private Callback<Float> onSlideCallback;

    public SimpleBottomSheetCallback(Callback<Integer> onStateChangedCallback,
                                     Callback<Float> onSlideCallback) {
        this.onStateChangedCallback = onStateChangedCallback;
        this.onSlideCallback = onSlideCallback;
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (onStateChangedCallback != null) {
            onStateChangedCallback.call(newState);
        }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        if (onSlideCallback != null) {
            onSlideCallback.call(slideOffset);
        }
    }
}