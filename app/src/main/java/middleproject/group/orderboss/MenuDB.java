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

        long id = db.insert(TABLE_NAME, null, cv);

        item.setId(id);

        return item;
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
        return result;
    }
}
