package klmanansala.apps.jemimasgroceries.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(GroceriesDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(GroceriesContract.GroceryEntry.TABLE_NAME);
        tableNameHashSet.add(GroceriesContract.InventoryEntry.TABLE_NAME);

        mContext.deleteDatabase(GroceriesDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new GroceriesDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the grocery entry
        // and inventory entry tables
        assertTrue("Error: Your database was created without both the grocery entry and inventory entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + GroceriesContract.GroceryEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> groceryColumnHashSet = new HashSet<String>();
        groceryColumnHashSet.add(GroceriesContract.GroceryEntry._ID);
        groceryColumnHashSet.add(GroceriesContract.GroceryEntry.COLUMN_NAME);
        groceryColumnHashSet.add(GroceriesContract.GroceryEntry.COLUMN_QUANTITY);
        groceryColumnHashSet.add(GroceriesContract.GroceryEntry.COLUMN_STATUS);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            groceryColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required grocery
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required grocery entry columns",
                groceryColumnHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + GroceriesContract.InventoryEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> inventoryColumnHashSet = new HashSet<String>();
        inventoryColumnHashSet.add(GroceriesContract.InventoryEntry._ID);
        inventoryColumnHashSet.add(GroceriesContract.InventoryEntry.COLUMN_NAME);
        inventoryColumnHashSet.add(GroceriesContract.InventoryEntry.COLUMN_QUANTITY);
        inventoryColumnHashSet.add(GroceriesContract.InventoryEntry.COLUMN_STATUS);
        inventoryColumnHashSet.add(GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE);

        do {
            String columnName = c.getString(columnNameIndex);
            inventoryColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required grocery
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required inventory entry columns",
                inventoryColumnHashSet.isEmpty());

        db.close();
    }

    public void testGroceryTable() {
        // First step: Get reference to writable database
        SQLiteDatabase db = new GroceriesDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Create ContentValues of what you want to insert
        ContentValues groceryTestEntry = TestUtilities.createGroceryValues();

        // Insert ContentValues into database and get a row ID back
        long groceryRowId;
        groceryRowId = db.insert(GroceriesContract.GroceryEntry.TABLE_NAME, null, groceryTestEntry);
        assertTrue(groceryRowId != -1);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(GroceriesContract.GroceryEntry.TABLE_NAME, null, null, null, null, null, null);

        // Move the cursor to a valid database row
        assertTrue(cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Retrieved data from db does not match test data.", cursor, groceryTestEntry);

        // Finally, close the cursor and database
        cursor.close();
        db.close();
    }

    public void testInventoryTable() {

        // First step: Get reference to writable database
        SQLiteDatabase db = new GroceriesDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Create ContentValues of what you want to insert
        ContentValues inventoryTestEntry = TestUtilities.createInventoryValues();

        // Insert ContentValues into database and get a row ID back
        long groceryRowId;
        groceryRowId = db.insert(GroceriesContract.InventoryEntry.TABLE_NAME, null, inventoryTestEntry);
        assertTrue(groceryRowId != -1);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(GroceriesContract.InventoryEntry.TABLE_NAME, null, null, null, null, null, null);

        // Move the cursor to a valid database row
        assertTrue(cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Retrieved data from db does not match test data.", cursor, inventoryTestEntry);

        // Finally, close the cursor and database
        cursor.close();
        db.close();
    }

}
