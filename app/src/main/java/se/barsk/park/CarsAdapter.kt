package se.barsk.park

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

/**
 * Adapter for the own/parked cars recycler views.
 */
class CarsAdapter(val cars: List<Car>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = OwnCarListEntry(parent.context)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = cars.size
    override fun getItemId(position: Int): Long = cars[position].hashCode().toLong()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder.itemView as CarListEntry).showItem(cars[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}