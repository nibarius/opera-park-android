package se.barsk.park.storage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar

/**
 * Class for handling all the interactions with the database storage.
 */
class Database(context: Context, dbName: String = context.getString(R.string.parked_cars_database_name)) {
    private val dbHelper = ParkDbHelper(context, dbName)

    fun fetchAllCars(): MutableList<OwnCar> {
        val projection: Array<String> = arrayOf(
                ParkContract.CarCollectionTable.COLUMN_NAME_REGNO,
                ParkContract.CarCollectionTable.COLUMN_NAME_OWNER,
                ParkContract.CarCollectionTable.COLUMN_NAME_NICKNAME,
                ParkContract.CarCollectionTable.COLUMN_NAME_UUID
        )
        val sortOrder = ParkContract.CarCollectionTable.COLUMN_NAME_POSITION + " ASC"

        val db = dbHelper.readableDatabase
        val cursor = db.query(
                ParkContract.CarCollectionTable.TABLE_NAME,
                projection,
                null, // No where clause
                null, // No where clause args
                null, // No grouping
                null, // No filtering
                sortOrder
        )

        val ownCars: MutableList<OwnCar> = mutableListOf()
        while (cursor.moveToNext()) {
            ownCars.add(OwnCar(
                    cursor.getString(cursor.getColumnIndexOrThrow(ParkContract.CarCollectionTable.COLUMN_NAME_REGNO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ParkContract.CarCollectionTable.COLUMN_NAME_OWNER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ParkContract.CarCollectionTable.COLUMN_NAME_NICKNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ParkContract.CarCollectionTable.COLUMN_NAME_UUID))
            ))
        }
        cursor.close()

        return ownCars
    }

    fun insertOrReplace(ownCar: OwnCar, position: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues()
        values.put(ParkContract.CarCollectionTable.COLUMN_NAME_POSITION, position)
        values.put(ParkContract.CarCollectionTable.COLUMN_NAME_REGNO, ownCar.regNo)
        values.put(ParkContract.CarCollectionTable.COLUMN_NAME_OWNER, ownCar.owner)
        values.put(ParkContract.CarCollectionTable.COLUMN_NAME_NICKNAME, ownCar.nickName)
        values.put(ParkContract.CarCollectionTable.COLUMN_NAME_UUID, ownCar.id)
        db.insertWithOnConflict(
                ParkContract.CarCollectionTable.TABLE_NAME,
                null, // no nullColumnHack
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun remove(ownCar: OwnCar) {
        val position = getPosition(ownCar)
        val db = dbHelper.writableDatabase
        db.delete(
                ParkContract.CarCollectionTable.TABLE_NAME,
                "${ParkContract.CarCollectionTable.COLUMN_NAME_UUID} = ?",
                arrayOf(ownCar.id)
        )
        decreasePositionsAbove(position)
    }

    /**
     * Returns the position for the given car or Int.MAX_VALUE if the car doesn't exist
     */
    private fun getPosition(ownCar: OwnCar): Int {
        val projection: Array<String> = arrayOf(
                ParkContract.CarCollectionTable.COLUMN_NAME_POSITION
        )
        val selection = ParkContract.CarCollectionTable.COLUMN_NAME_UUID + " = ?"
        val selectionArgs: Array<String> = arrayOf(ownCar.id)

        val db = dbHelper.readableDatabase
        val cursor = db.query(
                ParkContract.CarCollectionTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, // No grouping
                null, // No filtering
                null // No sorting
        )

        val pos = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(ParkContract.CarCollectionTable.COLUMN_NAME_POSITION))
        } else {
            Int.MAX_VALUE
        }
        cursor.close()
        return pos
    }

    /**
     * Decrease the position by one of all items with higher position than the given position.
     */
    private fun decreasePositionsAbove(position: Int) {
        val updateSQL = "UPDATE " + ParkContract.CarCollectionTable.TABLE_NAME +
                " SET " + ParkContract.CarCollectionTable.COLUMN_NAME_POSITION +
                " = " + ParkContract.CarCollectionTable.COLUMN_NAME_POSITION + " - 1" +
                " WHERE " + ParkContract.CarCollectionTable.COLUMN_NAME_POSITION +
                " > " + position
        val db = dbHelper.writableDatabase
        db.execSQL(updateSQL)
    }
}