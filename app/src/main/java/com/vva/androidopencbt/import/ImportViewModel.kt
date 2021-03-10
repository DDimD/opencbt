package com.vva.androidopencbt.import

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.vva.androidopencbt.db.DbRecord
import com.vva.androidopencbt.db.RecordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.FileReader
import java.lang.Exception

class ImportViewModel(private val dao: RecordDao): ViewModel() {
    private val _importState = MutableLiveData<ImportStates?>(null)
    val importState: LiveData<ImportStates?>
        get() = _importState

    private var importedRecordIds: List<Long>? = null

    fun importRecordsFromFile(docUri: Uri, context: Context) {
        import {
            val string = getStringFromFile(docUri, context)
            val listForImport = parseJson(string)
            val currentList = withContext(Dispatchers.IO){
                dao.getAllList()
            }
            val listToImport = getListForImport(listForImport, currentList)
            importedRecordIds = addRecordsToDatabase(listToImport)
        }
    }

    fun rollbackLastImport() {
        viewModelScope.launch(Dispatchers.IO) {
            importedRecordIds?.let { list ->
                list.forEach {
                    dao.deleteById(it)
                }
            }
        }
    }

    fun lastBackupRecordsCount(): Int {
        return importedRecordIds?.size ?: 0
    }

    private suspend fun addRecordsToDatabase(list: List<DbRecord>): List<Long> {
        val ids = ArrayList<Long>()
        withContext(Dispatchers.IO) {
            list.forEach {
                ids.add(dao.addRecord(it))
            }
        }
        return ids
    }

    private fun getListForImport(list1: List<DbRecord>, list2: List<DbRecord>): List<DbRecord> {
        return list1.filter { parsed ->
            var flag = true
            list2.forEach {
                if (parsed.equalsIgnoreId(it)) {
                    flag = false
                    return@forEach
                }
            }
            flag
        }
    }

    private fun parseJson(json: String): List<DbRecord> {
        return Json.decodeFromString(json)
    }

    private suspend fun getStringFromFile(docUri: Uri, context: Context): String {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openFileDescriptor(docUri, "r")?.use {
                BufferedReader(FileReader(it.fileDescriptor)).readLine()
            } ?: throw Exception("No data to import, input string is null")
        }
    }

    private fun import(block: suspend () -> Unit) {
        _importState.value = ImportStates.InProgress
        viewModelScope.launch {
            try {
                block()
                _importState.value = ImportStates.Success
            } catch (e: Exception) {
                _importState.value = ImportStates.Failure(e)
            } finally {
                _importState.value = null
            }
        }
    }
}

sealed class ImportStates {
    object InProgress : ImportStates()
    object Success : ImportStates()
    data class Failure(val e: Exception): ImportStates()
}

class ImportViewModelFactory(private val dao: RecordDao): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportViewModel::class.java)) {
            return ImportViewModel(dao) as T
        }
        throw IllegalAccessException("Unknown ViewModel class")
    }
}