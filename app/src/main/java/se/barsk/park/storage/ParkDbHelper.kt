package se.barsk.park.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ParkDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        val DATABASE_VERSION = 1
        val DATABASE_NAME = "Park.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(ParkContract.SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("We're still at version 1 this should never happen")
    }
}