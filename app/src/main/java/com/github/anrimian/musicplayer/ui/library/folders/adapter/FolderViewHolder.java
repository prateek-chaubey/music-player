package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import static com.github.anrimian.musicplayer.domain.Payloads.FILES_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_SELECTED;
import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_UNSELECTED;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionsCount;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateBackgroundColor;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onLongClick;

import android.graphics.Color;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ItemStorageFolderBinding;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;

import java.util.List;

import javax.annotation.Nonnull;


/**
 * Created on 31.10.2017.
 */

class FolderViewHolder extends FileViewHolder {

    private final ItemStorageFolderBinding viewBinding;

    private FolderFileSource folder;
    private String path;

    private boolean selected = false;

    FolderViewHolder(ViewGroup parent,
                     OnPositionItemClickListener<FolderFileSource> onFolderClickListener,
                     OnViewItemClickListener<FolderFileSource> onMenuClickListener,
                     OnPositionItemClickListener<FileSource> onLongClickListener) {
        super(parent, R.layout.item_storage_folder);
        viewBinding = ItemStorageFolderBinding.bind(itemView);

        if (onFolderClickListener != null) {
            viewBinding.clickableItem.setOnClickListener(v ->
                    onFolderClickListener.onItemClick(getBindingAdapterPosition(), folder)
            );
        }
        if (onLongClickListener != null) {
            onLongClick(viewBinding.clickableItem,  () -> {
                if (selected) {
                    return;
                }
                selectImmediate();
                onLongClickListener.onItemClick(getBindingAdapterPosition(), folder);
            });
        }
        viewBinding.btnActionsMenu.setOnClickListener(v -> onMenuClickListener.onItemClick(v, folder));
    }

    @Override
    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            int unselectedColor = Color.TRANSPARENT;
            int selectedColor = getSelectionColor();
            int endColor = selected ? selectedColor : unselectedColor;
            animateBackgroundColor(viewBinding.clickableItem, endColor);
        }
    }

    @Override
    public void setSelectedToMove(boolean selected) {
        int unselectedColor = Color.TRANSPARENT;
        int selectedColor = getMoveSelectionColor();
        int endColor = selected ? selectedColor : unselectedColor;
        animateBackgroundColor(itemView, endColor);
    }

    @Override
    public FileSource getFileSource() {
        return folder;
    }

    void bind(@Nonnull FolderFileSource folderFileSource) {
        this.folder = folderFileSource;
        this.path = folderFileSource.getName();
        showFolderName();
        showFilesCount();
    }

    public void update(FolderFileSource folderFileSource, List<Object> payloads) {
        this.folder = folderFileSource;
        this.path = folderFileSource.getName();
        bind(folderFileSource);
        for (Object payload: payloads) {
            if (payload instanceof List) {
                //noinspection SingleStatementInBlock,unchecked
                update(folderFileSource, (List) payload);
            }
            if (payload == ITEM_SELECTED) {
                setSelected(true);
                return;
            }
            if (payload == ITEM_UNSELECTED) {
                setSelected(false);
                return;
            }
            if (payload == NAME) {
                showFolderName();
            }
            if (payload == FILES_COUNT) {
                showFilesCount();
            }
        }
    }

    private void showFilesCount() {
        String text = formatCompositionsCount(getContext(), folder.getFilesCount());
        viewBinding.tvCompositionsCount.setText(text);
    }

    private void showFolderName() {
//        String displayPath = getFileName(path);
        viewBinding.tvFolderName.setText(path);
    }

    private void selectImmediate() {
        viewBinding.clickableItem.setBackgroundColor(getSelectionColor());
        selected = true;
    }
}
