package se.barsk.park.mainui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import se.barsk.park.datatypes.ParkedCar
import se.barsk.park.managecars.CarIcon

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
