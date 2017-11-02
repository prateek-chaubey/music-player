package com.github.anrimian.simplemusicplayer.ui.library.main;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created on 02.11.2017.
 */

@InjectViewState
public class LibraryPresenter extends MvpPresenter<LibraryView> {

    private MusicPlayerInteractor interactor;
    private Scheduler uiScheduler;

    private CompositeDisposable presenterDisposable = new CompositeDisposable();

    public LibraryPresenter(MusicPlayerInteractor interactor, Scheduler uiScheduler) {
        this.interactor = interactor;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        presenterDisposable.add(interactor.getPlayerStateObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayerStateChanged));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onStopPlayButtonClicked() {
        interactor.stopPlaying();
    }

    void onStartPlayButtonClicked() {
        interactor.resumePlaying();
    }

    private void onPlayerStateChanged(PlayerState playerState) {
        switch (playerState) {
            case PLAYING: {
                getViewState().showPlayState();
                return;
            }
            default: {
                getViewState().showStopState();
            }
        }
    }


}
