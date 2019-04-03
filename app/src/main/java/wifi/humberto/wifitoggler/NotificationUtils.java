package wifi.humberto.wifitoggler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;

public class NotificationUtils  extends ContextWrapper {
    private NotificationManager mManager;

    public static final String ANDROID_CHANNEL_NAME = "TOGGLER CHANNEL";
    public static final String ANDROID_CHANNEL_ID = "1";

    public NotificationUtils(Context base) {
        super(base);
        createChannels();
    }

    public void createChannels() {

        NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        // Sets whether notifications posted to this channel appear on the lockscreen or not
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        getManager().createNotificationChannel(androidChannel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }
}
