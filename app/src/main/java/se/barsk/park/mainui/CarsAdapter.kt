package se.barsk.park.mainui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import se.barsk.park.datatypes.Car
import se.barsk.park.managecars.ManageCarsListEntry


/**
 * Adapter for the own/parked cars recycler views.
 */
open class CarsAdapter(private val type: Type, var cars: List<Car>, val listener: (Car) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class Type {PARKED_CARS, OWN_CARS, MANAGE_CARS }

    var recyclerView: RecyclerView? = null

    init {
        setHasStableIds(true)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: ViewHolder = when (type) {
            Type.OWN_CARS -> {
                ViewHolder(OwnCarListEntry(parent.context, listener))
            }
            Type.PARKED_CARS -> {
                ViewHolder(ParkedCarListEntry(parent.context))
            }
            Type.MANAGE_CARS -> {
                ViewHolder(ManageCarsListEntry(parent.context))
            }
        }
        vh.onClick(listener)
        return vh
    }

    override fun getItemCount(): Int = cars.size
    override fun getItemId(position: Int): Long = cars[position].hashCode().toLong()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            (holder.itemView as CarListEntry).showItem(cars[position], isSelected(position))

    open fun isSelected(position: Int): Boolean = false

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onClick(listener: (car: Car) -> Unit) = itemView.setOnClickListener { _ ->
            listener.invoke(cars[adapterPosition])
        }
    }
}