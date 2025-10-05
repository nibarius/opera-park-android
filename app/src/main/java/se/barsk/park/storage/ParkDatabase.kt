package se.barsk.park.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.barsk.park.R
import se.barsk.park.datatypes.OwnCar

@Database(entities = [CarEntity::class], version = 2)
abstract class ParkDatabase : RoomDatabase() {
    companion object {
        /**
         * All fields were nullable in version 1, but in version 2 none are so
         * create a new table and copy the data, replace any nulls with "?"
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create new table
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `car_collection_tmp` (" +
                            "`uuid` TEXT PRIMARY KEY NOT NULL, " +
                            "`position` INTEGER NOT NULL, " +
                            "`regno` TEXT NOT NULL, " +
                            "`owner` TEXT NOT NULL, " +
                            "`nickname` TEXT NOT NULL)"
                )

                // Copy the data
                db.execSQL(
                    "INSERT INTO car_collection_tmp (uuid, position, regno, owner, nickname) " +
                            "SELECT IFNULL(uuid, '?'), IFNULL(position, '?'), IFNULL(regno, '?'), IFNULL(owner, '?'), IFNULL(nickname, '?') FROM car_collection"
                )

                db.execSQL("DROP TABLE car_collection")
                db.execSQL("ALTER TABLE car_collection_tmp RENAME TO car_collection")
            }
        }

        fun createDatabase(appContext: Context): ParkDatabase {
            return Room.databaseBuilder(
                appContext,
                ParkDatabase::class.java,
                appContext.getString(R.string.parked_cars_database_name)
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }

    abstract fun carDao(): CarDao

    fun fetchAllCars(): Flow<List<OwnCar>> {
        return carDao().fetchAllCars().map { carEntityList ->
            carEntityList.map {
                // In version 2.1 and below it was possible to create a car with only whitespace
                // in the licence plate number. When doing this the app would crash every time
                // the user enters the mange cars view making it impossible to fix the licence
                // plate number. When reading from storage replace blank licence plate numbers
                // with just a '?'
                val regNo = it.regNo.ifBlank { "?" }
                OwnCar(
                    regNo = regNo,
                    owner = it.owner,
                    nickName = it.nickName,
                    id = it.uuid
                )
            }.toMutableList()
        }
    }

    suspend fun insertOrReplace(ownCar: OwnCar, position: Int) =
        carDao().insert(CarEntity.fromOwnCar(ownCar, position))

    suspend fun remove(ownCar: OwnCar) {
        val dao = carDao()
        val pos = dao.getPosition(ownCar.id) ?: return
        dao.remove(CarEntity.fromOwnCar(ownCar, pos))
        dao.decreasePositionsAbove(pos)
    }
}