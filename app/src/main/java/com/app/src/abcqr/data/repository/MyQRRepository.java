package com.app.src.abcqr.data.repository;


import androidx.lifecycle.LiveData;

import com.app.src.abcqr.data.model.MyQRDao;
import com.app.src.abcqr.data.model.MyQR;

import java.util.List;

public class MyQRRepository {
    MyQRDao dao;

    public MyQRRepository(MyQRDao dao) {
        this.dao = dao;
    }

    public void insert(MyQR myQR) {
        dao.insert(myQR);
    }

    public void delete(MyQR myQR) {
        dao.delete(myQR);
    }

    public List<MyQR> getAll() {
        return dao.getAll();
    }
}
