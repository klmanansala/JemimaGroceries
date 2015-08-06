package klmanansala.apps.jemimasgroceries.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class GroceriesProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private GroceriesDbHelper mOpenHelper;

    static final int GROCERIES = 100;
    static final int GROCERIES_WITH_NAME = 101;
    static final int INVENTORY = 200;
    static final int INVENTORY_WITH_NAME = 201;
    static final int INVENTORY_WITH_DATE = 202;
    static final int ITEMNAMES = 300;

    private static final String sGroceryNameSelectionUsingLike =
            GroceriesContract.GroceryEntry.COLUMN_NAME + " LIKE ? ";

    private static final String sInventoryItemNameSelectionUsingLike =
            GroceriesContract.InventoryEntry.COLUMN_NAME + " LIKE ? ";

    private static final String sInventoryItemNameSelectionUsingEqual =
            GroceriesContract.InventoryEntry.COLUMN_NAME + " = ? ";

    private static final String sInventoryLessThanGivenDateSelection =
            GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE + " <= ? ";

    public static final String sActiveGrocerySelection =
            GroceriesContract.GroceryEntry.COLUMN_STATUS + " = "
            + GroceriesContract.GroceryEntry.STATUS_ACTIVE + " ";

    public static final String sActiveInventorySelection =
            GroceriesContract.InventoryEntry.COLUMN_STATUS + " = "
            + GroceriesContract.InventoryEntry.STATUS_ACTIVE + " ";

    private Cursor getAllGroceries(String[] projection, String selection, String[] selectionArgs,
                                 String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(
                GroceriesContract.GroceryEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getAllInventory(String[] projection, String selection, String[] selectionArgs,
                                   String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(
                GroceriesContract.InventoryEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getAllItemNames(String[] projection, String selection, String[] selectionArgs,
                                   String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(
                GroceriesContract.ItemNameEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getGroceriesByName(Uri uri, String[] projection, String sortOrder) {
        String name = GroceriesContract.GroceryEntry.getNameFromUri(uri);

        String selection = sGroceryNameSelectionUsingLike;
        String[] selectionArgs = new String[]{"%" + name + "%"};

        return mOpenHelper.getReadableDatabase().query(
                GroceriesContract.GroceryEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getInventoryByName(Uri uri, String[] projection, String sortOrder){
        String name = GroceriesContract.InventoryEntry.getNameFromUri(uri);

        String selection = sInventoryItemNameSelectionUsingEqual;
        String[] selectionArgs = new String[]{name};

        return mOpenHelper.getReadableDatabase().query(
                GroceriesContract.InventoryEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getInventoryByDate(Uri uri, String[] projection, String sortOrder){
        String date = GroceriesContract.InventoryEntry.getDateFromUri(uri);

        String selection = sInventoryLessThanGivenDateSelection;
        String[] selectionArgs = new String[]{date};

        return mOpenHelper.getReadableDatabase().query(
                GroceriesContract.InventoryEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new GroceriesDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "groceries"
            case GROCERIES: {
                retCursor = getAllGroceries(projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "inventory"
            case INVENTORY: {
                retCursor = getAllInventory(projection, selection, selectionArgs, sortOrder);
                break;
            }
            // "groceries/*"
            case GROCERIES_WITH_NAME: {
                retCursor = getGroceriesByName(uri, projection, sortOrder);
                break;
            }
            // "inventory/name/*"
            case INVENTORY_WITH_NAME: {
                retCursor = getInventoryByName(uri, projection, sortOrder);
                break;
            }
            // "inventory/date/#"
            case INVENTORY_WITH_DATE: {
                retCursor = getInventoryByDate(uri, projection, sortOrder);
                break;
            }
            // "itemnames"
            case ITEMNAMES: {
                retCursor = getAllItemNames(projection, selection, selectionArgs, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case GROCERIES:
                return GroceriesContract.GroceryEntry.CONTENT_TYPE;
            case INVENTORY:
                return GroceriesContract.InventoryEntry.CONTENT_TYPE;
            case GROCERIES_WITH_NAME:
                return GroceriesContract.GroceryEntry.CONTENT_TYPE;
            case INVENTORY_WITH_DATE:
                return GroceriesContract.InventoryEntry.CONTENT_TYPE;
            case INVENTORY_WITH_NAME:
                return GroceriesContract.InventoryEntry.CONTENT_TYPE;
            case ITEMNAMES:
                return GroceriesContract.ItemNameEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri = null;

        switch (match) {
            case GROCERIES: {
                formatItemName(values);
                long _id = db.insert(GroceriesContract.GroceryEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = GroceriesContract.GroceryEntry.buildGroceryUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case INVENTORY: {
                formatItemName(values);
                normalizeDate(values);
                long _id = db.insert(GroceriesContract.InventoryEntry.TABLE_NAME, null, values);
                if ( _id > 0 ){
                    returnUri = GroceriesContract.InventoryEntry.buildInventoryUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case ITEMNAMES: {
                formatItemName(values);
                long _id = db.insert(GroceriesContract.ItemNameEntry.TABLE_NAME, null, values);
                if ( _id > 0 ){
                    returnUri = GroceriesContract.ItemNameEntry.buildItemNameUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE)) {
            long dateValue = values.getAsLong(GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE);
            values.put(GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE, GroceriesContract.normalizeDate(dateValue));
        }
    }

    private void formatItemName(ContentValues values){
        String key = null;
        String name = null;
        if (values.containsKey(GroceriesContract.ItemNameEntry.COLUMN_NAME)){
            key = GroceriesContract.ItemNameEntry.COLUMN_NAME;
            name = values.getAsString(GroceriesContract.ItemNameEntry.COLUMN_NAME);
        } else if(values.containsKey(GroceriesContract.GroceryEntry.COLUMN_NAME)){
            key = GroceriesContract.GroceryEntry.COLUMN_NAME;
            name = values.getAsString(GroceriesContract.GroceryEntry.COLUMN_NAME);
        } else if(values.containsKey(GroceriesContract.InventoryEntry.COLUMN_NAME)){
            key = GroceriesContract.InventoryEntry.COLUMN_NAME;
            name = values.getAsString(GroceriesContract.InventoryEntry.COLUMN_NAME);
        }

        if(name != null) {
            name = name.toLowerCase();
            name = name.replaceFirst(name.substring(0,1), name.substring(0,1).toUpperCase());
            values.put(key, name);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case GROCERIES: {
                rowsDeleted = db.delete(GroceriesContract.GroceryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case INVENTORY: {
                rowsDeleted = db.delete(GroceriesContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case ITEMNAMES: {
                rowsDeleted = db.delete(GroceriesContract.ItemNameEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if ( rowsDeleted != 0 || selection == null){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case GROCERIES: {
                formatItemName(values);
                rowsUpdated = db.update(GroceriesContract.GroceryEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case INVENTORY: {
                formatItemName(values);
                normalizeDate(values);
                rowsUpdated = db.update(GroceriesContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case ITEMNAMES: {
                formatItemName(values);
                rowsUpdated = db.update(GroceriesContract.ItemNameEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if ( rowsUpdated != 0 || selection == null){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        formatItemName(value);
                        normalizeDate(value);
                        long _id = db.insert(GroceriesContract.InventoryEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // GroceriesContract to help define the types to the UriMatcher.
        uriMatcher.addURI(GroceriesContract.CONTENT_AUTHORITY, GroceriesContract.PATH_GROCERIES, GROCERIES);
        uriMatcher.addURI(GroceriesContract.CONTENT_AUTHORITY, GroceriesContract.PATH_INVENTORY, INVENTORY);
        uriMatcher.addURI(GroceriesContract.CONTENT_AUTHORITY, GroceriesContract.PATH_GROCERIES + "/*", GROCERIES_WITH_NAME);
        uriMatcher.addURI(GroceriesContract.CONTENT_AUTHORITY, GroceriesContract.PATH_INVENTORY + "/name/*", INVENTORY_WITH_NAME);
        uriMatcher.addURI(GroceriesContract.CONTENT_AUTHORITY, GroceriesContract.PATH_INVENTORY + "/date/#", INVENTORY_WITH_DATE);
        uriMatcher.addURI(GroceriesContract.CONTENT_AUTHORITY, GroceriesContract.PATH_ITEMNAMES, ITEMNAMES);

        // 3) Return the new matcher!
        return uriMatcher;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
