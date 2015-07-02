package klmanansala.apps.jemimasgroceries.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Created by kevin on 5/14/15.
 */
public class TestProvider extends AndroidTestCase{

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                GroceriesContract.GroceryEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                GroceriesContract.InventoryEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                GroceriesContract.GroceryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Grocery table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                GroceriesContract.InventoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Inventory table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }


    /*
        This test checks to make sure that the content provider is registered correctly.

     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                GroceriesProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: GroceriesProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + GroceriesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, GroceriesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: GroceriesProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
         */
    public void testGetType() {
        // content://klmanansala.apps.jemimasgroceries/groceries/
        String type = mContext.getContentResolver().getType(GroceriesContract.GroceryEntry.CONTENT_URI);
        // vnd.android.cursor.dir/klmanansala.apps.jemimasgroceries/groceries
        assertEquals("Error: the GroceryEntry CONTENT_URI should return GroceryEntry.CONTENT_TYPE",
                GroceriesContract.GroceryEntry.CONTENT_TYPE, type);

        // content://klmanansala.apps.jemimasgroceries/inventory/
        type = mContext.getContentResolver().getType(GroceriesContract.InventoryEntry.CONTENT_URI);
        // vnd.android.cursor.dir/klmanansala.apps.jemimasgroceries/inventory
        assertEquals("Error: the InventoryEntry CONTENT_URI should return InventoryEntry.CONTENT_TYPE",
                GroceriesContract.InventoryEntry.CONTENT_TYPE, type);

        // content://klmanansala.apps.jemimasgroceries/groceries/dummy
        String groceryName = "dummy";
        type = mContext.getContentResolver().getType(GroceriesContract.GroceryEntry.buildGroceriesWithNameUri(groceryName));
        // vnd.android.cursor.dir/klmanansala.apps.jemimasgroceries/groceries
        assertEquals("Error: the GroceryEntry CONTENT_URI with name should return GroceryEntry.CONTENT_TYPE",
                GroceriesContract.GroceryEntry.CONTENT_TYPE, type);

        // content://klmanansala.apps.jemimasgroceries/inventory/name/dummy
        String inventoryName = "dummy";
        type = mContext.getContentResolver().getType(GroceriesContract.InventoryEntry.buildInventoryWithNameUri(inventoryName));
        // vnd.android.cursor.dir/klmanansala.apps.jemimasgroceries/inventory
        assertEquals("Error: the InventoryEntry CONTENT_URI with name should return InventoryEntry.CONTENT_TYPE",
                GroceriesContract.InventoryEntry.CONTENT_TYPE, type);

        long testDate = 1419120000L; // December 21st, 2014
        // content://klmanansala.apps.jemimasgroceries/inventory/date/1419120000
        type = mContext.getContentResolver().getType(GroceriesContract.InventoryEntry.buildInventoryWithDateUri(testDate));
        // vnd.android.cursor.dir/klmanansala.apps.jemimasgroceries/inventory
        assertEquals("Error: the InventoryEntry CONTENT_URI with name should return InventoryEntry.CONTENT_TYPE",
                GroceriesContract.InventoryEntry.CONTENT_TYPE, type);

    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicGroceryQuery() {
        // insert our test records into the database
        GroceriesDbHelper dbHelper = new GroceriesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues groceryValues = TestUtilities.createGroceryValues();

        long groceryRowId = db.insert(GroceriesContract.GroceryEntry.TABLE_NAME, null, groceryValues);
        assertTrue("Unable to Insert GroceryEntry into the Database", groceryRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor groceryCursor = mContext.getContentResolver().query(
                GroceriesContract.GroceryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicGroceryQuery", groceryCursor, groceryValues);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicInventoryQuery() {
        // insert our test records into the database
        GroceriesDbHelper dbHelper = new GroceriesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues inventoryValues = TestUtilities.createInventoryValues();

        long inventoryRowId = db.insert(GroceriesContract.InventoryEntry.TABLE_NAME, null, inventoryValues);
        assertTrue("Unable to Insert InventoryEntry into the Database", inventoryRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor inventoryCursor = mContext.getContentResolver().query(
                GroceriesContract.InventoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicInventoryQuery", inventoryCursor, inventoryValues);
    }

    public void testGetActiveGroceryQuery() {
        // insert our test records into the database
        GroceriesDbHelper dbHelper = new GroceriesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues groceryValues = TestUtilities.createGroceryValues();

        long groceryRowId = db.insert(GroceriesContract.GroceryEntry.TABLE_NAME, null, groceryValues);
        assertTrue("Unable to Insert GroceryEntry into the Database", groceryRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor groceryCursor = mContext.getContentResolver().query(
                GroceriesContract.GroceryEntry.CONTENT_URI,
                null,
                GroceriesProvider.sActiveGrocerySelection,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testGetActiveGroceryQuery", groceryCursor, groceryValues);
    }

    public void testGetGroceriesByNameQuery() {
        // insert our test records into the database
        GroceriesDbHelper dbHelper = new GroceriesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues groceryValues = TestUtilities.createGroceryValues();

        long groceryRowId = db.insert(GroceriesContract.GroceryEntry.TABLE_NAME, null, groceryValues);
        assertTrue("Unable to Insert GroceryEntry into the Database", groceryRowId != -1);

        db.close();

        Uri queryUri = GroceriesContract.GroceryEntry
                .buildGroceriesWithNameUri(groceryValues
                        .getAsString(GroceriesContract.GroceryEntry.COLUMN_NAME));

        // Test the basic content provider query
        Cursor groceryCursor = mContext.getContentResolver().query(
                queryUri,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testGetGroceriesByNameQuery", groceryCursor, groceryValues);
    }

    public void testGetInventoryByNameQuery() {
        // insert our test records into the database
        GroceriesDbHelper dbHelper = new GroceriesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues inventoryValues = TestUtilities.createInventoryValues();

        long inventoryRowId = db.insert(GroceriesContract.InventoryEntry.TABLE_NAME, null, inventoryValues);
        assertTrue("Unable to Insert InventoryEntry into the Database", inventoryRowId != -1);

        db.close();

        Uri queryUri = GroceriesContract.InventoryEntry
                .buildInventoryWithNameUri(inventoryValues
                        .getAsString(GroceriesContract.InventoryEntry.COLUMN_NAME));

        // Test the basic content provider query
        Cursor inventoryCursor = mContext.getContentResolver().query(
                queryUri,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testGetInventoryByNameQuery", inventoryCursor, inventoryValues);
    }

    public void testGetInventoryByDateQuery() {
        // insert our test records into the database
        GroceriesDbHelper dbHelper = new GroceriesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues inventoryValues = TestUtilities.createInventoryValues();

        long inventoryRowId = db.insert(GroceriesContract.InventoryEntry.TABLE_NAME, null, inventoryValues);
        assertTrue("Unable to Insert InventoryEntry into the Database", inventoryRowId != -1);

        db.close();

        Uri queryUri = GroceriesContract.InventoryEntry
                .buildInventoryWithDateUri(inventoryValues
                        .getAsLong(GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE));

        // Test the basic content provider query
        Cursor inventoryCursor = mContext.getContentResolver().query(
                queryUri,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testGetInventoryByDateQuery", inventoryCursor, inventoryValues);
    }

    /*
        This test uses the provider to insert and then update the data.
     */
    public void testUpdateGrocery() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createGroceryValues();

        Uri groceryUri = mContext.getContentResolver().
                insert(GroceriesContract.GroceryEntry.CONTENT_URI, values);
        long groceryRowId = ContentUris.parseId(groceryUri);

        // Verify we got a row back.
        assertTrue(groceryRowId != -1);
        Log.d(LOG_TAG, "New row id: " + groceryRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(GroceriesContract.GroceryEntry._ID, groceryRowId);
        updatedValues.put(GroceriesContract.GroceryEntry.COLUMN_NAME, "updated name");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor groceryCursor = mContext.getContentResolver()
                .query(GroceriesContract.GroceryEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        groceryCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                GroceriesContract.GroceryEntry.CONTENT_URI, updatedValues
                , GroceriesContract.GroceryEntry._ID + "= ?",
                new String[] { Long.toString(groceryRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        groceryCursor.unregisterContentObserver(tco);
        groceryCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                GroceriesContract.GroceryEntry.CONTENT_URI,
                null,   // projection
                GroceriesContract.GroceryEntry._ID + " = " + groceryRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateGrocery.  Error validating grocery entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    /*
        This test uses the provider to insert and then update the data. Uncomment this test to
        see if your update location is functioning correctly.
     */
    public void testUpdateInventory() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createInventoryValues();

        Uri inventoryUri = mContext.getContentResolver().
                insert(GroceriesContract.InventoryEntry.CONTENT_URI, values);
        long inventoryRowId = ContentUris.parseId(inventoryUri);

        // Verify we got a row back.
        assertTrue(inventoryRowId != -1);
        Log.d(LOG_TAG, "New row id: " + inventoryRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(GroceriesContract.InventoryEntry._ID, inventoryRowId);
        updatedValues.put(GroceriesContract.InventoryEntry.COLUMN_NAME, "new name");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor inventoryCursor = mContext.getContentResolver()
                .query(GroceriesContract.InventoryEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        inventoryCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                GroceriesContract.InventoryEntry.CONTENT_URI, updatedValues
                , GroceriesContract.InventoryEntry._ID + "= ?",
                new String[] { Long.toString(inventoryRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // Students: If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        inventoryCursor.unregisterContentObserver(tco);
        inventoryCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                GroceriesContract.InventoryEntry.CONTENT_URI,
                null,   // projection
                GroceriesContract.InventoryEntry._ID + " = " + inventoryRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateInventory.  Error validating inventory entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createGroceryValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(GroceriesContract.GroceryEntry.CONTENT_URI, true, tco);
        Uri groceryUri = mContext.getContentResolver().insert(GroceriesContract.GroceryEntry.CONTENT_URI, testValues);

        // Did our content observer get called?  If this fails, your insert grocery
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long groceryRowId = ContentUris.parseId(groceryUri);

        // Verify we got a row back.
        assertTrue(groceryRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                GroceriesContract.GroceryEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating GroceryEntry.",
                cursor, testValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues inventoryValues = TestUtilities.createInventoryValues();
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(GroceriesContract.InventoryEntry.CONTENT_URI, true, tco);

        Uri inventoryInsertUri = mContext.getContentResolver()
                .insert(GroceriesContract.InventoryEntry.CONTENT_URI, inventoryValues);
        assertTrue(inventoryInsertUri != null);

        // Did our content observer get called?  If this fails, your insert inventory
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor inventoryCursor = mContext.getContentResolver().query(
                GroceriesContract.InventoryEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating InventoryEntry insert.",
                inventoryCursor, inventoryValues);

    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver groceryObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(GroceriesContract.GroceryEntry.CONTENT_URI, true, groceryObserver);

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver inventoryObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(GroceriesContract.InventoryEntry.CONTENT_URI, true, inventoryObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        groceryObserver.waitForNotificationOrFail();
        inventoryObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(groceryObserver);
        mContext.getContentResolver().unregisterContentObserver(inventoryObserver);
    }
}
