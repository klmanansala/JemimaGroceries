package klmanansala.apps.jemimasgroceries.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the database.
 */
public class GroceriesContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "klmanansala.apps.jemimasgroceries";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_GROCERIES = "groceries";
    public static final String PATH_INVENTORY = "inventory";
    public static final String PATH_ITEMNAMES = "itemnames";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static final class GroceryEntry implements BaseColumns {
        public static final String TABLE_NAME = "grocery";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_QUANTITY = "quantity";

        public static final String COLUMN_STATUS = "status";

        public static final String COLUMN_CHECKED = "checked";

        public static final int STATUS_ACTIVE = 1;
        public static final int STATUS_REMOVED = 0;

        public static final int CHECKED = 1;
        public static final int UNCHECKED = 0;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GROCERIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GROCERIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GROCERIES;

        public static Uri buildGroceryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getNameFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }
        
        public static Uri buildGroceriesWithNameUri(String name) {
            return CONTENT_URI.buildUpon().appendPath(name).build();
        }
    }

    public static final class InventoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "inventory";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_QUANTITY = "quantity";

        public static final String COLUMN_EXPIRATION_DATE = "expiration_date";

        public static final String COLUMN_STATUS = "status";

        public static final int STATUS_ACTIVE = 1;
        public static final int STATUS_REMOVED = 0;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_INVENTORY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static Uri buildInventoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getNameFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getDateFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }
        
        public static Uri buildInventoryWithNameUri(String name) {
            return CONTENT_URI.buildUpon()
                    .appendPath("name")
                    .appendPath(name).build();
        }
        
        public static Uri buildInventoryWithDateUri(long date) {
            long normalizedDate = normalizeDate(date);
            return CONTENT_URI.buildUpon()
                    .appendPath("date")
                    .appendPath(Long.toString(normalizedDate)).build();
        }
    }

    public static final class ItemNameEntry implements BaseColumns {
        public static final String TABLE_NAME = "itemname";

        public static final String COLUMN_NAME = "name";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEMNAMES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMNAMES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMNAMES;

        public static Uri buildItemNameUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
