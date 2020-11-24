package com.vva.androidopencbt.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.vva.androidopencbt.R
import com.vva.androidopencbt.RecordsViewModel

class SettingsFragmentRoot: Fragment() {
    private lateinit var linearLayout: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        view.findViewById<Toolbar>(R.id.settings_toolbar).setupWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        linearLayout = inflater.inflate(R.layout.fragment_settings, container, false) as LinearLayout

        parentFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragmentNew())
                .commit()

        return linearLayout
    }
}

class SettingsFragmentNew : PreferenceFragmentCompat() {
    private lateinit var prefs: Array<SwitchPreferenceCompat>
    private val viewModel: RecordsViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        prefs = arrayOf(
                findPreference<Preference>("enable_thoughts") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_rational") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_situation") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_emotions") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_intensity") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_feelings") as SwitchPreferenceCompat,
                findPreference<Preference>("enable_actions") as SwitchPreferenceCompat
        )
        for (i in 0..5) {
            prefs[i].onPreferenceClickListener = lsnr
        }

        (findPreference<Preference>("desc_ordering") as SwitchPreferenceCompat).setOnPreferenceChangeListener {
            preference, newValue ->
            (preference as SwitchPreferenceCompat).isChecked = newValue as Boolean
            viewModel.setOrder(newValue)

            newValue
        }

        (findPreference<ListPreference>("default_export") as ListPreference).setOnPreferenceChangeListener {
            preference, newValue ->

            true
        }
    }

    var lsnr = Preference.OnPreferenceClickListener { preference ->
        var flag = false
        for (i in 0..5) {
            if (prefs[i].isChecked) {
                flag = true
                break
            }
        }
        if (!flag) {
            Toast.makeText(context, getString(R.string.pref_empty), Toast.LENGTH_SHORT).show()
            (preference as SwitchPreferenceCompat).isChecked = true
        }

        true
    }
}