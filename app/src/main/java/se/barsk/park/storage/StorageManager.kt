package se.barsk.park.storage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import se.barsk.park.datatypes.CarCollection
import se.barsk.park.datatypes.OwnCar


object StorageManager {
    private var dbHelper: ParkDbHelper? = null

    fun init(context: Context) {
        dbHelper = ParkDbHelper(context)
    }

    fun fetchAllCars(): MutableList<OwnCar> {
        val projection: Array<String> = arrayOf(
                ParkContract.CarCollectionTable.COLUMN_NAME_REGNO,
                ParkContract.CarCollectionTable.COLUMN_NAME_OWNER,
                ParkContract.CarCollectionTable.COLUMN_NAME_NICKNAME,
                ParkContract.CarCollectionTable.COLUMN_NAME_UUID
        )
        val sortOrder = ParkContract.CarCollectionTable.COLUMN_NAME_POSITION + " ASC"

        val db = dbHelper!!.readableDatabase
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
        val db = dbHelper!!.writableDatabase
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
        val db = dbHelper!!.writableDatabase
        db.delete(
                ParkContract.CarCollectionTable.TABLE_NAME,
                "${ParkContract.CarCollectionTable.COLUMN_NAME_UUID} = ?",
                arrayOf(ownCar.id)
        )
    }
}