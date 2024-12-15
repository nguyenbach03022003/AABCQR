package com.app.src.abcqr.data.repository;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.app.src.abcqr.data.model.MyQRDao;
import com.app.src.abcqr.data.model.MyQR;
import com.app.src.abcqr.utils.Constant;
import com.app.src.abcqr.utils.DateTimeConverters;

@Database(entities = {MyQR.class}, version = 1, exportSchema = false)
@TypeConverters(DateTimeConverters.class)
public abstract class MyQRDatabase extends RoomDatabase {
    public abstract MyQRDao myQRDao();

    static MyQRDatabase INSTANCE = null;

    public static MyQRDatabase getInstance(Context context) {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        INSTANCE = Room.databaseBuilder(context, MyQRDatabase.class, Constant.TABLENAME)
                .allowMainThreadQueries().build();
        return INSTANCE;
    }
}
