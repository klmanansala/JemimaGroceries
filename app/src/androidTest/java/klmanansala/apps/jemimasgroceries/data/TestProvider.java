package klmanansala.apps.jemimasgroceries.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import klmanansala.apps.jemimasgroceries.TestUtilities;

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
        Cursor weatherCursor = mContext.getContentResolver().query(
                GroceriesContract.GroceryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicGroceryQuery", weatherCursor, groceryValues);
    }
}
