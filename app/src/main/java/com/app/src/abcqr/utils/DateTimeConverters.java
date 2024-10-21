package com.app.src.abcqr.utils;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateTimeConverters {
    // Converters for Room to store Date and Time
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
