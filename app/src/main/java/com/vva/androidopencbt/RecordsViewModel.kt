@file:Suppress("unused")

package com.vva.androidopencbt

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.vva.androidopencbt.db.CbdDatabase
import com.vva.androidopencbt.db.DbContract
import com.vva.androidopencbt.db.DbRecord
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.FileReader
import java.lang.Exception
import java.util.concurrent.TimeUnit

class RecordsViewModel(application: Application): AndroidViewModel(application) {
    private val db = CbdDatabase.getInstance(application)
    private var vmJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + vmJob)
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)
    var recyclerViewState: Parcelable? = null

    private val _recordsListUpdated = MutableLiveData<Boolean>()
    val recordsListUpdated: LiveData<Boolean>
        get() = _recordsListUpdated


    private val _isAuthenticated = MutableLiveData(!prefs.getBoolean("enable_pin_protection", false))
    val isAuthenticated:LiveData<Boolean>
        get() = _isAuthenticated

    private val _askChangesConfirm = MutableLiveData<Boolean?>()
    val askDetailsFragmentConfirm: LiveData<Boolean?>
        get() = _askChangesConfirm

    private val _isSelectionActive = MutableLiveData<Boolean>()
    val isSelectionActive: LiveData<Boolean>
        get() = _isSelectionActive

    fun askDetailsFragmentConfirmation() {
        _askChangesConfirm.value = true
    }

    fun detailsFragmentRollbackChanges() {
        _askChangesConfirm.value = false
    }

    fun detailsFragmentConfirmChangesCancel() {
        _askChangesConfirm.value = null
    }

    fun authSuccessful() {
        _isAuthenticated.value = true
    }

    fun deactivateSelection() {
        _isSelectionActive.value = false
    }

    fun activateSelection() {
        if (_isSelectionActive.value != true)
            _isSelectionActive.value = true
    }

    private val _importInAction = MutableLiveData<Boolean?>()
    val importInAction: LiveData<Boolean?>
        get() = _importInAction

    private val _importData = MutableLiveData<List<Long>?>()
    val importData: LiveData<List<Long>?>
        get() = _importData

    fun deleteRecord(id: Long) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                db.databaseDao.deleteRecord(
                        db.databaseDao.getRecordById(id)
                )
            }
        }
    }

    fun listUpdated() {
        _recordsListUpdated.value = true
        uiScope.launch {
            delay(TimeUnit.SECONDS.toMillis(1))
            _recordsListUpdated.value = false
        }
    }

    private suspend fun parseJsonFile(documentUri: Uri, context: Context): List<DbRecord>? {
        return withContext(Dispatchers.IO) {
            val fileDescriptor = context.contentResolver.openFileDescriptor(documentUri, "r")
            var list: ArrayList<DbRecord>?
            try {
                val string = BufferedReader(FileReader(fileDescriptor?.fileDescriptor!!)).readLine()
                list =  Json.decodeFromString(string)
            } catch (e: Exception) {
                list = null
            }

            list
        }
    }

    fun importRecordsFromFile(documentUri: Uri, context: Context) {
        _importInAction.value = true
        val ids = ArrayList<Long>()

        uiScope.launch {
            val records = parseJsonFile(documentUri, context)
            if (records == null) {
                _importData.value = null
            } else {
                withContext(Dispatchers.IO) {
                    val currentRecords = withContext(Dispatchers.IO) {
                        db.databaseDao.getAllList()
                    }

                    withContext(Dispatchers.Default) {
                        for (i in records.indices) {
                            var flag = true
                            for (j in currentRecords.indices) {
                                if (currentRecords[j].equalsIgnoreId(records[i])) {
                                    flag = false
                                    break
                                }
                            }
                            if (flag)
                                withContext(Dispatchers.IO) {
                                    ids.add(db.databaseDao.addRecord(records[i]))
                                }
                        }
                    }
                }
                _importData.value = ids
            }

            _importInAction.value = false
        }
    }

    fun doneImporting() {
        _importInAction.value = null
    }

    override fun onCleared() {
        super.onCleared()
        vmJob.cancel()
    }

    fun restoreRecyclerView(rv: RecyclerView) {
        uiScope.launch {
            withContext(Dispatchers.Default) {
                delay(10)
            }
            rv.layoutManager?.onRestoreInstanceState(recyclerViewState)
        }
    }
}