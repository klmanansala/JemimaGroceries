package klmanansala.apps.jemimasgroceries;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;

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
        inventoryValues.put(GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE, 12345);

        return inventoryValues;
    }
}
