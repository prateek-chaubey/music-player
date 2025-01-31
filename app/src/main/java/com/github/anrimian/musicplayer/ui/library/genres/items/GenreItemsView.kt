package com.github.anrimian.musicplayer.ui.library.genres.items

import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.Skip

private const val RENAME_STATE = "rename_state"

interface GenreItemsView : BaseLibraryCompositionsView {

    @AddToEndSingle
    fun showGenreInfo(genre: Genre)

    @OneExecution
    fun closeScreen()

    @Skip
    fun showRenameGenreDialog(genre: Genre)

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = RENAME_STATE)
    fun showRenameProgress()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = RENAME_STATE)
    fun hideRenameProgress()

}