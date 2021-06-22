package kkj.settingseditor.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kkj.settingseditor.data.MyDatabase
import kkj.settingseditor.data.MySettings

class PageViewModel : ViewModel() {
    companion object {
        private const val TAG = "SettingsEditor.PageViewModel"
    }

    private val _index = MutableLiveData<Int>()
    val text: LiveData<ArrayList<Array<String>>> = Transformations.map(_index) {
        when (it) {
            0 -> MyDatabase.getInstance()?.read()               // Favorite
            1 -> MySettings.getInstance()?.readGlobalSettings() // Global
            2 -> MySettings.getInstance()?.readSystemSettings() // System
            3 -> MySettings.getInstance()?.readSecureSettings() // Secure
            else -> arrayListOf()
        }
    }

    fun setIndex(index: Int) {
        _index.value = index
    }
}