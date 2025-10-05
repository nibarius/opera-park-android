package se.barsk.park.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import se.barsk.park.datatypes.OwnCar

@Entity(tableName = "car_collection")
data class CarEntity(
        @PrimaryKey val uuid: String,
        @ColumnInfo(name = "position") val position: Int,
        @ColumnInfo(name = "regno") val regNo: String,
        @ColumnInfo(name = "owner") val owner: String,
        @ColumnInfo(name = "nickname") val nickName: String
) {
    companion object {
        fun fromOwnCar(ownCar: OwnCar, position: Int) = CarEntity(
            uuid = ownCar.id,
            position = position,
            regNo = ownCar.regNo,
            owner = ownCar.owner,
            nickName = ownCar.nickName
        )
    }
}
