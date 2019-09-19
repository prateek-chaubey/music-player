package com.github.anrimian.musicplayer.ui.common.dialogs.composition;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.java.BiCallback;
import com.github.anrimian.musicplayer.domain.utils.java.TripleCallback;
import com.github.anrimian.musicplayer.ui.common.serealization.CompositionSerialaser;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuAdapter;
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.StatusBarColorDelegate;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.EXTRA_DATA_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.MENU_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.STATUS_BAR_COLOR_ATTR_ARG;
import static com.github.anrimian.musicplayer.domain.models.composition.CompositionModelHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils.setupBottomSheetDialogMaxWidth;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.createMenu;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getContentView;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getFloat;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getStatusBarHeight;
import static com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils.attachDynamicShadow;

public class CompositionActionDialogFragment extends BottomSheetDialogFragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.title_shadow)
    View titleShadow;

    @BindView(R.id.tv_composition_name)
    TextView tvCompositionName;

    @BindView(R.id.tv_composition_author)
    TextView tvCompositionAuthor;

    @BindView(R.id.list_container)
    View listContainer;

    private Composition composition;

    private BiCallback<Composition, Integer> onCompleteListener;
    private TripleCallback<Composition, Integer, Bundle> onTripleCompleteListener;

    private SlideDelegate slideDelegate;

    public static CompositionActionDialogFragment newInstance(Composition composition,
                                                              @MenuRes int menu) {
        return newInstance(composition, menu, android.R.attr.statusBarColor);
    }


    public static CompositionActionDialogFragment newInstance(Composition composition,
                                                              @MenuRes int menu,
                                                              @AttrRes int statusBarColorAttr) {
        return newInstance(composition, menu, statusBarColorAttr, null);
    }

    public static CompositionActionDialogFragment newInstance(Composition composition,
                                                              @MenuRes int menu,
                                                              Bundle extra) {
        return newInstance(composition, menu, android.R.attr.statusBarColor, extra);
    }

    public static CompositionActionDialogFragment newInstance(Composition composition,
                                                              @MenuRes int menu,
                                                              @AttrRes int statusBarColorAttr,
                                                              Bundle extra) {
        Bundle args = new Bundle();
        args.putBundle(COMPOSITION_ARG, CompositionSerialaser.serialize(composition));
        args.putInt(MENU_ARG, menu);
        args.putInt(STATUS_BAR_COLOR_ATTR_ARG, statusBarColorAttr);
        args.putBundle(EXTRA_DATA_ARG, extra);
        CompositionActionDialogFragment fragment = new CompositionActionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View view = View.inflate(getContext(), R.layout.dialog_composition_menu, null);
        dialog.setContentView(view);

        DisplayMetrics displayMetrics = requireActivity().getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        float heightPercent = getFloat(getResources(), R.dimen.composition_action_dialog_height);
        int minHeight = (int) (height * heightPercent);

        slideDelegate = buildSlideDelegate();
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setPeekHeight(minHeight);
        bottomSheetBehavior.setBottomSheetCallback(new SimpleBottomSheetCallback(newState -> {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }, this::showBottomSheetSlided));

        ButterKnife.bind(this, view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        attachDynamicShadow(recyclerView, titleShadow);

        //noinspection ConstantConditions
        @Nonnull Bundle args = getArguments();
        //noinspection ConstantConditions
        composition = CompositionSerialaser.deserialize(args.getBundle(COMPOSITION_ARG));

        Menu menu = createMenu(requireContext(), getArguments().getInt(MENU_ARG));
        MenuAdapter menuAdapter = new MenuAdapter(menu);
        menuAdapter.setOnItemClickListener(this::onActionItemClicked);
        recyclerView.setAdapter(menuAdapter);

        tvCompositionName.setText(formatCompositionName(composition));
        tvCompositionAuthor.setText(formatCompositionAuthor(composition, requireContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        setupBottomSheetDialogMaxWidth(this);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        slideDelegate.onSlide(0f);
    }

    private void showBottomSheetSlided(float slideOffset) {
        recyclerView.post(() -> {
            View contentView = getContentView(getActivity());
            if (contentView == null) {
                return;
            }
            float usableSlideOffset = slideOffset;
            int activityHeight = contentView.getHeight() - getStatusBarHeight(requireContext());
            int viewHeight = listContainer.getHeight();
            if (activityHeight > viewHeight) {
                usableSlideOffset = 0;
            }
            slideDelegate.onSlide(usableSlideOffset);
        });
    }

    public void setOnCompleteListener(BiCallback<Composition, Integer> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void setOnTripleCompleteListener(TripleCallback<Composition, Integer, Bundle> onTripleCompleteListener) {
        this.onTripleCompleteListener = onTripleCompleteListener;
    }

    private void onActionItemClicked(MenuItem menuItem) {
        if (onCompleteListener != null) {
            onCompleteListener.call(composition, menuItem.getItemId());
        }
        if (onTripleCompleteListener != null) {
            //noinspection ConstantConditions
            onTripleCompleteListener.call(composition,
                    menuItem.getItemId(),
                    getArguments().getBundle(EXTRA_DATA_ARG));
        }
        dismiss();
    }

    private SlideDelegate buildSlideDelegate() {
        return new BoundValuesDelegate(0.85f, 1f,
                new StatusBarColorDelegate(requireActivity().getWindow(),
                        getColorFromAttr(getContext(), getStatusBarColorAttr()),
                        getColorFromAttr(requireContext(), R.attr.colorPrimaryDarkSecondary))
        );
    }

    @AttrRes
    private int getStatusBarColorAttr() {
        Bundle args = getArguments();
        if (args != null) {
            int colorAttr = args.getInt(STATUS_BAR_COLOR_ATTR_ARG);
            if (colorAttr != 0) {
                return colorAttr;
            }
        }
        return android.R.attr.statusBarColor;
    }
}