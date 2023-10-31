package io.sunshine0523.sidebar.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.sunshine0523.sidebar.R
import io.sunshine0523.sidebar.app.SidebarApplication
import io.sunshine0523.sidebar.ui.sidebar.SidebarAppSettingActivity

/**
 * @author KindBrave
 * @since 2023/9/26
 */
class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SidebarApplication.CONFIG
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
        setPreferencesFromResource(R.xml.settings, null)
        findPreference<Preference>("sidebar_app")?.setOnPreferenceClickListener {
            startActivity(Intent(requireContext(), SidebarAppSettingActivity::class.java))
            true
        }
    }
}