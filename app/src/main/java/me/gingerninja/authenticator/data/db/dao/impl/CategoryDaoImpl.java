package me.gingerninja.authenticator.data.db.dao.impl;

import me.gingerninja.authenticator.data.db.dao.CategoryDao;
import me.gingerninja.authenticator.data.db.provider.DatabaseHandler;

public class CategoryDaoImpl implements CategoryDao {
    private final DatabaseHandler databaseHandler;

    public CategoryDaoImpl(DatabaseHandler databaseHandler) {
        this.databaseHandler = databaseHandler;
    }
}
