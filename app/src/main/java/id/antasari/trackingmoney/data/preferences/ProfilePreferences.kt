package id.antasari.trackingmoney.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profile")

class ProfilePreferences(private val context: Context) {
    companion object {
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    val userName: Flow<String> = context.profileDataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: "Pengguna"
    }

    suspend fun saveUserName(name: String) {
        context.profileDataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }
}
