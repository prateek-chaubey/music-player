package com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen.view.adapter.PlayQueueAdapter;
import com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen.view.delegate.ChangeTitleDelegate;
import com.github.anrimian.simplemusicplayer.ui.settings.SettingsFragment;
import com.github.anrimian.simplemusicplayer.ui.start.StartFragment;
import com.github.anrimian.simplemusicplayer.ui.storage_library_screen.StorageLibraryFragment;
import com.github.anrimian.simplemusicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.simplemusicplayer.ui.utils.views.delegate.BottomSheetDelegate;
import com.github.anrimian.simplemusicplayer.ui.utils.views.delegate.BottomSheetDelegateManager;
import com.github.anrimian.simplemusicplayer.ui.utils.views.delegate.BoundValuesDelegate;
import com.github.anrimian.simplemusicplayer.ui.utils.views.delegate.ExpandViewDelegate;
import com.github.anrimian.simplemusicplayer.ui.utils.views.delegate.ReverseDelegate;
import com.github.anrimian.simplemusicplayer.ui.utils.views.delegate.TargetViewDelegate;
import com.github.anrimian.simplemusicplayer.ui.utils.views.delegate.VisibilityDelegate;
import com.github.anrimian.simplemusicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper;
import com.github.anrimian.simplemusicplayer.ui.utils.views.view_pager.FragmentCreator;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatCompositionName;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.simplemusicplayer.utils.AndroidUtils.getColorFromAttr;

/**
 * Created on 19.10.2017.
 */

public class PlayerFragment extends MvpAppCompatFragment implements BackButtonListener, PlayerView {

    private static final int NO_ITEM = -1;
    private static final String SELECTED_DRAWER_ITEM = "selected_drawer_item";
    private static final String BOTTOM_SHEET_STATE = "bottom_sheet_state";

    private static final SparseArray<FragmentCreator> fragmentIdMap = new SparseArray<>();

    static {
        fragmentIdMap.put(R.id.menu_settings, SettingsFragment::new);
    }


    @InjectPresenter
    PlayerPresenter presenter;

    @BindView(R.id.drawer)
    DrawerLayout drawer;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    @BindView(R.id.bottom_sheet)
    CoordinatorLayout bottomSheet;

    @BindView(R.id.rv_playlist)
    RecyclerView rvPlayList;

    @BindView(R.id.iv_play_pause)
    ImageView ivPlayPause;

    @BindView(R.id.iv_skip_to_previous)
    ImageView ivSkipToPrevious;

    @BindView(R.id.iv_skip_to_next)
    ImageView ivSkipToNext;

    @BindView(R.id.iv_play_pause_expanded)
    ImageView ivPlayPauseExpanded;

    @BindView(R.id.iv_skip_to_previous_expanded)
    ImageView ivSkipToPreviousExpanded;

    @BindView(R.id.iv_skip_to_next_expanded)
    ImageView ivSkipToNextExpanded;

    @BindView(R.id.drawer_fragment_container)
    ViewGroup fragmentContainer;

    @BindView(R.id.tv_current_composition)
    TextView tvCurrentComposition;

    @BindView(R.id.btn_infinite_play)
    ImageView btnInfinitePlay;

    @BindView(R.id.btn_random_play)
    ImageView btnRandomPlay;

    @BindView(R.id.tv_played_time)
    TextView tvPlayedTime;

    @BindView(R.id.tv_total_time)
    TextView tvTotalTime;

    @BindView(R.id.sb_track_state)
    AppCompatSeekBar sbTrackState;

    @BindView(R.id.bottom_sheet_top_shadow)
    View bottomSheetTopShadow;

    @BindView(R.id.top_panel)
    View topBottomSheetPanel;

//    @BindView(R.id.bottom_sheet_bottom_shadow)
//    View bottomSheetBottomShadow;

    @BindView(R.id.iv_music_icon)
    ImageView ivMusicIcon;

    @BindView(R.id.btn_actions_menu)
    ImageView btnActionsMenu;

    @BindView(R.id.tv_current_composition_info)
    TextView tvCurrentCompositionInfo;

    @BindView(R.id.tv_current_composition_author)
    TextView tvCurrentCompositionAuthor;

//    @BindView(R.id.play_actions_top_shadow)
//    View playActionsTopShadow;

//    @BindView(R.id.bottom_panel)
//    View bottomPanel;

    private BottomSheetBehavior<CoordinatorLayout> bottomSheetBehavior;

    private PlayQueueAdapter playQueueAdapter;

    private BottomSheetDelegate bottomSheetDelegate;

    private ActionBarDrawerToggle drawerToggle;

    private int selectedDrawerItemId = NO_ITEM;
    private int itemIdToStart = NO_ITEM;

    private LinearLayoutManager playQueueLayoutManager;
    private SeekBarViewWrapper seekBarViewWrapper;

    @ProvidePresenter
    PlayerPresenter providePresenter() {
        return Components.getLibraryComponent().libraryPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        ButterKnife.bind(this, view);

        RxPermissions rxPermissions = new RxPermissions(getActivity());
        if (!rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, new StartFragment())
                    .commit();
            return;
        }

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (selectedDrawerItemId != itemId) {
                selectedDrawerItemId = itemId;
                itemIdToStart = itemId;
                clearFragment();
            }
            drawer.closeDrawer(Gravity.START);

            return true;
        });

        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawer, R.string.open_drawer, R.string.close_drawer);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (itemIdToStart != NO_ITEM) {
                    switch (itemIdToStart) {
                        case R.id.menu_library : {
                            startFragment(StorageLibraryFragment.newInstance(null));
                            break;
                        }
                        default: {
                            FragmentCreator fragmentCreator = fragmentIdMap.get(itemIdToStart);
                            startFragment(fragmentCreator.createFragment());
                        }
                    }
                    itemIdToStart = NO_ITEM;
                }
            }
        });

        BottomSheetDelegateManager bottomSheetDelegateManager = new BottomSheetDelegateManager();
        bottomSheetDelegateManager.addDelegate(new TargetViewDelegate(ivPlayPause, ivPlayPauseExpanded))
                .addDelegate(new TargetViewDelegate(ivSkipToPrevious, ivSkipToPreviousExpanded))
                .addDelegate(new TargetViewDelegate(ivSkipToNext, ivSkipToNextExpanded))
//                .addDelegate(new VisibilityDelegate(rvPlayList))
                .addDelegate(new BoundValuesDelegate(0f, 0.6f, new ReverseDelegate(new VisibilityDelegate(fragmentContainer))))
                .addDelegate(new BoundValuesDelegate(0.3f, 1.0f, new ExpandViewDelegate(R.dimen.music_icon_size, ivMusicIcon)))
                .addDelegate(new ChangeTitleDelegate(tvCurrentComposition, btnActionsMenu, ivSkipToPrevious))
                .addDelegate(new BoundValuesDelegate(0.95f, 1.0f, new VisibilityDelegate(tvCurrentCompositionAuthor)))
                .addDelegate(new BoundValuesDelegate(0.8f, 1.0f, new VisibilityDelegate(tvCurrentCompositionInfo)))
                .addDelegate(new BoundValuesDelegate(0.4f, 1.0f, new VisibilityDelegate(btnActionsMenu)))
//                .addDelegate(new BoundValuesDelegate(0.4f, 0.6f, new VisibilityDelegate(bottomSheetBottomShadow)))
//                .addDelegate(new BoundValuesDelegate(0.9f, 1.0f, new VisibilityDelegate(rvPlayList)))
                .addDelegate(new BoundValuesDelegate(0.93f, 1.0f, new VisibilityDelegate(sbTrackState)))
//                .addDelegate(new BoundValuesDelegate(0.95f, 1.0f, new VisibilityDelegate(playActionsTopShadow)))
                .addDelegate(new BoundValuesDelegate(0.98f, 1.0f, new VisibilityDelegate(btnInfinitePlay)))
                .addDelegate(new BoundValuesDelegate(0.98f, 1.0f, new VisibilityDelegate(btnRandomPlay)))
                .addDelegate(new BoundValuesDelegate(0.97f, 1.0f, new VisibilityDelegate(tvPlayedTime)))
                .addDelegate(new BoundValuesDelegate(0.97f, 1.0f, new VisibilityDelegate(tvTotalTime)));

        bottomSheetDelegate = new BoundValuesDelegate(0.05f, 0.95f, bottomSheetDelegateManager);


        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheet.setClickable(true);

        int bottomSheetState = STATE_COLLAPSED;
        if (savedInstanceState != null) {
            bottomSheetState = savedInstanceState.getInt(BOTTOM_SHEET_STATE);
        }
        bottomSheetDelegate.onSlide(bottomSheetState == STATE_COLLAPSED ? 0f : 1f);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case STATE_COLLAPSED: {
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        bottomSheetDelegate.onSlide(0f);
                        return;
                    }
                    case STATE_EXPANDED: {
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        bottomSheetDelegate.onSlide(1f);
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset > 0F && slideOffset < 1f) {
                    bottomSheetDelegate.onSlide(slideOffset);
                }
            }
        });

        bottomSheetBehavior.setState(bottomSheetState);

        ivSkipToPrevious.setOnClickListener(v -> presenter.onSkipToPreviousButtonClicked());
        ivSkipToNext.setOnClickListener(v -> presenter.onSkipToNextButtonClicked());

        playQueueLayoutManager = new LinearLayoutManager(getContext());
        rvPlayList.setLayoutManager(playQueueLayoutManager);

        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.drawer_fragment_container);
        if (currentFragment == null || savedInstanceState == null) {
            showLibraryScreen();
        } else {
            selectedDrawerItemId = savedInstanceState.getInt(SELECTED_DRAWER_ITEM, NO_ITEM);
        }

        btnActionsMenu.setOnClickListener(this::onCompositionMenuClicked);
//        bottomPanel.setOnClickListener(v -> onBottomPanelClicked());
        topBottomSheetPanel.setOnClickListener(v -> onTopPanelClicked());

        seekBarViewWrapper = new SeekBarViewWrapper(sbTrackState);
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo);
        seekBarViewWrapper.setOnSeekStartListener(presenter::onSeekStart);
        seekBarViewWrapper.setOnSeekStopListener(presenter::onSeekStop);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean toggleSelected = drawerToggle.onOptionsItemSelected(item);
        return toggleSelected || super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_DRAWER_ITEM, selectedDrawerItemId);
        outState.putInt(BOTTOM_SHEET_STATE, bottomSheetBehavior.getState());
    }

    @Override
    public boolean onBackPressed() {
        if (bottomSheetBehavior.getState() == STATE_EXPANDED) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);
            return true;
        }
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.drawer_fragment_container);
        return fragment instanceof BackButtonListener && ((BackButtonListener) fragment).onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop();
    }

    @Override
    public void showStopState() {
        ivPlayPause.setImageResource(R.drawable.ic_play);
        ivPlayPause.setOnClickListener(v -> presenter.onPlayButtonClicked());
    }

    @Override
    public void showPlayState() {
        ivPlayPause.setImageResource(R.drawable.ic_pause);
        ivPlayPause.setOnClickListener(v -> presenter.onStopButtonClicked());
    }

    @Override
    public void showMusicControls(boolean show) {
        setContentBottomHeight(show ?
                getResources().getDimensionPixelSize(R.dimen.bottom_sheet_height) : 0);

        if (!show && bottomSheetBehavior.getState() == STATE_EXPANDED) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        }
    }

    @Override
    public void showCurrentComposition(Composition composition, int position) {
        tvCurrentComposition.setText(formatCompositionName(composition));
        tvTotalTime.setText(formatMilliseconds(composition.getDuration()));
        tvCurrentCompositionInfo.setText(composition.getAlbum());
        tvCurrentCompositionAuthor.setText(formatCompositionAuthor(composition, getContext()));

        playQueueAdapter.onCurrentCompositionChanged(composition);

        if (position >= playQueueLayoutManager.findLastVisibleItemPosition()) {
            playQueueLayoutManager.scrollToPositionWithOffset(position, 0);
        } else {
            playQueueLayoutManager.scrollToPosition(position);
        }
    }

    @Override
    public void bindPlayList(List<Composition> currentPlayList) {
        playQueueAdapter = new PlayQueueAdapter(currentPlayList);
        playQueueAdapter.setOnCompositionClickListener(presenter::onCompositionItemClicked);
        rvPlayList.setAdapter(playQueueAdapter);
    }

    @Override
    public void updatePlayQueue(List<Composition> currentPlayList, List<Composition> newPlayList) {
        playQueueAdapter.updatePlayList(currentPlayList, newPlayList);
    }

    @Override
    public void showInfinitePlayingButton(boolean active) {
        if (active) {
            int selectedColor = getColorFromAttr(getContext(), R.attr.colorAccent);
            btnInfinitePlay.setColorFilter(selectedColor);
            btnInfinitePlay.setOnClickListener(v -> presenter.onDisableInfinitePlayingButtonClicked());
        } else {
            btnInfinitePlay.clearColorFilter();
            btnInfinitePlay.setOnClickListener(v -> presenter.onEnableInfinitePlayingButtonClicked());
        }
    }

    @Override
    public void showRandomPlayingButton(boolean active) {
        if (active) {
            int selectedColor = getColorFromAttr(getContext(), R.attr.colorAccent);
            btnRandomPlay.setColorFilter(selectedColor);
            btnRandomPlay.setOnClickListener(v -> presenter.onDisableRandomPlayingButtonClicked());
        } else {
            btnRandomPlay.clearColorFilter();
            btnRandomPlay.setOnClickListener(v -> presenter.onEnableRandomPlayingButtonClicked());
        }
    }

    @Override
    public void showTrackState(long currentPosition, long duration) {
        int progress = 0;
        if (duration != 0) {
            progress = (int) (currentPosition * 100 / duration);
        }
        seekBarViewWrapper.setProgress(progress);
        tvPlayedTime.setText(formatMilliseconds(currentPosition));
    }

    @Override
    public void showShareMusicDialog(String filePath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        Uri fileUri = FileProvider.getUriForFile(getContext(), getString(R.string.file_provider_authorities), new File(filePath));
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    @Override
    public void notifyPlayQueueItemRemoved(int index) {
        playQueueAdapter.notifyItemRemoved(index);
    }

    private void onBottomPanelClicked() {
        if (bottomSheetBehavior.getState() == STATE_EXPANDED) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        }
    }

    private void onTopPanelClicked() {
        if (bottomSheetBehavior.getState() == STATE_COLLAPSED) {
            bottomSheetBehavior.setState(STATE_EXPANDED);
        }
    }

    private void setContentBottomHeight(int heightInPixels) {
        bottomSheetBehavior.setPeekHeight(heightInPixels);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fragmentContainer.getLayoutParams();
        layoutParams.bottomMargin = heightInPixels;
        fragmentContainer.setLayoutParams(layoutParams);
    }

    private void startFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        Fragment existFragment = fragmentManager.findFragmentById(R.id.drawer_fragment_container);
        if (existFragment == null || existFragment.getClass() != fragment.getClass()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.anim_alpha_appear, R.anim.anim_alpha_disappear)
                    .replace(R.id.drawer_fragment_container, fragment)
                    .commit();
        }
    }

    private void showLibraryScreen() {
        selectedDrawerItemId = R.id.menu_library;
        navigationView.setCheckedItem(selectedDrawerItemId);
        startFragment(StorageLibraryFragment.newInstance(null));
    }

    private void clearFragment() {
        FragmentManager fm = getFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.drawer_fragment_container);
        if (currentFragment != null) {
            fm.beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }
    }

    private void onCompositionMenuClicked(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.composition_full_actions_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_share: {
                    presenter.onShareCompositionButtonClicked();
                    return true;
                }
                case R.id.menu_delete: {
                    presenter.onDeleteCompositionButtonClicked();//TODO also show dialog
                    return true;
                }
            }
            return false;
        });
        popup.show();
    }
}
