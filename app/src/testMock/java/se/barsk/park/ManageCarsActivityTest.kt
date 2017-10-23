package se.barsk.park

import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.robolectric.Robolectric
import se.barsk.park.datatypes.MockCarCollection
import se.barsk.park.managecars.ManageCarsActivity

class ManageCarsActivityTest : RobolectricTest() {
    private lateinit var activity: ManageCarsActivity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(ManageCarsActivity::class.java).create().start().resume().visible().get()
    }

    @Test
    fun fabIsVisibleTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        assertEquals(View.VISIBLE, fab.visibility)
    }

    @Test
    fun carListShownWithCarsTest() {
        val manageCarsRecyclerView = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
        val placeholderView = activity.findViewById<TextView>(R.id.manage_cars_placeholder)
        assertEquals(false, ParkApp.carCollection.getCars().isEmpty())
        assertEquals(View.VISIBLE, manageCarsRecyclerView.visibility)
        assertEquals(View.GONE, placeholderView.visibility)
    }

    @Test
    fun placeholderShownWithNoCarsTest() {
        ParkApp.carCollection = MockCarCollection(0)
        val activity = Robolectric.buildActivity(ManageCarsActivity::class.java).create().start().resume().visible().get()
        val manageCarsRecyclerView = activity.findViewById<RecyclerView>(R.id.manage_cars_recyclerview)
        val placeholderView = activity.findViewById<TextView>(R.id.manage_cars_placeholder)
        assertEquals(true, ParkApp.carCollection.getCars().isEmpty())
        assertEquals(View.GONE, manageCarsRecyclerView.visibility)
        assertEquals(View.VISIBLE, placeholderView.visibility)
    }

    @Test
    fun hideFabWhenClickingItTest() {
        val fab = activity.findViewById<FloatingActionButton>(R.id.manage_cards_fab)
        fab.performClick()
        assertEquals(View.GONE, fab.visibility)
    }
}