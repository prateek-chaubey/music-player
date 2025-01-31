package com.github.anrimian.musicplayer.ui.library.folders.root;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentRootLibraryFoldersBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.LibraryFragment;
import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersFragmentKt;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.JugglerView;
import com.github.anrimian.musicplayer.ui.utils.wrappers.DefferedObject;

import java.util.List;

import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class LibraryFoldersRootFragment extends LibraryFragment
        implements FolderRootView, BackButtonListener {

    @InjectPresenter
    FolderRootPresenter presenter;

    private FragmentRootLibraryFoldersBinding binding;

    private JugglerView jvFoldersContainer;

    private FragmentNavigation navigation;
    private final DefferedObject<FragmentNavigation> navigationWrapper = new DefferedObject<>();

    @ProvidePresenter
    FolderRootPresenter providePresenter() {
        return Components.getLibraryRootFolderComponent().folderRootPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRootLibraryFoldersBinding.inflate(inflater, container, false);
        jvFoldersContainer = binding.libraryFoldersContainer;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.progressStateView.onTryAgainClick(presenter::onEmptyFolderStackArrived);

        navigation = FragmentNavigation.from(getChildFragmentManager());
        navigation.initialize(jvFoldersContainer, savedInstanceState);
        navigation.setExitAnimation(R.anim.anim_slide_out_right);
        navigation.setEnterAnimation(R.anim.anim_slide_in_right);
        navigationWrapper.setObject(navigation);

        if (!navigation.hasScreens()) {
            presenter.onEmptyFolderStackArrived();
        }
    }

    @Override
    public void onFragmentMovedOnTop() {
        super.onFragmentMovedOnTop();
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.folders);

        FragmentNavigation folderNavigation = FragmentNavigation.from(getChildFragmentManager());
        if (folderNavigation.isInitialized()) {
            folderNavigation.dispatchMovedToTop();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        navigation.onSaveInstanceState(outState);
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = navigation.getFragmentOnTop();
        return fragment instanceof BackButtonListener
                && ((BackButtonListener) fragment).onBackPressed();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        navigationWrapper.call(navigation -> navigation.setMenuVisible(menuVisible));
    }

    @Override
    public void showFolderScreens(List<Long> ids) {
        navigation.addNewFragmentStack(mapList(ids, LibraryFoldersFragmentKt::newFolderFragment),
                R.anim.anim_alpha_appear);
    }

    @Override
    public void showProgress() {
        binding.progressStateView.showProgress();
    }

    @Override
    public void showError(ErrorCommand errorCommand) {
        binding.progressStateView.showMessage(errorCommand.getMessage(), true);
    }

    @Override
    public void showIdle() {
        binding.progressStateView.hideAll();
    }
}
