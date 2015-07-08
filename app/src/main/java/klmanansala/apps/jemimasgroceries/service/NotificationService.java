package klmanansala.apps.jemimasgroceries.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Calendar;

import klmanansala.apps.jemimasgroceries.MainActivity;
import klmanansala.apps.jemimasgroceries.R;
import klmanansala.apps.jemimasgroceries.data.GroceriesContract;

/**
 * Created by kevin on 7/8/15.
 */
public class NotificationService extends IntentService {

    private static final String[] COLUMNS = {
            GroceriesContract.InventoryEntry._ID
            ,GroceriesContract.InventoryEntry.COLUMN_NAME
    };

    static final int COL_ID = 0;
    static final int COL_NAME = 1;

    private static final int NOTIFICATION_ID = 1234;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Calendar cal = Calendar.getInstance();
        long dateToday = cal.getTimeInMillis();

        Uri queryUri = GroceriesContract.InventoryEntry.buildInventoryWithDateUri(dateToday);
        Cursor cursor = this.getContentResolver().query(queryUri, COLUMNS, null, null, null);

        StringBuilder contentText = new StringBuilder();
        contentText.append(this.getString(R.string.expiring_items));

        if(cursor.moveToFirst()){
            do {
                String itemName = cursor.getString(COL_NAME);
                contentText.append(" " + itemName + ",");
            } while(cursor.moveToNext());
        }

        String title = this.getString(R.string.app_name);
        int iconId = R.mipmap.ic_launcher;

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this);
        notifBuilder.setContentText(contentText);
        notifBuilder.setContentTitle(title);
        notifBuilder.setSmallIcon(iconId);

        Intent notifIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(notifIntent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        notifBuilder.setContentIntent(pendingIntent);

        Notification notif = notifBuilder.build();
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notif);
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent notifIntent = new Intent(context, NotificationService.class);
            context.startService(notifIntent);
        }
    }
}
