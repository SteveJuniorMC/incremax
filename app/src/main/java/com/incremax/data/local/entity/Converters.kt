package com.incremax.data.local.entity

import androidx.room.TypeConverter
import com.incremax.domain.model.ExerciseCategory
import com.incremax.domain.model.ExerciseType
import com.incremax.domain.model.IncrementFrequency
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(dateFormatter)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, dateFormatter) }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.format(dateTimeFormatter)

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it, dateTimeFormatter) }

    @TypeConverter
    fun fromExerciseType(type: ExerciseType): String = type.name

    @TypeConverter
    fun toExerciseType(value: String): ExerciseType = ExerciseType.valueOf(value)

    @TypeConverter
    fun fromExerciseCategory(category: ExerciseCategory): String = category.name

    @TypeConverter
    fun toExerciseCategory(value: String): ExerciseCategory = ExerciseCategory.valueOf(value)

    @TypeConverter
    fun fromIncrementFrequency(frequency: IncrementFrequency): String = frequency.name

    @TypeConverter
    fun toIncrementFrequency(value: String): IncrementFrequency = IncrementFrequency.valueOf(value)
}
