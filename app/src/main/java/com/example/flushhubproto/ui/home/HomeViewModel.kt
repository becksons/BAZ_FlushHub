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
            LocationInfo("156 Bay State Rd Boston, MA 02215", "0.2 miles", "5 mins"),
            LocationInfo("1 Silber Way, Boston, MA 02215", "0.3 miles", "6 mins"),
            LocationInfo("775 Commonwealth Ave, Boston, MA 02215", "0.1 miles", "2 mins"),
            LocationInfo("100 Bay State Rd, Boston, MA 02215", "0.4 miles", "7 mins"),
            LocationInfo("808 Commonwealth Ave, Boston, MA 02215", "0.5 miles", "9 mins")
        )
    }
}
