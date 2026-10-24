package se.barsk.park.mainui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import de.psdev.licensesdialog.LicensesDialogFragment
import se.barsk.park.ParkApp
import se.barsk.park.R
import se.barsk.park.navigation.ParkNavHost
import se.barsk.park.theme.ParkTheme

/**
 * The single activity of the app, hosting all screens in a navigation graph.
 */
class ParkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ParkApp.init(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ParkTheme {
                ParkNavHost(onShowThirdPartyLicenses = { showThirdPartyList() })
            }
        }
    }

    private fun showThirdPartyList() {
        val fragment = LicensesDialogFragment.Builder(this)
            .setNotices(R.raw.notices)
            .setNoticesCssStyle(R.string.notices_custom_style)
            .setShowFullLicenseText(false)
            .setIncludeOwnLicense(true)
            .build()

        fragment.show(supportFragmentManager, null)
    }
}
