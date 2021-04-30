package com.example.fitnessapp.ui.tracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TrackerViewModel : ViewModel() {

    val state = MutableLiveData<Int>().apply {
        value = RUN_MODE
    }

    companion object {
        const val BIKE_MODE = 0
        const val RUN_MODE = 1
    }
}