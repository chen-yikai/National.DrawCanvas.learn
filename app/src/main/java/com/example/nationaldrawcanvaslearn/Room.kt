package com.example.nationaldrawcanvaslearn

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "draws")
data class Draw(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val draw: List<Line>
)

class Converters {
    @TypeConverter
    fun fromLineList(lines: List<Line>?): String? {
        if (lines == null) return null
        val jsonArray = JSONArray()
        lines.forEach { line ->
            val jsonObject = JSONObject().apply {
                put("start", JSONObject().apply {
                    put("x", line.start.x)
                    put("y", line.start.y)
                })
                put("end", JSONObject().apply {
                    put("x", line.end.x)
                    put("y", line.end.y)
                })
                put("colorArgb", line.color.toArgb())
                put("strokeWidth", line.strokeWidth)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toLineList(json: String?): List<Line>? {
        if (json == null) return null
        val jsonArray = JSONArray(json)
        val lines = mutableListOf<Line>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val startJson = jsonObject.getJSONObject("start")
            val endJson = jsonObject.getJSONObject("end")
            val line = Line(
                start = Offset(
                    x = startJson.getDouble("x").toFloat(),
                    y = startJson.getDouble("y").toFloat()
                ),
                end = Offset(
                    x = endJson.getDouble("x").toFloat(),
                    y = endJson.getDouble("y").toFloat()
                ),
                color = Color(jsonObject.getInt("colorArgb")),
                strokeWidth = jsonObject.getDouble("strokeWidth").toFloat()
            )
            lines.add(line)
        }
        return lines
    }
}

@Dao
interface DrawDao {
    @Insert
    suspend fun insert(draw: Draw)

    @Query("SELECT * FROM draws WHERE id = :id")
    fun get(id: Int): Flow<Draw>

    @Query("SELECT * FROM draws")
    fun getAll(): Flow<List<Draw>>

    @Query("DELETE FROM draws WHERE id = :id")
    suspend fun delete(id: Int)
}

@Database(entities = [Draw::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drawDao(): DrawDao
}