package com.github.anrimian.musicplayer.di.app;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.DB_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.IO_SCHEDULER;

import android.content.Context;
import android.os.Build;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.StorageCompositionsInserter;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.library.edit.EditorRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.scanner.MediaScannerRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.scanner.StorageCompositionAnalyzer;
import com.github.anrimian.musicplayer.data.repositories.scanner.StoragePlaylistAnalyzer;
import com.github.anrimian.musicplayer.data.repositories.scanner.files.FileScanner;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSourceApi30;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSourceImpl;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor;
import com.github.anrimian.musicplayer.data.storage.source.FileSourceProvider;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

@Module
public class StorageModule {

    @Provides
    @Nonnull
    @Singleton
    StorageMusicProvider storageMusicProvider(Context context, StorageAlbumsProvider albumsProvider) {
        return new StorageMusicProvider(context, albumsProvider);
    }

    @Provides
    @Nonnull
    @Singleton
    StorageGenresProvider storageGenresProvider(Context context) {
        return new StorageGenresProvider(context);
    }

    @Provides
    @Nonnull
    @Singleton
    StorageAlbumsProvider storageAlbumsProvider(Context context) {
        return new StorageAlbumsProvider(context);
    }

    @Provides
    @Nonnull
    @Singleton
    FileSourceProvider fileSourceProvider(Context context) {
        return new FileSourceProvider(context);
    }

    @Provides
    @Nonnull
    @Singleton
    CompositionSourceEditor compositionSourceEditor(StorageMusicProvider musicProvider,
                                                    FileSourceProvider fileSourceProvider) {
        return new CompositionSourceEditor(musicProvider, fileSourceProvider);
    }

    @Provides
    @Nonnull
    @Singleton
    EditorRepository compositionEditorRepository(CompositionSourceEditor sourceEditor,
                                                 StorageFilesDataSource filesDataSource,
                                                 CompositionsDaoWrapper compositionsDao,
                                                 AlbumsDaoWrapper albumsDao,
                                                 ArtistsDaoWrapper artistsDao,
                                                 GenresDaoWrapper genresDao,
                                                 FoldersDaoWrapper foldersDao,
                                                 StorageMusicProvider storageMusicProvider,
                                                 StorageGenresProvider storageGenresProvider,
                                                 StateRepository stateRepository,
                                                 SettingsRepository settingsRepository,
                                                 @Named(DB_SCHEDULER) Scheduler scheduler) {
        return new EditorRepositoryImpl(
                sourceEditor,
                filesDataSource,
                compositionsDao,
                albumsDao,
                artistsDao,
                genresDao,
                foldersDao,
                storageMusicProvider,
                storageGenresProvider,
                stateRepository,
                settingsRepository,
                scheduler);
    }

    @Provides
    @Nonnull
    @Singleton
    StorageFilesDataSource storageFilesDataSource(StorageMusicProvider musicProvider) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new StorageFilesDataSourceApi30(musicProvider);
        }
        return new StorageFilesDataSourceImpl(musicProvider);
    }

    @Provides
    @Nonnull
    EditorInteractor compositionEditorInteractor(EditorRepository editorRepository,
                                                 LibraryRepository musicProviderRepository) {
        return new EditorInteractor(editorRepository, musicProviderRepository);
    }

    @Provides
    @Nonnull
    @Singleton
    FileScanner fileScanner(CompositionsDaoWrapper compositionsDao,
                            CompositionSourceEditor compositionSourceEditor,
                            StateRepository stateRepository,
                            Analytics analytics,
                            @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new FileScanner(compositionsDao, compositionSourceEditor, stateRepository, analytics, scheduler);
    }

    @Provides
    @Nonnull
    @Singleton
    MediaScannerRepository mediaScannerRepository(StorageMusicProvider musicProvider,
                                                  StoragePlayListsProvider playListsProvider,
                                                  StorageGenresProvider genresProvider,
                                                  CompositionsDaoWrapper compositionsDao,
                                                  GenresDaoWrapper genresDao,
                                                  SettingsRepository settingsRepository,
                                                  StorageCompositionAnalyzer compositionAnalyzer,
                                                  StoragePlaylistAnalyzer storagePlaylistAnalyzer,
                                                  FileScanner fileScanner,
                                                  LoggerRepository loggerRepository,
                                                  Analytics analytics,
                                                  @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new MediaScannerRepositoryImpl(musicProvider,
                playListsProvider,
                genresProvider,
                compositionsDao,
                genresDao,
                settingsRepository,
                compositionAnalyzer,
                storagePlaylistAnalyzer,
                fileScanner,
                loggerRepository,
                analytics,
                scheduler);
    }

    @Provides
    @Nonnull
    StorageCompositionAnalyzer compositionAnalyzer(CompositionsDaoWrapper compositionsDao,
                                                   FoldersDaoWrapper foldersDaoWrapper,
                                                   StateRepository stateRepository,
                                                   StorageCompositionsInserter compositionsInserter) {
        return new StorageCompositionAnalyzer(compositionsDao,
                foldersDaoWrapper,
                stateRepository,
                compositionsInserter);
    }

    @Provides
    @Nonnull
    StoragePlaylistAnalyzer storagePlaylistAnalyzer(PlayListsDaoWrapper playListsDao,
                                                    StoragePlayListsProvider playListsProvider) {
        return new StoragePlaylistAnalyzer(playListsDao, playListsProvider);
    }

    @Provides
    @Nonnull
    StorageCompositionsInserter compositionsInserter(AppDatabase appDatabase,
                                                     CompositionsDao compositionsDao,
                                                     CompositionsDaoWrapper compositionsDaoWrapper,
                                                     FoldersDao foldersDao,
                                                     ArtistsDao artistsDao,
                                                     AlbumsDao albumsDao) {
        return new StorageCompositionsInserter(appDatabase,
                compositionsDao,
                compositionsDaoWrapper,
                foldersDao,
                artistsDao,
                albumsDao);
    }

}
