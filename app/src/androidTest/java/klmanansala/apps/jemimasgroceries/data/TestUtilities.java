package klmanansala.apps.jemimasgroceries.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import klmanansala.apps.jemimasgroceries.utils.PollingCheck;

public class TestUtilities extends AndroidTestCase {

    public static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    public static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    public static ContentValues createGroceryValues() {
        ContentValues groceryValues = new ContentValues();
        groceryValues.put(GroceriesContract.GroceryEntry.COLUMN_NAME, "dummy name");
        groceryValues.put(GroceriesContract.GroceryEntry.COLUMN_QUANTITY, 5);
        groceryValues.put(GroceriesContract.GroceryEntry.COLUMN_STATUS, 1);

        return groceryValues;
    }

    public static ContentValues createInventoryValues() {
        ContentValues inventoryValues = new ContentValues();
        inventoryValues.put(GroceriesContract.InventoryEntry.COLUMN_NAME, "dummy name");
        inventoryValues.put(GroceriesContract.InventoryEntry.COLUMN_QUANTITY, 5);
        inventoryValues.put(GroceriesContract.InventoryEntry.COLUMN_STATUS, 1);

        long dateToday = Calendar.getInstance().getTimeInMillis();
        dateToday = GroceriesContract.normalizeDate(dateToday);
        inventoryValues.put(GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE, dateToday);

        return inventoryValues;
    }

    /*
        The functions of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
