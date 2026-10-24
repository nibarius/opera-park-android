package se.barsk.park.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import se.barsk.park.mainui.ParkViewModel
import se.barsk.park.mainui.ParkedCarsDestination
import se.barsk.park.managecars.ManageCarsDestination
import se.barsk.park.settings.SettingsScreen

@Serializable
object ParkedCarsRoute

@Serializable
data class ManageCarsRoute(val addCar: Boolean = false)

@Serializable
object SettingsRoute

@Composable
fun ParkNavHost(
    onShowThirdPartyLicenses: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = ParkedCarsRoute,
        enterTransition = { slideInHorizontally { it } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it / 4 } + fadeOut() },
        popEnterTransition = { slideInHorizontally { -it / 4 } + fadeIn() },
        popExitTransition = { slideOutHorizontally { it } + fadeOut() }
    ) {
        composable<ParkedCarsRoute> {
            val viewModel: ParkViewModel = viewModel()
            // The back stack entry is only resumed while this screen is on top and the activity
            // is resumed, so the garage is refreshed and automatic updates run only while the
            // parked cars are actually visible.
            LifecycleResumeEffect(viewModel) {
                viewModel.onScreenResumed()
                onPauseOrDispose { viewModel.onScreenPaused() }
            }
            ParkedCarsDestination(
                viewModel = viewModel,
                onManageCarsClick = dropUnlessResumed { navController.navigate(ManageCarsRoute()) },
                onSettingsClick = dropUnlessResumed { navController.navigate(SettingsRoute) },
                onAddCarClick = dropUnlessResumed { navController.navigate(ManageCarsRoute(addCar = true)) }
            )
        }

        composable<ManageCarsRoute> { backStackEntry ->
            val route: ManageCarsRoute = backStackEntry.toRoute()
            ManageCarsDestination(
                initialAddCar = route.addCar,
                onNavigateUp = dropUnlessResumed { navController.navigateUp() }
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                viewModel = viewModel(),
                onShowThirdPartyLicenses = onShowThirdPartyLicenses,
                onNavigateUp = dropUnlessResumed { navController.navigateUp() }
            )
        }
    }
}
