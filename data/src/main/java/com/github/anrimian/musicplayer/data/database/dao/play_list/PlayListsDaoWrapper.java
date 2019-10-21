package com.github.anrimian.musicplayer.data.database.dao.play_list;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryDto;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListPojo;
import com.github.anrimian.musicplayer.data.database.entities.playlist.RawPlayListItem;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionMapper;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

public class PlayListsDaoWrapper {

    private final PlayListDao playListDao;
    private final AppDatabase appDatabase;

    public PlayListsDaoWrapper(PlayListDao playListDao, AppDatabase appDatabase) {
        this.playListDao = playListDao;
        this.appDatabase = appDatabase;
    }

    public void applyChanges(List<StoragePlayList> addedPlayLists,
                             List<StoragePlayList> changedPlayLists) {
        appDatabase.runInTransaction(() -> {
            playListDao.insertPlayListEntities(mapList(addedPlayLists, this::toEntity));
            for (StoragePlayList playList: changedPlayLists) {
                playListDao.updatePlayListModifyTimeByStorageId(playList.getId(), playList.getDateModified());
                playListDao.updatePlayListNameByStorageId(playList.getId(), playList.getName());
            }
        });
    }

    public PlayList insertPlayList(StoragePlayList playList) {
        PlayListEntity entity = new PlayListEntity(
                playList.getId(),
                playList.getName(),
                playList.getDateAdded(),
                playList.getDateModified());
        long id = playListDao.insertPlayListEntity(entity);
        if (id == -1) {
            throw new IllegalStateException("db not modified");
        }
        return new PlayList(id,
                playList.getId(),
                playList.getName(),
                playList.getDateAdded(),
                playList.getDateModified(),
                0,
                0);
    }

    public List<StoragePlayList> getAllAsStoragePlayLists() {
        return playListDao.getAllAsStoragePlayLists();
    }

    public void deletePlayList(long id) {
        playListDao.deletePlayList(id);
    }

    public void updatePlayListName(long id, String name) {
        playListDao.updatePlayListName(id, name);
    }

    public void updatePlayListModifyTime(long id, Date modifyTime) {
        playListDao.updatePlayListModifyTime(id, modifyTime);
    }

    public Observable<List<PlayList>> getPlayListsObservable() {
        return playListDao.getPlayListsObservable()
                .map(entities -> mapList(entities, this::toPlayList));
    }

    public List<IdPair> getPlayListsIds() {
        return playListDao.getPlayListsIds();
    }

    public Observable<PlayList> getPlayListsObservable(long id) {
        return playListDao.getPlayListObservable(id)
                .takeWhile(entities -> !entities.isEmpty())
                .map(entities -> toPlayList(entities.get(0)));
    }

    public Observable<List<PlayListItem>> getPlayListItemsObservable(long playListId) {
        return playListDao.getPlayListItemsObservable(playListId)
                .map(entities -> mapList(entities, this::toItem));
    }

    public List<StoragePlayListItem> getPlayListItemsAsStorageItems(long playlistId) {
        return playListDao.getPlayListItemsAsStorageItems(playlistId);
    }

    public void deletePlayListEntry(long id, long playListId) {
        appDatabase.runInTransaction(() -> {
            int position = playListDao.selectPositionById(id);
            playListDao.deletePlayListEntry(id);
            playListDao.decreasePositionsAfter(position, playListId);
        });
    }

    public void insertPlayListItems(List<RawPlayListItem> items,
                                    long playListId,
                                    @Nullable Long storagePlayListId) {
        insertPlayListItems(items,
                playListId,
                storagePlayListId,
                playListDao.selectMaxOrder(playListId) + 1
        );
    }

    public void insertPlayListItems(List<RawPlayListItem> items,
                                    long playListId,
                                    @Nullable Long storagePlayListId,
                                    int position) {
        appDatabase.runInTransaction(() -> {
            playListDao.increasePositionsByCountAfter(items.size(), position, playListId);

            List<PlayListEntryEntity> entities = new ArrayList<>(items.size());
            int orderPosition = position;
            for (RawPlayListItem item : items) {
                PlayListEntryEntity entryEntity = new PlayListEntryEntity(
                        item.getStorageItemId(),
                        storagePlayListId,
                        item.getAudioId(),
                        playListId,
                        orderPosition++
                );
                entities.add(entryEntity);
            }
            playListDao.insertPlayListEntries(entities);
        });
    }

    public Long selectStorageId(long id) {
        return playListDao.selectStorageId(id);
    }

    public void moveItems(long playListId, int fromPos, int toPos) {
        playListDao.moveItems(playListId, fromPos, toPos);
    }

    private PlayListItem toItem(PlayListEntryDto entryDto) {
        return new PlayListItem(entryDto.getItemId(),
                entryDto.getStorageItemId(),
                CompositionMapper.toComposition(entryDto.getComposition()));
    }

    private PlayList toPlayList(PlayListPojo pojo) {
        return new PlayList(pojo.getId(),
                pojo.getStorageId(),
                pojo.getName(),
                pojo.getDateAdded(),
                pojo.getDateModified(),
                pojo.getCompositionsCount(),
                pojo.getTotalDuration());
    }

    private PlayListEntity toEntity(StoragePlayList storagePlayList) {
        return new PlayListEntity(storagePlayList.getId(),
                storagePlayList.getName(),
                storagePlayList.getDateAdded(),
                storagePlayList.getDateModified());
    }
}