package com.example.phonebook

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class SmsStorage(private val context: Context) {
    private val gson = Gson()

    fun exportToUri(uri: Uri, messages: List<SmsMessage>): Boolean {
        return try {
            val json = gson.toJson(messages)
            context.contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os).use { writer ->
                    writer.write(json)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun importFromUri(uri: Uri): List<SmsMessage>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { `is` ->
                InputStreamReader(`is`).use { reader ->
                    val type = object : TypeToken<List<SmsMessage>>() {}.type
                    gson.fromJson(reader, type)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
