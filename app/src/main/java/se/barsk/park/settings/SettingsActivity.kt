package se.barsk.park.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.*
import android.support.v7.app.AppCompatActivity
import de.psdev.licensesdialog.LicensesDialogFragment
import se.barsk.park.BuildConfig
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.network.Backend


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
        fragment.setThirdPartyClickListener({ showThirdPartyList() })
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

        private lateinit var thirdPartyListener: () -> Unit

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) =
                preferenceChanged(key)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferencesName =
                    ParkApp.getSharedPreferencesFileName(activity.applicationContext)
            addPreferencesFromResource(R.xml.preferences)
        }

        override fun onResume() {
            super.onResume()
            preferenceChanged(getString(R.string.key_refresh_interval))
            preferenceChanged(getString(R.string.key_park_server_url))
            setTitles()
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen?, preference: Preference?): Boolean {
            if (preference?.key == getString(R.string.key_third_party_licenses)) {
                thirdPartyListener.invoke()
            } else if (preference?.key == getString(R.string.key_privacy_statement)) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Backend.privacyStatementUrl)))
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference)
        }

        fun setThirdPartyClickListener(listener: () -> Unit) {
            thirdPartyListener = listener
        }

        private fun setTitles() {
            findPreference(getString(R.string.key_about_group)).title = getString(R.string.settings_about_group_title, getString(R.string.app_name))
            findPreference(getString(R.string.key_version)).title = getString(R.string.settings_version, BuildConfig.VERSION_NAME)
        }

        private fun preferenceChanged(key: String) {
            val pref = findPreference(key)
            when (key) {
                getString(R.string.key_refresh_interval) -> {
                    pref as ListPreference
                    pref.summary = pref.entry
                }
                getString(R.string.key_park_server_url) -> {
                    pref as EditTextPreference
                    pref.summary = if (pref.text.isEmpty())
                        getString(R.string.no_server_placeholder_text)
                    else
                        pref.text
                }
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