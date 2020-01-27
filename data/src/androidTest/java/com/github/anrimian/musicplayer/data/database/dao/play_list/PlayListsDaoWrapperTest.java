package com.github.anrimian.musicplayer.data.database.dao.play_list;

import android.content.Context;

import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

public class PlayListsDaoWrapperTest {

    private PlayListDao playListDao;
    private CompositionsDao compositionsDao;
    private AppDatabase db;

    private PlayListsDaoWrapper daoWrapper;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        compositionsDao = db.compositionsDao();
        playListDao = db.playListDao();

        daoWrapper = new PlayListsDaoWrapper(playListDao, compositionsDao, db);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testModifyChangeWithDuplicates() {
        StoragePlayList playList1 = new StoragePlayList(1L,
                "test",
                new Date(),
                new Date());
        StoragePlayList playList2 = new StoragePlayList(2L,
                "test1",
                new Date(),
                new Date());
        daoWrapper.applyChanges(asList(playList1, playList2), Collections.emptyList());
        StoragePlayList duplicatePlayList = new StoragePlayList(2L,
                "test",
                new Date(),
                new Date());
        daoWrapper.applyChanges(Collections.emptyList(), asList(duplicatePlayList));

    }
}