package middleproject.group.orderboss;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLData;

/**
 * Created by shimao on 2016/4/6.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "Order_Helper";
    private static SQLiteDatabase database;

    public DBHelper(Context context,String name,SQLiteDatabase.CursorFactory factory,int version){
        super(context,DATABASE_NAME,null,VERSION);
    }

    /*function to call database*/
    public static SQLiteDatabase DatabaseCaller(Context context){
        if (database == null || !database.isOpen()){
            database = new DBHelper(context,DATABASE_NAME,null,VERSION).getWritableDatabase();
        }
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE_TABLE_MENU = "create table Menu ( _ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, Name TEXT, Price INTEGER)";
        String DATABASE_CREATE_TABLE_ORDER = "create table Order ( _ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, Name TEXT, Num INTEGER, price INTEGER)";

        db.execSQL(DATABASE_CREATE_TABLE_MENU);
        db.execSQL(DATABASE_CREATE_TABLE_ORDER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Menu");
        db.execSQL("DROP TABLE IF EXISTS Order");
        onCreate(db);
    }
}
