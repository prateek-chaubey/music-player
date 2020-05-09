package com.github.anrimian.musicplayer.ui.library.albums.list

import com.github.anrimian.musicplayer.data.utils.rx.RxUtils
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryAlbumsInteractor
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvp.AppPresenter
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.util.*

class AlbumsListPresenter(private val interactor: LibraryAlbumsInteractor,
                          errorParser: ErrorParser,
                          uiScheduler: Scheduler) 
    : AppPresenter<AlbumsListView>(uiScheduler, errorParser) {
    
    private var albumsDisposable: Disposable? = null
    
    private var albums: List<Album> = ArrayList()
    
    private var searchText: String? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeOnAlbumsList()
    }

    fun onTryAgainLoadCompositionsClicked() {
        subscribeOnAlbumsList()
    }

    fun onOrderMenuItemClicked() {
        viewState.showSelectOrderScreen(interactor.order)
    }

    fun onOrderSelected(order: Order?) {
        interactor.order = order
    }

    fun onSearchTextChanged(text: String?) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text
            subscribeOnAlbumsList()
        }
    }

    fun getSearchText() = searchText

    private fun subscribeOnAlbumsList() {
        if (albums.isEmpty()) {
            viewState.showLoading()
        }
        RxUtils.dispose(albumsDisposable, presenterDisposable)
        albumsDisposable = interactor.getAlbumsObservable(searchText)
                .observeOn(uiScheduler)
                .subscribe(this::onAlbumsReceived, this::onAlbumsReceivingError)
        presenterDisposable.add(albumsDisposable!!)
    }

    private fun onAlbumsReceivingError(throwable: Throwable) {
        val errorCommand = errorParser.parseError(throwable)
        viewState.showLoadingError(errorCommand)
    }

    private fun onAlbumsReceived(albums: List<Album>) {
        this.albums = albums
        viewState.submitList(albums)
        if (albums.isEmpty()) {
            if (TextUtils.isEmpty(searchText)) {
                viewState.showEmptyList()
            } else {
                viewState.showEmptySearchResult()
            }
        } else {
            viewState.showList()
        }
    }

}