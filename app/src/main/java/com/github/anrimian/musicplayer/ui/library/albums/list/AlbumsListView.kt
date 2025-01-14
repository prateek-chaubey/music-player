package com.github.anrimian.musicplayer.ui.library.albums.list

import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy
import moxy.MvpView
import moxy.viewstate.strategy.StateStrategyType
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.OneExecution

private const val LIST_STATE = "list_state"

interface AlbumsListView : MvpView {

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showEmptyList()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showEmptySearchResult()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showList()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showLoading()

    @StateStrategyType(value = SingleStateByTagStrategy::class, tag = LIST_STATE)
    fun showLoadingError(errorCommand: ErrorCommand?)

    @AddToEndSingle
    fun submitList(albums: List<Album>)

    @OneExecution
    fun showErrorMessage(errorCommand: ErrorCommand)

    @OneExecution
    fun showSelectOrderScreen(order: Order)

    @OneExecution
    fun restoreListPosition(listPosition: ListPosition)

}