package se.barsk.park.storage

import android.provider.BaseColumns

object ParkContract {

    const val SQL_CREATE_ENTRIES =
            "CREATE TABLE " + CarCollectionTable.TABLE_NAME + " (" +
                    CarCollectionTable.COLUMN_NAME_UUID + " TEXT PRIMARY KEY," +
                    CarCollectionTable.COLUMN_NAME_POSITION + " INTEGER," +
                    CarCollectionTable.COLUMN_NAME_REGNO + " TEXT," +
                    CarCollectionTable.COLUMN_NAME_OWNER + " TEXT," +
                    CarCollectionTable.COLUMN_NAME_NICKNAME + " TEXT)"

    class CarCollectionTable private constructor() : BaseColumns {
        companion object {
            const val TABLE_NAME = "car_collection"
            const val COLUMN_NAME_UUID = "uuid"
            const val COLUMN_NAME_POSITION = "position"
            const val COLUMN_NAME_REGNO = "regno"
            const val COLUMN_NAME_OWNER = "owner"
            const val COLUMN_NAME_NICKNAME = "nickname"
        }

    }
}