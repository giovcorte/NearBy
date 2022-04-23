package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.databinding.databinding.IData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedDetailViewModel(application: Application): AbstractDetailViewModel(application) {

    override fun loadDetails(id: String) {
        viewModelScope.launch {
            val detail = repository.getStoredPlace(id)
            withContext(Dispatchers.Main) {
                detail?.let {
                    val details: MutableList<IData?> = mutableListOf()
                    details.add(getImages(it.photos?.filter { photo ->
                        photo.id != null
                    }))
                    details.add(it)
                    details.add(getReviews(it))
                    details.add(it.opening_hours)
                    this@SavedDetailViewModel.details.postValue(details.filterNotNull().toMutableList())
                }
            }
        }
    }
}