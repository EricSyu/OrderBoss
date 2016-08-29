package middleproject.group.orderboss;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shimao on 2016/4/6.
 * Simply put the interaction with table Menu in this class
 */
public class MenuDB {

    public static final String TABLE_NAME = "Menu";

    public static final String KEY_ID = "_ID";
    public static final String Column_Name = "Name";
    public static final String Column_Price = "Price";
    public static final String Column_Species = "Species";
    public static final String Column_SalesNum = "SalesNum";
    public static final String Column_Picture = "Picture";


    private SQLiteDatabase db;

    public MenuDB(Context context){
        db = DBHelper.getDatabase(context);
    }

    public void close(){
        db.close();
    }

    public MenuDBItem insert(MenuDBItem item){
        ContentValues cv = new ContentValues();

        cv.put(Column_Name, item.getName());
        cv.put(Column_Price, item.getPrice());
        cv.put(Column_Species, item.getSpecies());
        cv.put(Column_SalesNum, item.getSalesNum());
        cv.put(Column_Picture, item.getPictureName());

        long id = db.insert(TABLE_NAME, null, cv);

        item.setId(id);

        return item;
    }

    public boolean update(MenuDBItem item) {
        ContentValues cv = new ContentValues();

        cv.put(Column_Name, item.getName());
        cv.put(Column_Price, item.getPrice());
        cv.put(Column_Species, item.getSpecies());
        cv.put(Column_SalesNum, item.getSalesNum());
        cv.put(Column_Picture, item.getPictureName());

        String where = KEY_ID + "=" + item.getId();
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    public boolean incrementMealSalesNum(String mealName, int amount){
        Cursor cursor = db.query(TABLE_NAME, null, String.format("%s='%s'", Column_Name, mealName), null, null, null, null, null);
        MenuDBItem result = null;
        if (cursor.moveToNext()) {
             result = getRecord(cursor);
        }
        result.setSalesNum(result.getSalesNum() + amount);

        cursor.close();
        return update(result);
    }

    public ArrayList<MenuDBItem> getAll() {
        ArrayList<MenuDBItem> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public boolean delete(long id){
        String where = KEY_ID + "=" + id;
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    public MenuDBItem getRecord(Cursor cursor) {
        MenuDBItem result = new MenuDBItem();
        result.setId(cursor.getLong(0));
        result.setName(cursor.getString(1));
        result.setPrice(cursor.getInt(2));
        result.setSpecies(cursor.getString(3));
        result.setSalesNum(cursor.getInt(4));
        result.setPictureName(cursor.getString(5));
        return result;
    }
}
