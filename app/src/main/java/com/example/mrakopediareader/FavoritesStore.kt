package com.example.mrakopediareader

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.mrakopediareader.api.dto.Page

class FavoritesStore(context: Context) {
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    operator fun get(name: String): String? {
        return sharedPreferences.getString(name, null)
    }

    fun remove(name: String?) {
        sharedPreferences.edit().remove(name).apply()
    }

    operator fun set(name: String, value: String?) {
        sharedPreferences.edit().putString(name, value).apply()
    }

    val pages: Collection<Page>
        get() = sharedPreferences.all
            .keys
            .fold(arrayListOf()) {
                list, title -> this[title]?.let { list.add(Page(title, it)); list } ?: list
            }

    fun has(name: String): Boolean {
        return sharedPreferences.getString(name, null) != null
    }

}