package com.github.anrimian.musicplayer.data.storage.providers.music;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 24.10.2017.
 */

public class StorageFullComposition {

    @Nullable
    private final String artist;
    @Nullable
    private final String title;
    @Nonnull
    private final String filePath;

    private final long duration;
    private final long size;
    private final long id;

    @Nonnull
    private final Date dateAdded;
    @Nonnull
    private final Date dateModified;

    private final StorageAlbum storageAlbum;

    public StorageFullComposition(@Nullable String artist,
                                  @Nullable String title,
                                  @Nonnull String filePath,
                                  long duration,
                                  long size,
                                  long id,
                                  @Nonnull Date dateAdded,
                                  @Nonnull Date dateModified,
                                  StorageAlbum storageAlbum) {
        this.artist = artist;
        this.title = title;
        this.filePath = filePath;
        this.duration = duration;
        this.size = size;
        this.id = id;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.storageAlbum = storageAlbum;
    }

    public StorageAlbum getStorageAlbum() {
        return storageAlbum;
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nonnull
    public String getFilePath() {
        return filePath;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public long getId() {
        return id;
    }

    @Nonnull
    public Date getDateAdded() {
        return dateAdded;
    }

    @Nonnull
    public Date getDateModified() {
        return dateModified;
    }

    @NonNull
    @Override
    public String toString() {
        return "Composition{" +
                "\n id=" + id +
                "\n filePath='" + filePath + '\'' +
                "\n duration=" + duration +
                "\n size=" + size +
                "\n dateAdded=" + dateAdded +
                "\n dateModified=" + dateModified +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageFullComposition that = (StorageFullComposition) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
