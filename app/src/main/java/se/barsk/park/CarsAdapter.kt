package se.barsk.park

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.util.SparseBooleanArray





/**
 * Adapter for the own/parked cars recycler views.
 */
open class CarsAdapter(val type: Type, var cars: List<Car>, val listener: (Car) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class Type {PARKED_CARS, OWN_CARS, MANAGE_CARS }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: ViewHolder
        when (type) {
            Type.OWN_CARS -> {
                vh = ViewHolder(OwnCarListEntry(parent.context))
            }
            Type.PARKED_CARS -> {
                vh = ViewHolder(ParkedCarListEntry(parent.context))
            }
            Type.MANAGE_CARS -> {
                vh = ViewHolder(ManageCarsListEntry(parent.context))
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