package com.github.anrimian.simplemusicplayer.domain.business.music;

import com.github.anrimian.simplemusicplayer.domain.business.music.utils.FakeMusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.exceptions.FileNodeNotFoundException;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.FileTree;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.visitors.CollectVisitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.observers.TestObserver;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Created on 25.10.2017.
 */
public class StorageLibraryInteractorTest {

    private MusicProviderRepository musicProviderRepository;
    private StorageLibraryInteractor storageLibraryInteractor;

    private List<Composition> fakeCompositions = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        Composition one = new Composition();
        one.setFilePath("root/music/one");
        fakeCompositions.add(one);

        Composition two = new Composition();
        two.setFilePath("root/music/two");
        fakeCompositions.add(two);

        Composition three = new Composition();
        three.setFilePath("root/music/old/three");
        fakeCompositions.add(three);

        Composition four = new Composition();
        four.setFilePath("root/music/old/to delete/four");
        fakeCompositions.add(four);

        musicProviderRepository = new FakeMusicProviderRepository(fakeCompositions);
        storageLibraryInteractor = new StorageLibraryInteractorImpl(musicProviderRepository);
    }

    @Test
    public void testFullTreeRootFilter() {
        List<MusicFileSource> collection = getListMusicListInPath(null);
        assertEquals(3, collection.size());
        MusicFileSource old = collection.get(0);
        assertEquals("old", old.getPath());

        String path = old.getPath();
        List<MusicFileSource> oldCollection = getListMusicListInPath(path);
        MusicFileSource toDelete = oldCollection.get(0);
        assertEquals("to delete", toDelete.getPath());

        path += "/" + toDelete.getPath();
        List<MusicFileSource> toDeleteCollection = getListMusicListInPath(path);
        assertEquals("four", toDeleteCollection.get(0).getPath());
        assertEquals("root/music/old/to delete/four", toDeleteCollection.get(0).getComposition().getFilePath());
    }

    @Test
    public void testPathTreeFilter() {
        List<MusicFileSource> collection = getListMusicListInPath("old");
        testCorrectCollection(collection);
        assertEquals(2, collection.size());
        assertNotNull(collection.get(1).getComposition());
        assertNull(collection.get(0).getComposition());
    }

    @Test
    public void testLastItemTreeFilter() {
        List<MusicFileSource> collection = getListMusicListInPath("old/to delete");
        assertEquals(1, collection.size());
        assertEquals("four", collection.get(0).getPath());
        assertEquals("root/music/old/to delete/four", collection.get(0).getComposition().getFilePath());
    }

    @Test
    public void testItemNotFoundTreeFilter() {
        TestObserver<List<MusicFileSource>> subscriber = new TestObserver<>();

        storageLibraryInteractor.getMusicInPath("root/music/unknown")
                .subscribe(subscriber);

        //noinspection unchecked
        subscriber.assertFailure(FileNodeNotFoundException.class);
    }

    private void testCorrectCollection(List<MusicFileSource> collection) {
        for (MusicFileSource musicFileSource: collection) {
            assertNotNull(musicFileSource.getPath());
        }
    }

    private List<MusicFileSource> getListMusicListInPath(String path) {
        return storageLibraryInteractor.getMusicInPath(path).blockingGet();
    }

    @After
    public void tearDown() throws Exception {

    }

}