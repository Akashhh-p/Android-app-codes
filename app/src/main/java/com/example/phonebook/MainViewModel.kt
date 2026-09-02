package com.example.phonebook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _appContacts = MutableLiveData<MutableList<Contact>>(mutableListOf())
    val appContacts: LiveData<MutableList<Contact>> = _appContacts

    private val _deviceContacts = MutableLiveData<MutableList<Contact>>(mutableListOf())
    val deviceContacts: LiveData<MutableList<Contact>> = _deviceContacts

    private val _importedDeviceContacts = MutableLiveData<MutableList<Contact>>(mutableListOf())
    val importedDeviceContacts: LiveData<MutableList<Contact>> = _importedDeviceContacts

    private val _smsMessages = MutableLiveData<MutableList<SmsMessage>>(mutableListOf())
    val smsMessages: LiveData<MutableList<SmsMessage>> = _smsMessages

    private val _importedSmsMessages = MutableLiveData<MutableList<SmsMessage>>(mutableListOf())
    val importedSmsMessages: LiveData<MutableList<SmsMessage>> = _importedSmsMessages

    var isDeviceContactsLoaded = false
    var isSmsLoaded = false
    var isAppContactsLoaded = false

    fun setAppContacts(list: List<Contact>) {
        _appContacts.value = list.toMutableList()
        isAppContactsLoaded = true
    }

    fun setDeviceContacts(list: List<Contact>) {
        _deviceContacts.value = list.toMutableList()
        isDeviceContactsLoaded = true
    }

    fun setImportedDeviceContacts(list: List<Contact>) {
        _importedDeviceContacts.value = list.toMutableList()
    }

    fun setSmsMessages(list: List<SmsMessage>) {
        _smsMessages.value = list.toMutableList()
        isSmsLoaded = true
    }

    fun setImportedSmsMessages(list: List<SmsMessage>) {
        _importedSmsMessages.value = list.toMutableList()
    }
}
