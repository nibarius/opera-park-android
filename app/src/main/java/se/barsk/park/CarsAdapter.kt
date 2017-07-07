package se.barsk.park

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

/**
 * Adapter for the own/parked cars recycler views.
 */
class CarsAdapter(val cars: List<Car>, val listener: (Car) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: ViewHolder
        when (cars[0]) {
            is OwnCar -> {
                vh = ViewHolder(OwnCarListEntry(parent.context))
            }
            is ParkedCar -> {
                vh = ViewHolder(ParkedCarListEntry(parent.context))
            }
            else -> {
                throw RuntimeException("Unknown car type found")
            }
        }
        vh.onClick(listener)
        return vh
    }

    override fun getItemCount(): Int = cars.size
    override fun getItemId(position: Int): Long = cars[position].hashCode().toLong()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder.itemView as CarListEntry).showItem(cars[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onClick(listener: (car: Car) -> Unit) {
            itemView.setOnClickListener { _ ->
                listener.invoke(cars[adapterPosition])
            }
        }
    }
}