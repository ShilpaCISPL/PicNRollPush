package picnroll.shilpa_cispl.com.picnroll.sendpushnotifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import picnroll.shilpa_cispl.com.picnroll.customgallery.FolderImagesActivity;

/**
 * Created by shilpa-cispl on 04/10/17.
 */

public class Notification_reciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String mess = intent.getStringExtra("string");

        String test = intent.getStringExtra("test");
        Log.d("tag","HEYYmess--"+mess +"\n" +test);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent repeating_intent = new Intent(context,FolderImagesActivity.class);
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,100,repeating_intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setContentIntent(pendingIntent).
                setSmallIcon(android.R.drawable.arrow_up_float).setContentTitle(test).
                setContentText(mess).setAutoCancel(true);

        notificationManager.notify(100,builder.build());

    }
}
