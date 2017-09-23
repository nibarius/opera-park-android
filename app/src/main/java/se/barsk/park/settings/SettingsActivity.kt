package se.barsk.park.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import de.psdev.licensesdialog.LicensesDialogFragment
import se.barsk.park.BuildConfig
import se.barsk.park.R
import se.barsk.park.consume
import se.barsk.park.storage.StorageManager


/**
 * Activity for settings
 */
class SettingsActivity : AppCompatActivity() {
    // https://stackoverflow.com/questions/26509180/no-actionbar-in-preferenceactivity-after-upgrade-to-support-library-v21
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val fragment = ParkPreferenceFragment()
        fragment.setThirdPartyClickListener(Preference.OnPreferenceClickListener { consume { showThirdPartyList() } })
        fragmentManager.beginTransaction().replace(android.R.id.content, fragment).commit()
    }


    private fun showThirdPartyList() {
        val fragment = LicensesDialogFragment.Builder(this)
                .setNotices(R.raw.notices)
                .setShowFullLicenseText(false)
                .setIncludeOwnLicense(true)
                .build()

        fragment.show(supportFragmentManager, null)
    }

    class ParkPreferenceFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

        private lateinit var thirdPartyListener: Preference.OnPreferenceClickListener

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            updateSummary(key)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferencesName = StorageManager.SHARED_PREF_FILE_SETTINGS
            addPreferencesFromResource(R.xml.preferences)
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onResume() {
            super.onResume()
            updateSummary("park_server_url")
            setTitles()
            findPreference("third_party_licenses").onPreferenceClickListener = thirdPartyListener
        }

        fun setThirdPartyClickListener(listener: Preference.OnPreferenceClickListener) {
            thirdPartyListener = listener
        }

        private fun setTitles() {
            findPreference("about_group").title = getString(R.string.settings_about_group_title, getString(R.string.app_name))
            findPreference("version").title = getString(R.string.settings_version, BuildConfig.VERSION_NAME)
        }

        private fun updateSummary(key: String) {
            val pref = findPreference(key)
            when (key) {
                "park_server_url" -> {
                    pref as EditTextPreference
                    pref.summary = if (pref.text.isEmpty())
                        getString(R.string.no_server_placeholder_text)
                    else
                        pref.text
                }
            }
        }
    }
}