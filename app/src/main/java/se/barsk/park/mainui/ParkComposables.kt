package se.barsk.park.mainui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import se.barsk.park.R
import se.barsk.park.datatypes.ParkedCar
import se.barsk.park.managecars.CarIcon

@Composable
fun ParkedCarsScreen(
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