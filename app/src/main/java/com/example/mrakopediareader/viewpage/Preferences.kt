package com.example.mrakopediareader.viewpage

import android.content.SharedPreferences
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class Preferences(private val sharedPreferences: SharedPreferences) {
    private val scrollTopSubject = BehaviorSubject.createDefault(getBoolean(KEY_SCROLL_TOP, false))

    private val scrollTopObservable = scrollTopSubject.doOnNext {
        setBoolean(KEY_SCROLL_TOP, it)
    }

    @Suppress("SameParameterValue")
    private fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    @Suppress("SameParameterValue")
    private fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    val scrollTopVisible: Boolean
        get() = scrollTopSubject.value ?: false

    fun toggleScrollTopVisible() {
        scrollTopSubject.onNext(!(scrollTopSubject.value ?: false))
    }

    fun observeScrollTopVisible(): Observable<Boolean> {
        return scrollTopObservable
    }

    companion object {
        const val KEY_SCROLL_TOP = "KEY_SCROLL_TOP"
    }
}