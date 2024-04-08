package com.example.flushhubproto.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flushhubproto.LocationInfo

class HomeViewModel : ViewModel() {
    private val _locationInfos = MutableLiveData<List<LocationInfo>>()
    val locationInfos: LiveData<List<LocationInfo>> = _locationInfos


    init {

        _locationInfos.value = listOf(
            LocationInfo("621 Commonwealth Ave, Boston, MA 02215", "0.2 miles", "4 mins"),
            LocationInfo("156 Bay State Rd Boston, MA 02215", "0.2 miles", "5 mins")

        )
    }

}