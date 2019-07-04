package com.github.anrimian.musicplayer.di.app;


import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.di.app.library.LibraryComponent;
import com.github.anrimian.musicplayer.di.app.library.LibraryModule;
import com.github.anrimian.musicplayer.di.app.play_list.PlayListComponent;
import com.github.anrimian.musicplayer.di.app.play_list.PlayListModule;
import com.github.anrimian.musicplayer.di.app.settings.SettingsComponent;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListPresenter;
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListPresenter;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.PlayListsPresenter;
import com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created on 11.02.2017.
 */

@Singleton
@Component(modules = {
        AppModule.class,
        SchedulerModule.class,
        ErrorModule.class,
        MusicModule.class,
        DbModule.class,
        StorageModule.class,
        SettingsModule.class,
        PlayListsModule.class
})
public interface AppComponent {

    LibraryComponent libraryComponent(LibraryModule libraryModule);
    PlayListComponent playListComponent(PlayListModule module);
    SettingsComponent settingsComponent();

    MusicPlayerInteractor musicPlayerInteractor();
    DisplaySettingsInteractor displaySettingsInteractor();

    PlayListsPresenter playListsPresenter();
    CreatePlayListPresenter createPlayListsPresenter();
    ChoosePlayListPresenter choosePlayListPresenter();

    UiStatePreferences uiStatePreferences();

    WidgetUpdater widgetUpdater();
    NotificationsDisplayer notificationDisplayer();

    void inject(MusicService musicService);
}