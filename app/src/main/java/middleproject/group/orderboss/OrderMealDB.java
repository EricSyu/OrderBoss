package middleproject.group.orderboss;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    public static final String Column_Date = "Date";

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
        cv.put(Column_Date, item.getDate());

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
        cv.put(Column_Date, item.getDate());

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
        Cursor cursor = db.query(TABLE_NAME, null, String.format("%s=0 AND %s='%s'", Column_Send, Column_Date, getCurrentDate()), null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public ArrayList<OrderMealItem> getSended() {
        ArrayList<OrderMealItem> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, String.format("%s=1 AND %s='%s'", Column_Send, Column_Date, getCurrentDate()), null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public ArrayList<OrderMealItem> getAllRecord() {
        ArrayList<OrderMealItem> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, String.format("%s=2", Column_Send), null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }

        cursor.close();
        return result;
    }

    public ArrayList<OrderMealItem> getTodayOutside() {
        ArrayList<OrderMealItem> result = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, String.format("%s='%s' AND %s<0", Column_Date, getCurrentDate(), Column_Table), null, null, null, null, null);

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
        result.setDate(cursor.getString(5));
        return result;
    }

    private String getCurrentDate(){
        SimpleDateFormat dateStringFormat = new SimpleDateFormat( "yyyy-MM-dd" );
        Date date=new Date();
        String dataStr = dateStringFormat.format(date);
        return dataStr;
    }

    @Deprecated
    public boolean delete(long id){
        String where = KEY_ID + "=" + id;
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    public void sample() {
        OrderMealItem item = new OrderMealItem(1, "雞肉飯x2,乾麵x1", 125, 0, getCurrentDate());
        OrderMealItem item2 = new OrderMealItem(2, "肉燥飯x3,乾麵x1,可樂x3", 255, 0, getCurrentDate());
        OrderMealItem item3 = new OrderMealItem(3, "乾麵x1,湯麵x1", 95, 0, getCurrentDate());
        OrderMealItem item4 = new OrderMealItem(4, "雞肉飯x1,肉燥飯x1,乾麵x1,湯麵x1,可樂x1,麥香奶茶x1", 215, 0, getCurrentDate());

        insert(item);
        insert(item2);
        insert(item3);
        insert(item4);
    }

    public void sample2() {
        OrderMealItem item = new OrderMealItem(1, "", 2515, 2, "2016-05-08");
        OrderMealItem item2 = new OrderMealItem(2, "蛤蠣湯x1", 8354, 2, "2016-04-27");
        OrderMealItem item3 = new OrderMealItem(3, "蛤蠣湯x1", 3200, 2, "2016-03-22");
        OrderMealItem item4 = new OrderMealItem(4, "蛤蠣湯x1", 3451, 2, "2016-02-12");
        OrderMealItem item5 = new OrderMealItem(5, "蛤蠣湯x1", 4571, 2, "2016-06-12");

        insert(item);
        insert(item2);
        insert(item3);
        insert(item4);
        insert(item5);
    }

    public void sample3(){
        OrderMealItem item = new OrderMealItem(1, "蛤蠣湯x1", 5215, 2, "2016-03-15");
        OrderMealItem item2 = new OrderMealItem(2, "蛤蠣湯x1", 3854, 2, "2016-05-24");
        OrderMealItem item3 = new OrderMealItem(3, "蛤蠣湯x1", 3200, 2, "2016-02-13");
        OrderMealItem item4 = new OrderMealItem(4, "蛤蠣湯x1", 2451, 2, "2016-04-25");
        OrderMealItem item5 = new OrderMealItem(5, "蛤蠣湯x1", 2458, 2, "2016-02-05");

        insert(item);
        insert(item2);
        insert(item3);
        insert(item4);
        insert(item5);
    }

}
