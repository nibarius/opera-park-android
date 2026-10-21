package se.barsk.park

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import se.barsk.park.mainui.ParkActivity

// Inspiration from:
// https://www.bignerdranch.com/blog/splash-screens-the-right-way/
// https://medium.com/@lucasurbas/placeholder-ui-launch-screen-d85c35552119
/**
 * Activity for the splash screen that just opens the ParkActivity
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, ParkActivity::class.java))
        finish()
    }
}