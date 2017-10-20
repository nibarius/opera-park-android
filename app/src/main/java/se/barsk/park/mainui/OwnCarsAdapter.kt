package se.barsk.park.mainui

import android.support.v7.widget.RecyclerView
import se.barsk.park.datatypes.Car

/**
 * Adapter for the list of own cars in the main ui.
 */
class OwnCarsAdapter(cars: List<Car>, listener: (Car) -> Unit) :
        CarsAdapter(Type.OWN_CARS, cars, listener) {
    var garageFull = false

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            (holder.itemView as OwnCarListEntry).showItem(cars[position], garageFull = garageFull)
}