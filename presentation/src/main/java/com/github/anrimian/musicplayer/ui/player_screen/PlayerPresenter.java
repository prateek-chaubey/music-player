package com.github.anrimian.musicplayer.ui.player_screen;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.player.PlayerScreenInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.PlayQueueItemHelper;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.DiffCalculator;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

import static com.github.anrimian.musicplayer.Constants.NO_POSITION;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.areSourcesTheSame;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

/**
 * Created on 02.11.2017.
 */

@InjectViewState
public class PlayerPresenter extends MvpPresenter<PlayerView> {

    private final MusicPlayerInteractor playerInteractor;
    private final PlayListsInteractor playListsInteractor;
    private final PlayerScreenInteractor playerScreenInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private final CompositeDisposable batterySafeDisposable = new CompositeDisposable();

    private List<PlayQueueItem> playQueue = new ArrayList<>();

    private final DiffCalculator<PlayQueueItem> diffCalculator = new DiffCalculator<>(
            () -> playQueue,
            PlayQueueItemHelper::areSourcesTheSame);

    private PlayQueueItem currentItem;
    private boolean isCoversEnabled = false;

    private final List<Composition> compositionsForPlayList = new LinkedList<>();
    private final List<Composition> compositionsToDelete = new LinkedList<>();

    private int currentPosition = NO_POSITION;

    private boolean scrollToPositionAfterUpdate = false;
    private boolean jumpToNewItem = true;

    public PlayerPresenter(MusicPlayerInteractor musicPlayerInteractor,
                           PlayListsInteractor playListsInteractor,
                           PlayerScreenInteractor playerScreenInteractor,
                           ErrorParser errorParser,
                           Scheduler uiScheduler) {
        this.playerInteractor = musicPlayerInteractor;
        this.playListsInteractor = playListsInteractor;
        this.playerScreenInteractor = playerScreenInteractor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().setSkipToNextButtonEnabled(true);
        getViewState().showRandomPlayingButton(playerInteractor.isRandomPlayingEnabled());
        if (playerScreenInteractor.isPlayerPanelOpen()) {
            getViewState().expandBottomPanel();
        } else {
            getViewState().collapseBottomPanel();
        }
        subscribeOnUiSettings();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onStart() {
        subscribeOnRepeatMode();
        subscribeOnPlayerStateChanges();
        subscribeOnPlayQueue();
        subscribeOnCurrentCompositionChanging();
        subscribeOnTrackPositionChanging();
    }

    void onStop() {
        jumpToNewItem = true;
        batterySafeDisposable.clear();
    }

    void onCurrentScreenRequested() {
        getViewState().showDrawerScreen(playerScreenInteractor.getSelectedDrawerScreen(),
                playerScreenInteractor.getSelectedPlayListScreenId());
    }

    void onOpenPlayQueueClicked() {
        playerScreenInteractor.setPlayerPanelOpen(true);
    }

    void onBottomPanelExpanded() {
        playerScreenInteractor.setPlayerPanelOpen(true);
        getViewState().expandBottomPanel();
    }

    void onBottomPanelCollapsed() {
        playerScreenInteractor.setPlayerPanelOpen(false);
        getViewState().collapseBottomPanel();
    }

    void onDrawerScreenSelected(int screenId) {
        playerScreenInteractor.setSelectedDrawerScreen(screenId);
        getViewState().showDrawerScreen(screenId, 0);
    }

    void onLibraryScreenSelected() {
        getViewState().showLibraryScreen(playerScreenInteractor.getSelectedLibraryScreen());
    }

    void onPlayButtonClicked() {
        playerInteractor.play();
    }

    void onStopButtonClicked() {
        playerInteractor.pause();
    }

    void onSkipToPreviousButtonClicked() {
        playerInteractor.skipToPrevious();
    }

    void onSkipToNextButtonClicked() {
        playerInteractor.skipToNext();
    }

    void onRepeatModeChanged(int mode) {
        playerInteractor.setRepeatMode(mode);
    }

    void onRandomPlayingButtonClicked(boolean enable) {
        scrollToPositionAfterUpdate = true;
        playerInteractor.setRandomPlayingEnabled(enable);
        getViewState().showRandomPlayingButton(enable);
    }

    void onShareCompositionButtonClicked() {
        getViewState().showShareMusicDialog(currentItem.getComposition().getFilePath());
    }

    void onCompositionItemClicked(int position, PlayQueueItem item) {
        this.currentItem = item;
        playerInteractor.skipToPosition(position);

        onCurrentCompositionChanged(item, 0);
    }

    void onQueueItemIconClicked(int position, PlayQueueItem playQueueItem) {
        if (playQueueItem.equals(currentItem)) {
            playerInteractor.playOrPause();
        } else {
            playerInteractor.play();
            onCompositionItemClicked(position, playQueueItem);
        }
    }

    void onTrackRewoundTo(int progress) {
        playerInteractor.seekTo(progress);
    }

    void onDeleteCompositionButtonClicked(Composition composition) {
        compositionsToDelete.clear();
        compositionsToDelete.add(composition);
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onDeleteCurrentCompositionButtonClicked() {
        compositionsToDelete.clear();
        compositionsToDelete.add(currentItem.getComposition());
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onAddQueueItemToPlayListButtonClicked(Composition composition) {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(composition);
        getViewState().showSelectPlayListDialog();
    }

    void onAddCurrentCompositionToPlayListButtonClicked() {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(currentItem.getComposition());
        getViewState().showSelectPlayListDialog();
    }

    void onPlayListForAddingSelected(PlayList playList) {
        addPreparedCompositionsToPlayList(playList);
    }

    void onPlayListForAddingCreated(PlayList playList) {
        List<Composition> compositionsToAdd = mapList(playQueue, PlayQueueItem::getComposition);
        playListsInteractor.addCompositionsToPlayList(compositionsToAdd, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> getViewState().showAddingToPlayListComplete(playList, compositionsToAdd),
                        this::onAddingToPlayListError);
    }

    void onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions();
    }

    void onSeekStart() {
        playerInteractor.onSeekStarted();
    }

    void onSeekStop(int progress) {
        playerInteractor.onSeekFinished(progress);
    }

    void onItemSwipedToDelete(Integer position) {
        deletePlayQueueItem(playQueue.get(position));
    }

    void onDeleteQueueItemClicked(PlayQueueItem item) {
        deletePlayQueueItem(item);
    }

    void onItemMoved(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                swapItems(i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                swapItems(i, i - 1);
            }
        }
    }

    void onEditCompositionButtonClicked() {
        getViewState().startEditCompositionScreen(currentItem.getComposition().getId());
    }

    private void swapItems(int from, int to) {
        PlayQueueItem fromItem = playQueue.get(from);
        PlayQueueItem toItem = playQueue.get(to);

        Collections.swap(playQueue, from, to);
        getViewState().notifyItemMoved(from, to);

        if (fromItem.equals(currentItem)) {
            currentPosition = to;
        }
        if (toItem.equals(currentItem)) {
            currentPosition = from;
        }
        playerInteractor.swapItems(fromItem, from, toItem, to);
    }

    private void deletePlayQueueItem(PlayQueueItem item) {
        playerInteractor.removeQueueItem(item)
                .observeOn(uiScheduler)
                .subscribe();
    }

    private void subscribeOnRepeatMode() {
        playerInteractor.getRepeatModeObservable()
                .observeOn(uiScheduler)
                .subscribe(getViewState()::showRepeatMode);
    }

    private void addPreparedCompositionsToPlayList(PlayList playList) {
        playListsInteractor.addCompositionsToPlayList(compositionsForPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> onAddingToPlayListCompleted(playList),
                        this::onAddingToPlayListError);
    }

    private void deletePreparedCompositions() {
        playerInteractor.deleteCompositions(compositionsToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteCompositionsSuccess, this::onDeleteCompositionError);
    }

    private void onDeleteCompositionsSuccess() {
        getViewState().showDeleteCompositionMessage(compositionsToDelete);
        compositionsToDelete.clear();
    }

    private void onDeleteCompositionError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteCompositionError(errorCommand);
        compositionsToDelete.clear();
    }

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(PlayList playList) {
        getViewState().showAddingToPlayListComplete(playList, compositionsForPlayList);
        compositionsForPlayList.clear();
    }

    private void subscribeOnCurrentCompositionChanging() {
        batterySafeDisposable.add(playerInteractor.getCurrentCompositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayQueueEventReceived));
    }

    private void onPlayQueueEventReceived(PlayQueueEvent playQueueEvent) {
        PlayQueueItem newItem = playQueueEvent.getPlayQueueItem();
        if (currentItem == null || !currentItem.equals(newItem)
                || !areSourcesTheSame(newItem.getComposition(), currentItem.getComposition())) {
            onCurrentCompositionChanged(newItem, playQueueEvent.getTrackPosition());
        }
    }

    private void onCurrentCompositionChanged(PlayQueueItem newItem, long trackPosition) {
        this.currentItem = newItem;
        getViewState().showCurrentQueueItem(newItem, isCoversEnabled);
        if (newItem != null) {
            getViewState().showTrackState(trackPosition, newItem.getComposition().getDuration());
            Integer position = playerInteractor.getQueuePosition(newItem);
            if (position != null) {
                scrollToItemPosition(position);
            }
        }
    }

    private void subscribeOnPlayerStateChanges() {
        batterySafeDisposable.add(playerInteractor.getPlayerStateObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayerStateChanged));
    }

    private void onPlayerStateChanged(PlayerState playerState) {
        switch (playerState) {
            case PLAY: {
                getViewState().showPlayState();
                return;
            }
            default: {
                getViewState().showStopState();
            }
        }
    }

    private void subscribeOnPlayQueue() {
        batterySafeDisposable.add(playerInteractor.getPlayQueueObservable()
                .map(diffCalculator::calculateChange)
                .observeOn(uiScheduler)
                .subscribe(this::onPlayQueueChanged, this::onPlayQueueReceivingError));
    }

    private void onPlayQueueChanged(ListUpdate<PlayQueueItem> update) {
        playQueue = update.getNewList();
        getViewState().showPlayQueueSubtitle(playQueue.size());
        getViewState().setMusicControlsEnabled(!playQueue.isEmpty());
        getViewState().setSkipToNextButtonEnabled(playQueue.size() > 1);
        getViewState().updatePlayQueue(update, !scrollToPositionAfterUpdate);
        if (scrollToPositionAfterUpdate) {
            scrollToPositionAfterUpdate = false;
            if (currentItem != null) {
                Integer position = playerInteractor.getQueuePosition(currentItem);
                if (position != null) {
                    scrollToItemPosition(position);
                }
            }
        }
    }

    private void scrollToItemPosition(int position) {
        boolean fastScroll = Math.abs(position - currentPosition) > 1;

        getViewState().scrollQueueToPosition(position, !jumpToNewItem && !fastScroll);
        jumpToNewItem = false;

        currentPosition = position;
    }

    private void onPlayQueueReceivingError(Throwable throwable) {
        errorParser.parseError(throwable);
        getViewState().setMusicControlsEnabled(false);
    }

    private void subscribeOnTrackPositionChanging() {
        batterySafeDisposable.add(playerInteractor.getTrackPositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onTrackPositionChanged));
    }

    private void onTrackPositionChanged(Long currentPosition) {
        if (currentItem != null) {
            long duration = currentItem.getComposition().getDuration();
            getViewState().showTrackState(currentPosition, duration);
        }
    }

    private void subscribeOnUiSettings() {
        presenterDisposable.add(playerScreenInteractor.getCoversEnabledObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onUiSettingsReceived, errorParser::logError));
    }

    private void onUiSettingsReceived(boolean isCoversEnabled) {
        this.isCoversEnabled = isCoversEnabled;
        getViewState().setPlayQueueCoversEnabled(isCoversEnabled);
        if (currentItem != null) {
            getViewState().showCurrentQueueItem(currentItem, isCoversEnabled);
        }
    }
}
