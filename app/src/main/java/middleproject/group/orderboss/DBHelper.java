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
    private static SQLiteDatabase database;
    private static String database_name = "OrderDB";

    public DBHelper(Context context){
        super(context,database_name,null,VERSION);
    }

    /*function to call database*/
    public static SQLiteDatabase getDatabase(Context context){
        if(database == null || !database.isOpen()){
            database = new DBHelper(context).getWritableDatabase();
        }
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DATABASE_CREATE_TABLE_MENU = "create table Menu ( _ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, Name TEXT, Price INTEGER)";
        String DATABASE_CREATE_TABLE_ORDER = "create table OrderMeal ( _ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, TableNum INTEGER, OrderItem TEXT, Price INTEGER, Send INTEGER DEFAULT 0)";

        db.execSQL(DATABASE_CREATE_TABLE_MENU);
        db.execSQL(DATABASE_CREATE_TABLE_ORDER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Menu");
        db.execSQL("DROP TABLE IF EXISTS OrderMeal");
        onCreate(db);
    }
}
