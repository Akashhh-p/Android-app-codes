package com.example.phonebook

import com.google.gson.annotations.SerializedName

data class Contact(
    @SerializedName("contactId")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @Transient
    val dataId: Long = -1,
    @Transient
    var isSelected: Boolean = false
)
