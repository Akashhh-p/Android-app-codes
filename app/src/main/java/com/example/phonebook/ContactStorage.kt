package com.example.phonebook

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ContactStorage(private val context: Context) {
    private val gson = Gson()
    private val internalFileName = "contacts.json"
    private val externalFileName = "phone_book_export.json"

    fun saveContactsInternal(contacts: List<Contact>) {
        val json = gson.toJson(contacts)
        context.openFileOutput(internalFileName, Context.MODE_PRIVATE).use { fos ->
            OutputStreamWriter(fos).use { writer ->
                writer.write(json)
            }
        }
    }

    fun loadContactsInternal(): List<Contact>? {
        val file = File(context.filesDir, internalFileName)
        if (!file.exists()) return null

        return try {
            context.openFileInput(internalFileName).use { fis ->
                InputStreamReader(fis).use { reader ->
                    val type = object : TypeToken<List<Contact>>() {}.type
                    gson.fromJson(reader, type)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun exportToAppSpecificExternal(contacts: List<Contact>): String? {
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return null
        val file = File(externalDir, externalFileName)
        return try {
            val json = gson.toJson(contacts)
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    writer.write(json)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun readFromAppSpecificExternal(): List<Contact>? {
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return null
        val file = File(externalDir, externalFileName)
        if (!file.exists()) return null

        return try {
            file.inputStream().use { fis ->
                InputStreamReader(fis).use { reader ->
                    val type = object : TypeToken<List<Contact>>() {}.type
                    gson.fromJson(reader, type)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun exportToUri(uri: Uri, contacts: List<Contact>): Boolean {
        return try {
            val json = gson.toJson(contacts)
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

    fun importFromUri(uri: Uri): List<Contact>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { `is` ->
                InputStreamReader(`is`).use { reader ->
                    val type = object : TypeToken<List<Contact>>() {}.type
                    gson.fromJson(reader, type)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
