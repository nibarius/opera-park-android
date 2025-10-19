package se.barsk.park.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.core.view.WindowCompat
import de.psdev.licensesdialog.LicensesDialogFragment
import se.barsk.park.ParkApp
import se.barsk.park.R


/**
 * Activity for settings
 */
class SettingsActivity : AppCompatActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        ParkApp.init(this)
        super.onCreate(savedInstanceState)

        // Enable edge to edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // You should use a proper theme here, e.g., your app's MaterialTheme
            MaterialTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onShowThirdPartyLicenses = { showThirdPartyList() },
                    onNavigateUp = { finish() }
                )
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
