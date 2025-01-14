package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.view.View;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.core.view.ViewCompat.isLaidOut;

/**
 * Created on 21.01.2018.
 */

public class VisibilityDelegate implements SlideDelegate {

    private final View view;

    public VisibilityDelegate(View view) {
        this.view = view;
    }

    @Override
    public void onSlide(float slideOffset) {
        if (isLaidOut(view)) {
            makeVisible(slideOffset);
        } else {
            view.post(() -> makeVisible(slideOffset));
        }
    }

    private void makeVisible(float slideOffset) {
        view.setVisibility(slideOffset == 0 ? INVISIBLE : VISIBLE);
        view.setAlpha(slideOffset);
    }
}
