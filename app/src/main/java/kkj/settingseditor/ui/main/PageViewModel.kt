package kkj.settingseditor.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import kkj.settingseditor.MySettings

class PageViewModel : ViewModel() {
    private val _index = MutableLiveData<Int>()
    val text: LiveData<ArrayList<Array<String>>> = Transformations.map(_index) {
        when (it) {
            1 -> MySettings.getInstance()?.queryGlobalSettings() // Global
            2 -> MySettings.getInstance()?.querySystemSettings() // System
            3 -> MySettings.getInstance()?.querySecureSettings() // Secure
            else -> arrayListOf()
        }
    }

    fun setIndex(index: Int) {
        _index.value = index
    }
}