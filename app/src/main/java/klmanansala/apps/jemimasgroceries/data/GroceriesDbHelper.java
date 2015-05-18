package klmanansala.apps.jemimasgroceries.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GroceriesDbHelper extends SQLiteOpenHelper{

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "groceries.db";

    public GroceriesDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + GroceriesContract.GroceryEntry.TABLE_NAME + " (" +
                GroceriesContract.GroceryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                GroceriesContract.GroceryEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
                GroceriesContract.GroceryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                GroceriesContract.GroceryEntry.COLUMN_STATUS + " INTEGER NOT NULL" +
                ");";

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + GroceriesContract.InventoryEntry.TABLE_NAME + " (" +
                GroceriesContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                GroceriesContract.InventoryEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL," +
                GroceriesContract.InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE + " INTEGER NOT NULL, " +
                GroceriesContract.InventoryEntry.COLUMN_STATUS + " INTEGER NOT NULL" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //for now whenever schema changes, drop the old tables and create the new ones
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GroceriesContract.GroceryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GroceriesContract.InventoryEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
