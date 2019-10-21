package com.github.anrimian.musicplayer.data.database.mappers;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;

public class CompositionMapper {

    public static Composition toComposition(CompositionEntity entity) {
        return new Composition(entity.getArtist(),
                entity.getTitle(),
                entity.getAlbum(),
                entity.getFilePath(),
                entity.getDuration(),
                entity.getSize(),
                entity.getId(),
                entity.getStorageId(),
                entity.getDateAdded(),
                entity.getDateModified(),
                entity.getCorruptionType());
    }

    public static CompositionEntity toEntity(StorageComposition composition) {
        CorruptionType corruptionType = null;
        if (composition.getDuration() == 0) {
            corruptionType = CorruptionType.UNKNOWN;
        }

        return new CompositionEntity(composition.getArtist(),
                composition.getTitle(),
                composition.getAlbum(),
                composition.getFilePath(),
                composition.getDuration(),
                composition.getSize(),
                composition.getId(),
                composition.getDateAdded(),
                composition.getDateModified(),
                corruptionType);
    }
}