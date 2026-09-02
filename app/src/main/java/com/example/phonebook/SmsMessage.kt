package com.example.phonebook

import com.google.gson.annotations.SerializedName

data class SmsMessage(
    @SerializedName("id")
    val id: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("dateTime")
    val date: String,
    @Transient
    var isSelected: Boolean = false
)
