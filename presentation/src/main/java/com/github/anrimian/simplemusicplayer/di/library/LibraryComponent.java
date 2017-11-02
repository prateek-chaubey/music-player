package com.github.anrimian.simplemusicplayer.di.library;

import com.github.anrimian.simplemusicplayer.di.library.storage.StorageLibraryComponent;
import com.github.anrimian.simplemusicplayer.di.library.storage.StorageLibraryModule;
import com.github.anrimian.simplemusicplayer.ui.library.main.LibraryPresenter;

import dagger.Subcomponent;

/**
 * Created on 29.10.2017.
 */

@LibraryScope
@Subcomponent(modules = LibraryModule.class)
public interface LibraryComponent {

    StorageLibraryComponent storageLibraryComponent(StorageLibraryModule storageLibraryModule);

    LibraryPresenter libraryPresenter();
}
