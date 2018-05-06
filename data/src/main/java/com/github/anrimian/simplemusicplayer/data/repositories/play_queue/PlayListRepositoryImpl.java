package com.github.anrimian.simplemusicplayer.data.repositories.play_queue;

import com.github.anrimian.simplemusicplayer.data.database.AppDatabase;
import com.github.anrimian.simplemusicplayer.data.database.dao.CompositionsDao;
import com.github.anrimian.simplemusicplayer.data.database.models.CompositionEntity;
import com.github.anrimian.simplemusicplayer.data.database.models.CompositionItemEntity;
import com.github.anrimian.simplemusicplayer.data.models.compositions.CompositionsMapper;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.CurrentPlayListInfo;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayListRepository;

import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

import static java.util.Arrays.asList;

/**
 * Created on 18.11.2017.
 */

public class PlayListRepositoryImpl implements PlayListRepository {

    private CompositionsDao compositionsDao;
    private Scheduler dbScheduler;

    private CompositionsMapper compositionsMapper = Mappers.getMapper(CompositionsMapper.class);

    public PlayListRepositoryImpl(AppDatabase appDatabase, Scheduler dbScheduler) {
        this.compositionsDao = appDatabase.compositionsDao();
        this.dbScheduler = dbScheduler;
    }

    @Override
    public Completable setCurrentPlayList(CurrentPlayListInfo currentPlayListInfo) {
        return Completable.fromRunnable(() -> {
            compositionsDao.deleteCurrentPlayList();
//            compositionsDao.deleteCompositions();

            List<Composition> initialPlayList = currentPlayListInfo.getInitialPlayList();
            List<Composition> currentPlayList = currentPlayListInfo.getCurrentPlayList();
//            compositionsDao.insertCompositions(compositionsMapper.toCompositionEntityList(initialPlayList));

            /*List<Composition> compositions = currentPlayList.getCompositions();
            for (int i = 0; i < compositions.size(); i++) {
                Composition composition = compositions.get(i);
                CompositionEntity compositionEntity = compositionsMapper.toCompositionEntity(composition);
                CompositionItemEntity compositionItemEntity = new CompositionItemEntity();
                compositionItemEntity.setComposition(compositionEntity);
                compositionItemEntity.setInitialPosition(i);
                compositionItemEntity.setShuffledPosition(currentPlayList.getPositionForIndex(i));
                compositionsDao.setPlayQueue(compositionItemEntity);
            }*/

            List<CompositionItemEntity> itemEntities = new ArrayList<>();
            for (int i = 0; i < initialPlayList.size(); i++) {
                Composition composition = initialPlayList.get(i);
                CompositionEntity compositionEntity = compositionsMapper.toCompositionEntity(composition);
                CompositionItemEntity compositionItemEntity = new CompositionItemEntity();
                compositionItemEntity.setComposition(compositionEntity);
                compositionItemEntity.setInitialPosition(initialPlayList.indexOf(composition));
                compositionItemEntity.setShuffledPosition(currentPlayList.indexOf(composition));
                itemEntities.add(compositionItemEntity);
            }
            compositionsDao.setCurrentPlayList(itemEntities);
        }).subscribeOn(dbScheduler);
    }

    @Override
    public Single<CurrentPlayListInfo> getCurrentPlayList() {
        return Single.fromCallable(() -> {
            List<CompositionItemEntity> compositionItemEntities = compositionsDao.getCurrentPlayList();
            Composition[] initialPlayListArray = new Composition[compositionItemEntities.size()];
            Composition[] currentPlayListArray = new Composition[compositionItemEntities.size()];
            for (CompositionItemEntity compositionItem: compositionItemEntities) {
                Composition composition = compositionsMapper.toComposition(compositionItem.getComposition());
                initialPlayListArray[compositionItem.getInitialPosition()] = composition;
                currentPlayListArray[compositionItem.getShuffledPosition()] = composition;
            }
            List<Composition> initialPlayList = asList(initialPlayListArray);
            List<Composition> currentPlayList = asList(currentPlayListArray);

            return new CurrentPlayListInfo(initialPlayList, currentPlayList);
        }).subscribeOn(dbScheduler);
    }
}