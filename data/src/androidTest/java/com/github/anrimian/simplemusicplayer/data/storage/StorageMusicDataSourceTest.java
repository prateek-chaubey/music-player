package com.github.anrimian.simplemusicplayer.data.storage;

import android.Manifest;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class StorageMusicDataSourceTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    private StorageMusicDataSource storageMusicDataSource;

    @Before
    public void before() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        storageMusicDataSource = new StorageMusicDataSource(appContext);
    }

    @Test
    public void testRepositoryReturnValues() {
        List<Composition> compositions = storageMusicDataSource.getAllCompositions().blockingGet();
        for (Composition composition: compositions) {
            System.out.println(composition);
            Assert.assertNotNull(composition.getFilePath());
        }
    }

}