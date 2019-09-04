package com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewPositionItemClickListener;

import java.util.List;

/**
 * Created on 31.10.2017.
 */

public class PlayListItemAdapter extends RecyclerView.Adapter<PlayListItemViewHolder> {

    private final boolean coversEnabled;

    private List<PlayListItem> musicList;
    private OnItemClickListener<Integer> onCompositionClickListener;
    private OnViewPositionItemClickListener<PlayListItem> onMenuItemClickListener;
    private OnItemClickListener<Integer> onIconClickListener;

    public PlayListItemAdapter(List<PlayListItem> musicList, boolean coversEnabled) {
        this.musicList = musicList;
        this.coversEnabled = coversEnabled;
    }

    @NonNull
    @Override
    public PlayListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayListItemViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                onCompositionClickListener,
                onMenuItemClickListener,
                onIconClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListItemViewHolder holder, int position) {
        PlayListItem composition = musicList.get(position);
        holder.bind(composition, coversEnabled);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public void setItems(List<PlayListItem> list) {
        musicList = list;
    }

    public void setOnCompositionClickListener(OnItemClickListener<Integer> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setOnMenuItemClickListener(OnViewPositionItemClickListener<PlayListItem> onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    public void setOnIconClickListener(OnItemClickListener<Integer> onIconClickListener) {
        this.onIconClickListener = onIconClickListener;
    }
}
