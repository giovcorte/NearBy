package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.nearbyapp.nearby.components.ResponseWrapper
import com.nearbyapp.nearby.model.TextWrapper
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.repository.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedDetailViewModel(application: Application): AbstractDetailViewModel(application) {

    override fun loadDetails(id: String, dataSource: DataSource) {
        viewModelScope.launch {
            when(val response = repository.getPlaceDetail(id, dataSource)) {
                is ResponseWrapper.Success -> {
                    withContext(Dispatchers.Main) {
                        response.value?.detail?.let {
                            val detailsList: MutableList<Identifiable?> = mutableListOf()
                            detailsList.add(getImages(it.photos?.filter { photo ->
                                photo.id != null
                            }))
                            detailsList.add(it)
                            detailsList.add(getReviews(it))
                            detailsList.add(it.opening_hours)
                            details.postValue(detailsList.filterNotNull())
                        }
                    }
                }
                else -> {
                    details.postValue(mutableListOf(TextWrapper("Nessun dettaglio salvato")))
                }
            }
        }
    }
}