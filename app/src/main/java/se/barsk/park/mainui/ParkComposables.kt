package se.barsk.park.mainui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar
import se.barsk.park.datatypes.ParkedCar
import se.barsk.park.managecars.CarIcon

@Composable
fun ParkedCarsScreen(
    isRefreshing: Boolean,
    parkedCars: List<ParkedCar>,
    parkingState: ParkActivity.ParkingState,
    unableToConnectText: String,
    setServer: () -> Unit,
    retry: () -> Unit,
    ownCars: List<OwnCar>,
    onCarClick: (OwnCar) -> Unit,
    onAddCarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        Row(modifier = modifier.fillMaxSize()) {
            ParkedCarsSection(
                isRefreshing,
                parkedCars,
                parkingState,
                unableToConnectText,
                setServer,
                retry,
                modifier = Modifier.weight(1f) // take all remaining space
            )
            MyCarsSection(
                ownCars,
                parkedCars,
                parkingState == ParkActivity.ParkingState.FULL,
                onCarClick,
                onAddCarClick,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(256.dp)
            )
        }
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            ParkedCarsSection(
                isRefreshing,
                parkedCars,
                parkingState,
                unableToConnectText,
                setServer,
                retry,
                modifier = Modifier.weight(1f) // Take the remaining space
            )
            MyCarsSection(
                ownCars,
                parkedCars,
                parkingState == ParkActivity.ParkingState.FULL,
                onCarClick,
                onAddCarClick
            )
        }
    }
}

@Composable
fun ParkedCarsSection(
    isRefreshing: Boolean,
    cars: List<ParkedCar>,
    parkingState: ParkActivity.ParkingState,
    unableToConnectText: String,
    setServer: () -> Unit,
    retry: () -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = retry, modifier = modifier) {
        ParkedCarsContent(
            cars = cars,
            parkingState = parkingState,
            unableToConnectText = unableToConnectText,
            setServer = setServer,
            retry = retry,
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.backgroundColor))
        )
    }
}

@Composable
fun ParkedCarsContent(
    cars: List<ParkedCar>,
    parkingState: ParkActivity.ParkingState,
    unableToConnectText: String,
    setServer: () -> Unit,
    retry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        parkingState == ParkActivity.ParkingState.NO_SERVER -> {
            ServerIssuePlaceholder(
                text = stringResource(R.string.no_server_placeholder_text),
                buttonText = stringResource(R.string.no_server_placeholder_button),
                onClick = { setServer() },
                modifier = modifier
            )
        }

        parkingState == ParkActivity.ParkingState.WAITING_ON_RESPONSE && cars.isEmpty() -> {
            LoadingPlaceholder(modifier = modifier)
        }

        parkingState == ParkActivity.ParkingState.REQUEST_FAILED -> {
            ServerIssuePlaceholder(
                text = unableToConnectText,
                buttonText = stringResource(R.string.unable_to_connect_placeholder_button),
                onClick = { retry() },
                modifier = modifier
            )
        }

        parkingState == ParkActivity.ParkingState.EMPTY -> {
            NoParkedCarsPlaceholder(modifier = modifier)
        }

        else -> {
            ParkedCarsListContent(cars, modifier = modifier)
        }
    }
}


@Composable
fun ParkedCarsListContent(
    items: List<ParkedCar>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(items = items, key = { it.regNo }) { entry ->
            ParkedCarsListEntry(entry)
        }
    }
}

@Composable
fun ParkedCarsListEntry(car: ParkedCar) {
    ListItem(
        supportingContent = { Text(car.regNo.uppercase()) },
        headlineContent = { Text(text = car.owner) },
        trailingContent = { Text(car.startTime.substringAfter(" ")) },
        leadingContent = { CarIcon(car) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
fun NoParkedCarsPlaceholder(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()), // Needed for pull to refresh
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.empty_placeholder),
            contentDescription = null,
        )
        PlaceholderBodyText(stringResource(R.string.parked_cars_placeholder))
    }
}

@Composable
fun ServerIssuePlaceholder(
    text: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_cloud_off_black_72dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null,
        )
        PlaceholderBodyText(text)
        TextButton(
            onClick = { onClick() },
        ) {
            Text(text = buttonText)
        }
    }
}

@Composable
fun LoadingPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        PlaceholderBodyText(stringResource(R.string.updating_status_placeholder))
    }
}

@Composable
private fun PlaceholderBodyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
fun OwnCarsList(
    ownCars: List<OwnCar>,
    parkedCars: List<ParkedCar>,
    garageFull: Boolean,
    onCarClick: (OwnCar) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            LazyColumn(modifier = modifier) {
                items(items = ownCars, key = { it.regNo }) { car ->
                    val isParked = parkedCars.any { it.regNo == car.regNo }
                    OwnCarEntry(
                        car = car,
                        isParked = isParked,
                        garageFull = garageFull,
                        onCarClick = onCarClick
                    )
                }
            }
        }

        else -> {
            LazyRow(modifier = modifier) {
                items(items = ownCars, key = { it.regNo }) { car ->
                    val isParked = parkedCars.any { it.regNo == car.regNo }
                    OwnCarEntry(
                        car = car,
                        isParked = isParked,
                        garageFull = garageFull,
                        onCarClick = onCarClick
                    )
                }
            }
        }
    }
}


@Composable
fun OwnCarEntry(
    car: OwnCar,
    isParked: Boolean,
    garageFull: Boolean,
    onCarClick: (OwnCar) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val buttonEnabled = if (isParked) true else !garageFull
    val actionText = if (isParked) {
        stringResource(R.string.unpark_label).uppercase()
    } else {
        stringResource(R.string.park_label).uppercase()
    }

    val commonModifier = Modifier.padding(8.dp)
    val onClickAction = { onCarClick(car) }

    when {
        // Case 1: Parked car in Dark Theme -> OutlinedButton (Unpark style)
        isParked && isDarkTheme -> {
            OutlinedButton(
                onClick = onClickAction,
                modifier = commonModifier,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(2.dp, colorResource(R.color.unparkButtonColor)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorResource(R.color.unparkButtonColor),
                    disabledContentColor = colorResource(R.color.colorDisabled)
                )
            ) {
                ButtonContent(actionText = actionText, regNo = car.regNo)
            }
        }

        // Case 2: Parked car in Light Theme -> Filled Button
        isParked -> {
            Button(
                onClick = onClickAction,
                modifier = commonModifier,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.unparkButtonColor),
                )
            ) {
                ButtonContent(actionText = actionText, regNo = car.regNo)
            }
        }

        // Case 3: Not parked -> OutlinedButton (Park style)
        else -> {
            OutlinedButton(
                onClick = onClickAction,
                modifier = commonModifier,
                shape = RoundedCornerShape(8.dp),
                enabled = buttonEnabled,
                border = BorderStroke(
                    2.dp,
                    if (buttonEnabled) colorResource(R.color.parkButtonColor) else colorResource(R.color.colorDisabled)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorResource(R.color.parkButtonColor),
                    disabledContentColor = colorResource(R.color.colorDisabled)
                )
            ) {
                ButtonContent(actionText = actionText, regNo = car.regNo)
            }
        }
    }
}

@Composable
private fun ButtonContent(actionText: String, regNo: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = actionText, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Text(text = regNo, style = MaterialTheme.typography.titleMedium, maxLines = 1)
    }
}

@Composable
fun OwnCarsPlaceholder(onAddCarClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val text =
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                stringResource(R.string.no_cars_placeholder_message_short)
            } else {
                stringResource(R.string.no_cars_placeholder_message)
            }
        Text(
            text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        TextButton(onAddCarClick) {
            Text(stringResource(R.string.add_car_button).uppercase())
        }
    }
}

@Composable
fun MyCarsSection(
    ownCars: List<OwnCar>,
    parkedCars: List<ParkedCar>,
    garageFull: Boolean,
    onCarClick: (OwnCar) -> Unit,
    onAddCarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
    ) {
        Text(
            stringResource(R.string.my_cars_label),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp),
            maxLines = 1
        )
        if (ownCars.isEmpty()) {
            OwnCarsPlaceholder(onAddCarClick)
        } else {
            OwnCarsList(
                ownCars = ownCars,
                parkedCars = parkedCars,
                garageFull = garageFull,
                onCarClick = onCarClick,
            )
        }
    }
}
