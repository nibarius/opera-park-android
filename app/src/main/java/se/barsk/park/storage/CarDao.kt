package se.barsk.park.storage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(car: CarEntity)

    @Delete
    suspend fun remove(car: CarEntity)

    @Query("SELECT * FROM car_collection ORDER BY position ASC")
    fun fetchAllCars(): Flow<List<CarEntity>>

    @Query("SELECT position FROM car_collection WHERE uuid = :id")
    suspend fun getPosition(id: String): Int?

    @Query("UPDATE car_collection SET position = position - 1 WHERE position > :position")
    suspend fun decreasePositionsAbove(position: Int)
}