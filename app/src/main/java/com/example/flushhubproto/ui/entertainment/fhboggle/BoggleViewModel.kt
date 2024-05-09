package com.example.flushhubproto.ui.entertainment.fhboggle

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.max

class BoggleViewModel : ViewModel() {
    val submittedWord = MutableLiveData<String>()
    val score = MutableLiveData<Int>().apply { value = 0 }
    var resetGameEvent = MutableLiveData<Boolean>()

    fun updateScore(newScore: Int) {
        score.postValue(max(0, (score.value ?: 0)+ newScore))
    }

    private fun resetScore() {
        score.value = 0
        score.postValue(0)
    }

    fun triggerGameReset() {
        resetGameEvent.value = true
        resetGameEvent.postValue(true)
        resetScore()
    }
}