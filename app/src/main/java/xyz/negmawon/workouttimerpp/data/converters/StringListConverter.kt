package xyz.negmawon.workouttimerpp.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Converts a list of strings to a JSON string and back.
 * Room calls these automatically for any `List<String>` column.
 */
class StringListConverter {

    private val gson = Gson()
    private val type = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun fromJson(json: String?): List<String> =
        json?.let { gson.fromJson(it, type) } ?: emptyList()

    @TypeConverter
    fun toJson(list: List<String>?): String =
        gson.toJson(list ?: emptyList<String>(), type)
}