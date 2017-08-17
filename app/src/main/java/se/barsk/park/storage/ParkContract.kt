package se.barsk.park.storage

import android.provider.BaseColumns

object ParkContract {

    val SQL_CREATE_ENTRIES =
            "CREATE TABLE " + CarCollectionTable.TABLE_NAME + " (" +
                    CarCollectionTable.COLUMN_NAME_UUID + " TEXT PRIMARY KEY," +
                    CarCollectionTable.COLUMN_NAME_POSITION + " INTEGER," +
                    CarCollectionTable.COLUMN_NAME_REGNO + " TEXT," +
                    CarCollectionTable.COLUMN_NAME_OWNER + " TEXT," +
                    CarCollectionTable.COLUMN_NAME_NICKNAME + " TEXT)"

    class CarCollectionTable private constructor() : BaseColumns {
        companion object {
            val TABLE_NAME = "car_collection"
            val COLUMN_NAME_UUID = "uuid"
            val COLUMN_NAME_POSITION = "position"
            val COLUMN_NAME_REGNO = "regno"
            val COLUMN_NAME_OWNER = "owner"
            val COLUMN_NAME_NICKNAME = "nickname"
        }

    }
}