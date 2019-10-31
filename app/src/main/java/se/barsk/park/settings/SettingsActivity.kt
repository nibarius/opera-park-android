package se.barsk.park.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import de.psdev.licensesdialog.LicensesDialogFragment
import se.barsk.park.BuildConfig
import se.barsk.park.ParkApp
import se.barsk.park.R


/**
 * Activity for settings
 */
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ParkApp.init(this)
        super.onCreate(savedInstanceState)
    }

    // https://stackoverflow.com/questions/26509180/no-actionbar-in-preferenceactivity-after-upgrade-to-support-library-v21
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val fragment = ParkPreferenceFragment()
        fragment.setThirdPartyClickListener { showThirdPartyList() }
        supportFragmentManager.beginTransaction().replace(android.R.id.content, fragment).commit()
    }

    private fun showThirdPartyList() {
        val fragment = LicensesDialogFragment.Builder(this)
                .setNotices(R.raw.notices)
                .setShowFullLicenseText(false)
                .setIncludeOwnLicense(true)
                .build()

        fragment.show(supportFragmentManager, null)
    }

    class ParkPreferenceFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
        private lateinit var thirdPartyListener: () -> Unit

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName =
                    ParkApp.getSharedPreferencesFileName(requireActivity().applicationContext)
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) =
                preferenceChanged(key)

        override fun onResume() {
            super.onResume()
            setupPreferences()
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        fun setThirdPartyClickListener(listener: () -> Unit) {
            thirdPartyListener = listener
        }

        private fun setupPreferences() {
            findPreference<PreferenceCategory>(getString(R.string.key_about_group))
                    ?.title = getString(R.string.settings_about_group_title, getString(R.string.app_name))
            findPreference<Preference>(getString(R.string.key_version))?.title = getString(R.string.settings_version, BuildConfig.VERSION_NAME)
            val serverPref = findPreference<EditTextPreference>(getString(R.string.key_park_server_url))
            serverPref?.setOnBindEditTextListener { editText ->
                editText.hint = getString(R.string.park_server_input_hint)
                editText.inputType = InputType.TYPE_TEXT_VARIATION_URI
            }
            findPreference<Preference>(getString(R.string.key_third_party_licenses))
                    ?.setOnPreferenceClickListener {
                        thirdPartyListener()
                        true
                    }
        }

        private fun preferenceChanged(key: String) {
            when (key) {
                getString(R.string.key_usage_statistics) -> {
                    ParkApp.storageManager.recordUsageStatisticsConsentChange()
                    ParkApp.analytics.optOutToggled()
                }
                getString(R.string.key_crash_reporting) -> {
                    ParkApp.storageManager.recordCrashReportingConsentChange()
                }
            }
        }
    }
}