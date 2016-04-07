package middleproject.group.orderboss;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by shimao on 2016/4/6.
 * Simply put the interaction with table Order in this class
 */
public class OrderMealDB {

    public static final String TABLE_NAME = "OrderMeal";

    public static final String KEY_ID = "_ID";
    public static final String Column_Table = "TableNum";
    public static final String Column_OrderItem = "OrderItem";
    public static final String Column_Price = "Price";
    public static final String Column_Send = "Send";

    private SQLiteDatabase db;

    public OrderMealDB(Context context){
        db = DBHelper.getDatabase(context);
    }

    public void close(){
        db.close();
    }

    public OrderMealItem insert(OrderMealItem item){
        ContentValues cv = new ContentValues();

        cv.put(Column_Table, item.getTable());
        cv.put(Column_OrderItem, item.getOrderItem());
        cv.put(Column_Price, item.getPrice());
        cv.put(Column_Send, item.getSend());

        long id = db.insert(TABLE_NAME, null, cv);

        item.setId(id);

        return item;
    }

    public boolean update(OrderMealItem item) {
        ContentValues cv = new ContentValues();

        cv.put(Column_Table, item.getTable());
        cv.put(Column_OrderItem, item.getOrderItem());
        cv.put(Column_Price, item.getPrice());
        cv.put(Column_Send, item.getSend());

        String where = KEY_ID + "=" + item.getId();
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    public ArrayList<OrderMealItem> getAll() {
        ArrayList<OrderMealItem> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public ArrayList<OrderMealItem> getNotSend() {
        ArrayList<OrderMealItem> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, Column_Send + "= 0", null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public ArrayList<OrderMealItem> getSended() {
        ArrayList<OrderMealItem> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, Column_Send + "= 1", null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public OrderMealItem getRecord(Cursor cursor) {
        OrderMealItem result = new OrderMealItem();
        result.setId(cursor.getLong(0));
        result.setTable(cursor.getInt(1));
        result.setOrderItem(cursor.getString(2));
        result.setPrice(cursor.getInt(3));
        result.setSend(cursor.getInt(4));
        return result;
    }

    public boolean delete(long id){
        String where = KEY_ID + "=" + id;
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    public void sample() {
        OrderMealItem item = new OrderMealItem(2, "蔥油餅22", 30, 0);
        OrderMealItem item2 = new OrderMealItem(3, "蔥油餅33", 33, 0);
        OrderMealItem item3 = new OrderMealItem(4, "蔥油餅44", 40, 1);
        OrderMealItem item4 = new OrderMealItem(5, "蔥油餅55", 55, 1);

        insert(item);
        insert(item2);
        insert(item3);
        insert(item4);
    }

}
