package se.barsk.park.managecars

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar
import kotlin.math.abs

@Composable
fun ManageCarsScreen(
    cars: List<ManageCarsListEntry>,
    onClick: (car: OwnCar) -> Unit,
    onLongClick: (car: OwnCar) -> Unit,
    modifier: Modifier = Modifier
) {
    if (cars.isEmpty()) {
        ManageCarsPlaceholder(modifier = modifier)
    } else {
        OwnCarsListContent(cars, onClick, onLongClick, modifier = modifier)
    }
}

@Composable
fun ManageCarsPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_directions_car_black_72dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null,
        )
        Text(
            text = stringResource(R.string.manage_cars_list_placeholder),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ManageCarsFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.add_car_button)
        )
    }
}

@Composable
fun OwnCarsListContent(
    items: List<ManageCarsListEntry>,
    onClick: (OwnCar) -> Unit,
    onLongClick: (OwnCar) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(items = items, key = { it.car.id }) { entry ->
            ManageCarsListEntry(entry, onClick, onLongClick)
        }
    }
}

@Composable
fun ManageCarsListEntry(
    entry: ManageCarsListEntry,
    onClick: (car: OwnCar) -> Unit,
    onLongClick: (car: OwnCar) -> Unit
) {
    val containerColor = if (entry.selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = { onClick(entry.car) },
            onLongClick = { onLongClick(entry.car) }
        ),
        colors = ListItemDefaults.colors(containerColor = containerColor),
        headlineContent = { Text(entry.car.regNo.uppercase()) },
        supportingContent = { Text(entry.car.owner) },
        leadingContent = {
            AnimatedContent(
                targetState = entry.selected,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith
                            (fadeOut() + scaleOut(targetScale = 0.8f))
                }
            ) { isSelected ->
                if (isSelected) {
                    SelectedCarIcon()
                } else {
                    CarIcon(entry.car)
                }
            }
        }
    )
}

@Composable
fun CarIcon(car: OwnCar) {
    val regNo = car.regNo.uppercase()
    val colors = integerArrayResource(R.array.avatar_background_color)
    val colorForCar = colors[abs(regNo.hashCode()) % colors.size]
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color = Color(colorForCar)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (regNo.isNotEmpty()) regNo.substring(0, 1) else stringResource(R.string.missing_placeholder),
            style = MaterialTheme.typography.titleMedium,
            color = colorResource(R.color.colorAvatarTextColor)
        )
    }
}

@Composable
fun SelectedCarIcon() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}