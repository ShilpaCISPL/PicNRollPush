package picnroll.shilpa_cispl.com.picnroll.sendpushnotifications;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by shilpa-cispl on 03/10/17.
 */

public class  MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    static String title, message;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        title = remoteMessage.getNotification().getTitle();
        message = remoteMessage.getNotification().getBody();
        sendMessage();

    }
    // Send an Intent with an action named "my-event".
    private void sendMessage() {
        Intent intent = new Intent("my-event");
        // add data
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}