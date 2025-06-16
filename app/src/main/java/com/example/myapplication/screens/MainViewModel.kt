package com.example.myapplication.screens

// ... (other imports: ViewModel, StateFlow, SharingStarted, stateIn, viewModelScope)
import android.util.Log
import com.example.myapplication.database.ActiveItem
import com.example.myapplication.database.EventDB // Assuming EventDB is accessible
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Ensure this is imported

class MainViewModel : ViewModel() { // Removed context if EventDB doesn't need it for init

    private val eventDb = EventDB() // Instance of your EventDB

    val allActiveItems: StateFlow<List<ActiveItem>> =
        eventDb.getAllActiveItemsFlow() // This is now a Flow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000), // Keep subscribed for 5s after last collector
                initialValue = emptyList() // Initial value while waiting for the first emission
            )

    fun addItem(item:ActiveItem) {
        eventDb.addActiveItem(item, {
            Log.d("MainViewModel", "Item added successfully")
        }) {

        }

    }
}