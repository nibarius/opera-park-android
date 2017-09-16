package se.barsk.park

import android.net.Uri
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.network.NetworkManager

/**
 * Class for handling handling incoming deep links and generating deep links
 * when sharing cars.
 */
class DeepLink(link: Uri) {
    companion object {
        private const val PARAMETER_SERVER = "park_server"
        private const val PARAMETER_OWNER = "o"
        private const val PARAMETER_REGNO = "r"
        private const val SCHEME = "https"
        private const val AUTHORITY = "opera-park.appspot.com"
        private const val PATH = ""
        private const val DYNAMIC_LINK_DOMAIN = "qgy49.app.goo.gl"

        private fun getDeepLinkFor(cars: List<OwnCar>): Uri {
            val builder = Uri.Builder()
                    .scheme(SCHEME)
                    .authority(AUTHORITY)
                    .appendPath(PATH)
                    .appendQueryParameter(PARAMETER_SERVER, NetworkManager.serverUrl)
            for ((regNo, owner) in cars) {
                builder.appendQueryParameter(PARAMETER_REGNO, regNo)
                builder.appendQueryParameter(PARAMETER_OWNER, owner)
            }
            return builder.build()
        }

        fun getDynamicLinkFor(cars: List<OwnCar>): String {
            val deepLink = getDeepLinkFor(cars)
            val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(deepLink)
                    .setDynamicLinkDomain(DYNAMIC_LINK_DOMAIN)
                    .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                    .setGoogleAnalyticsParameters(
                            DynamicLink.GoogleAnalyticsParameters.Builder()
                                    .setSource("in-app")
                                    .setCampaign("share")
                                    .build())
                    .setNavigationInfoParameters(
                            DynamicLink.NavigationInfoParameters.Builder()
                                    .setForcedRedirectEnabled(true)
                                    .build())
                    .buildDynamicLink()
            return dynamicLink.uri.toString()
        }
    }

    val isValid: Boolean
    val server: String
    val cars: MutableList<OwnCar> = mutableListOf()

    init {
        var valid = true
        val newServer = link.getQueryParameter(PARAMETER_SERVER)
        if (newServer != null) {
            server = newServer
        } else {
            server = ""
            valid = false
        }

        val regnos = link.getQueryParameters(PARAMETER_REGNO)
        val owners = link.getQueryParameters(PARAMETER_OWNER)
        if (regnos.size == owners.size) {
            // add OwnCars to cars list
            regnos.indices.mapTo(cars) { OwnCar(regnos[it], owners[it]) }
        } else {
            valid = false
        }

        isValid = valid
    }
}