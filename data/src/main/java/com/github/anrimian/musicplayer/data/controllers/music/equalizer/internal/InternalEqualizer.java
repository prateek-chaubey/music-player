package com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal;

import android.media.audiofx.Equalizer;
import android.util.Log;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.AppEqualizer;
import com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.repositories.equalizer.EqualizerStateRepository.NO_PRESET;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;

//save only when band seekbar was released(?)
//play -> enable eq = eq not attached - works?
//bands number depends on device: use horizontal seek bar
//unite internal equalizer screen with equalizer setup dialog. Use bottom sheet dialog?
//check sound gap on eq start
//remove selected preset displaying? And saving at all
//display correct band values
//
public class InternalEqualizer implements AppEqualizer {

    private final EqualizerStateRepository equalizerStateRepository;

    private final BehaviorSubject<EqualizerState> currentStateSubject = BehaviorSubject.create();

    private Equalizer equalizer;

    public InternalEqualizer(EqualizerStateRepository equalizerStateRepository) {
        this.equalizerStateRepository = equalizerStateRepository;
    }

    @Override
    public void attachEqualizer(int audioSessionId) {
        if (audioSessionId != 0) {
            Log.d("KEK", "attachEqualizer, audioSessionId: " + audioSessionId);
            if (equalizer == null) {
                equalizer = new Equalizer(1000, audioSessionId);

                EqualizerState equalizerState = equalizerStateRepository.loadEqualizerState();
                Log.d("KEK", "attachEqualizer, loadEqualizerState: " + equalizerState);
                if (equalizerState != null) {
                    applyEqualizerState(equalizer, equalizerState);
                    currentStateSubject.onNext(equalizerState);
                } else {
                    currentStateSubject.onNext(extractEqualizerState(equalizer));
                }
            }

            equalizer.setEnabled(true);
        }
    }

    @Override
    public void detachEqualizer(int audioSessionId) {
        if (equalizer != null) {
            equalizer.setEnabled(false);
        }
    }

    public Single<EqualizerConfig> getEqualizerConfig() {
        return Single.fromCallable(() -> {
            Equalizer tempEqualizer = new Equalizer(0, 1);
            EqualizerConfig config = extractEqualizerInfo(tempEqualizer);
            tempEqualizer.release();
            return config;
        });
    }

    public Observable<EqualizerState> getEqualizerStateObservable() {
        return withDefaultValue(currentStateSubject, () -> {
            EqualizerState equalizerState = equalizerStateRepository.loadEqualizerState();
            if (equalizerState == null) {
                Equalizer tempEqualizer = new Equalizer(0, 1);
                equalizerState = extractEqualizerState(tempEqualizer);
                tempEqualizer.release();
            }
            return equalizerState;
        });
    }

    public void setBandLevel(short bandNumber, short level) {
        if (equalizer != null) {
            equalizer.setBandLevel(bandNumber, level);
        }

        EqualizerState equalizerState = equalizerStateRepository.loadEqualizerState();
        if (equalizerState == null) {
            equalizerState = new EqualizerState(NO_PRESET, new HashMap<>());
        }
        equalizerState.getBendLevels().put(bandNumber, level);
        equalizerState.setCurrentPreset(NO_PRESET);
        Log.d("KEK", "setBandLevel, saveEqualizerState: " + equalizerState);
        equalizerStateRepository.saveEqualizerState(equalizerState);
        currentStateSubject.onNext(equalizerState);
    }

    public void setPreset(Preset preset) {
        if (equalizer == null) {
            Equalizer tempEqualizer = new Equalizer(0, 1);
            tempEqualizer.usePreset(preset.getNumber());
            EqualizerState equalizerState = extractEqualizerState(tempEqualizer);
            tempEqualizer.release();

            Log.d("KEK", "setPreset, saveEqualizerState(from tmp eq): " + equalizerState);
            equalizerStateRepository.saveEqualizerState(equalizerState);
            currentStateSubject.onNext(equalizerState);
        } else {
            if (preset.getNumber() != equalizer.getCurrentPreset()
                    && preset.getNumber() <= equalizer.getNumberOfPresets()) {
                equalizer.usePreset(preset.getNumber());
                EqualizerState equalizerState = extractEqualizerState(equalizer);

                Log.d("KEK", "setPreset, saveEqualizerState: " + equalizerState);
                equalizerStateRepository.saveEqualizerState(equalizerState);
                currentStateSubject.onNext(equalizerState);
            }
        }
    }

    private void applyEqualizerState(Equalizer equalizer, EqualizerState equalizerState) {
        for (Map.Entry<Short, Short> band: equalizerState.getBendLevels().entrySet()) {
            equalizer.setBandLevel(band.getKey(), band.getValue());
        }
    }

    private EqualizerState extractEqualizerState(Equalizer equalizer) {
        Map<Short, Short> maps = new HashMap<>();
        for(short i = 0; i < equalizer.getNumberOfBands(); i++) {
            maps.put(i, equalizer.getBandLevel(i));
        }
        return new EqualizerState(
                equalizer.getCurrentPreset(),
                maps
        );
    }

    private EqualizerConfig extractEqualizerInfo(Equalizer equalizer) {
        short[] bandLevelRange = equalizer.getBandLevelRange();

        short lowestRange = bandLevelRange[0];
        short highestRange = bandLevelRange[1];

        List<Band> bands = new ArrayList<>();
        for(short i = 0; i < equalizer.getNumberOfBands(); i++) {
            bands.add(new Band(
                    i,
                    equalizer.getBandFreqRange(i),
                    equalizer.getCenterFreq(i))
            );
        }

        List<Preset> presets = new ArrayList<>();
        for(short i = 0; i < equalizer.getNumberOfPresets(); i++) {
            Preset preset = new Preset(
                    i,
                    equalizer.getPresetName(i)
            );
            presets.add(preset);
        }

        return new EqualizerConfig(lowestRange, highestRange, bands, presets);
    }

}