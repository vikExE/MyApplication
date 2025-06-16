package com.example.myapplication.database // Or a more appropriate package like com.example.myapplication.models

import java.util.Date


data class ActiveItem(
    var id: String = "",
    var title: String = "",
    var startDate: Long? = null,
    var endDate: Long? = null,
    var address: String = "",
    var description: String = "",
    var imageUrl: String? = null
) {

    fun getStartDateAsDate(): Date? {
        return startDate?.let { Date(it) }
    }

    fun getEndDateAsDate(): Date? {
        return endDate?.let { Date(it) }
    }
}
