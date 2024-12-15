package com.app.src.abcqr.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.app.src.abcqr.utils.Constant;
import com.app.src.abcqr.utils.DateTimeConverters;

@Entity(tableName = Constant.TABLENAME)
@TypeConverters(DateTimeConverters.class)
public class MyQR {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = Constant.TYPE)
    private String type;

    @ColumnInfo(name = Constant.TIME)
    private Long time;

    @ColumnInfo(name = Constant.MESSAGE)
    private String message;

    public MyQR(String type, Long time, String message) {
        this.type = type;
        this.time = time;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
