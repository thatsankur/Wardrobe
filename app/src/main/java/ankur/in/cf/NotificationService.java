package ankur.in.cf;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Created by ankur on 26/12/15.
 */
public class NotificationService extends IntentService {

    public NotificationService() {
        super("mainservice");
    }

    public NotificationService(String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        showNotification();
    }

    private void showNotification() {

        Uri soundUri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Let's see What wear today")
                .setContentText("Choose your outfit among many combinations you have!")
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, new Intent(this,
                                        MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .setSound(soundUri).setSmallIcon(R.drawable.wardrobe)
                .build();
        NotificationManagerCompat.from(this).notify(0, notification);
    }

}