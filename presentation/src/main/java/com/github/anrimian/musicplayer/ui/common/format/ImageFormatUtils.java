package com.github.anrimian.musicplayer.ui.common.format;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ImageView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.images.ImageCache;
import com.github.anrimian.musicplayer.ui.utils.ImageUtils;

import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ImageFormatUtils {

    private static final WeakHashMap<ImageView, Disposable> imageLoadingMap = new WeakHashMap<>();

    public static void displayImage(@NonNull ImageView imageView,
                                    @NonNull Composition composition) {
        imageView.setImageResource(R.drawable.ic_music_placeholder);
        Disposable disposable = imageLoadingMap.get(imageView);
        if (disposable != null) {
            disposable.dispose();
        }
        disposable = Single.fromCallable(() -> getCompositionImageOrThrow(composition))
                .timeout(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageView::setImageBitmap,
                        t -> imageView.setImageResource(R.drawable.ic_music_placeholder));
        imageLoadingMap.put(imageView, disposable);
    }

    @Nonnull
    private static Bitmap getCompositionImageOrThrow(Composition composition) {
        Bitmap bitmap = getCompositionImage(composition);
        if (bitmap == null) {
            throw new RuntimeException("composition image not found");
        }
        return bitmap;
    }

    @Nullable
    public static Bitmap getCompositionImage(Composition composition) {
        ImageCache imageCache = ImageCache.getInstance();
        Bitmap bitmap = imageCache.getBitmap(composition.getId());
        if (bitmap == null) {
            synchronized (ImageCache.class) {
                bitmap = imageCache.getBitmap(composition.getId());
                if (bitmap == null) {
                    bitmap = extractImageComposition(composition);
                    if (bitmap != null) {
                        imageCache.putBitmap(composition.getId(), bitmap);
                    }
                }
            }
        }
        return bitmap;
    }

    @Nullable
    private static Bitmap extractImageComposition(Composition composition) {
        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(composition.getFilePath());
            byte[] imageBytes = mmr.getEmbeddedPicture();
            mmr.release();
            if (imageBytes == null) {
                return null;
            }
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }
    }
}
