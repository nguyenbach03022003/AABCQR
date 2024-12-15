package com.app.src.abcqr.data.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.app.src.abcqr.utils.Constant;

import java.util.List;

@Dao
public interface MyQRDao {
    @Insert
    void insert(MyQR myQR);

    @Delete
    void delete(MyQR myQR);

    @Query("SELECT * FROM " + Constant.TABLENAME + " ORDER BY " + Constant.TIME + " DESC")
    List<MyQR> getAll();
}
