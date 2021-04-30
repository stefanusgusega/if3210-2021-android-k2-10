package com.example.fitnessapp.ui.trackerinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.fitnessapp.storage.FitnessRepository
import com.example.fitnessapp.storage.run.Run

class TrackerInfoViewModel(private val repository: FitnessRepository, private val runId: Int) : ViewModel() {
    // TODO: Implement the ViewModel

    val runInfo: LiveData<Run> = repository.runDao.get(runId).asLiveData()
}

class TrackerInfoViewModelFactory(private val repository: FitnessRepository, private val runId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackerInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackerInfoViewModel(repository, runId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}