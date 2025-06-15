package com.example.myapplication.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class MainScreen {

    @Composable
     fun ActiveScreen() {
        Text("Active Screen Content")
    }

    @Composable
    fun UpcomingScreen() {
        Text("Upcoming Screen Content")
    }

    @Composable
    fun PastScreen() {
        Text("Past Screen Content")
    }


}