package wifi.humberto.wifitoggler;

import android.app.Notification;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    WifiManager wifi = null;
    Context context = null;
    Thread thread = null;
    boolean keepRunning = true;
    boolean threadIsRunning = false;
    private NotificationUtils mNotificationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mNotificationUtils = new NotificationUtils(this);
    }

    public void buttonClicked(View view)
    {
        if (wifi == null) {
            return;
        }

//        int state = wifi.getWifiState();
//        String stateStr = "";
//
//        switch(state){
//            case WifiManager.WIFI_STATE_DISABLED:
//                stateStr = "Wifi is disabled";
//                break;
//            case WifiManager.WIFI_STATE_DISABLING:
//                stateStr = "Wifi is disabling";
//                break;
//            case WifiManager.WIFI_STATE_ENABLED:
//                stateStr = "Wifi is enabled";
//                break;
//            case WifiManager.WIFI_STATE_ENABLING:
//                stateStr = "Wifi is enabling";
//                break;
//            case WifiManager.WIFI_STATE_UNKNOWN:
//            default:
//                stateStr = "Wifi is unkown";
//                break;
//        }

        if(threadIsRunning)
        {
            Toast.makeText(context, "WifiToggle is already running!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(context, "WifiToggle started", Toast.LENGTH_SHORT).show();
            manageNotification();
        }
    }

    public Notification.Builder getChannelNotification(String title, String body) {
        return new Notification.Builder(getApplicationContext(), NotificationUtils.ANDROID_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.icon_small);
    }


    void manageNotification()
    {
        final Notification.Builder mBuilder = getChannelNotification("Toggling in progress", "Progress");


        mBuilder.setOngoing(true); // persistent
        // mId allows you to update the notification later on.
        mNotificationUtils.getManager().notify(1, mBuilder.build());

        //// TODO: 2/27/2017 migrate to TimerTask?
        thread =  new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        threadIsRunning = true;
                        //Get values from user
                        // Get wifi off minutes
                        EditText wifiOffView = (EditText)findViewById(R.id.wifiOffText);
                        String wifiOffString = wifiOffView.getText().toString();

                        // Get wifi on minutes
                        EditText wifiOnView = (EditText)findViewById(R.id.wifiOnText);
                        String wifiOnString = wifiOnView.getText().toString();

                        // Get quit after minutes
                        EditText quitAfterView = (EditText)findViewById(R.id.quitAfterText);
                        String quitAfterString = quitAfterView.getText().toString();

                        // Num minutes
                        int wifiOffMin = Integer.parseInt(wifiOffString);
                        int wifiOnMin = Integer.parseInt(wifiOnString);
                        int quitAfterMin = Integer.parseInt(quitAfterString);

                        long wifiOffMillis = wifiOffMin * 1000 * 60;
                        long wifiOnMillis = wifiOnMin * 1000 * 60;
                        long quitAfterMillis = quitAfterMin * 1000 * 60;


                        long startMillis = System.currentTimeMillis();
                        long millisLastToggle = startMillis;
                        boolean wifiOn = true;

                        int state = wifi.getWifiState();

                        if(state == WifiManager.WIFI_STATE_ENABLED) //wifi is enabled
                        {
                            wifiOn = true;
                        } else {
                            wifiOn = false;
                        }

                        while (keepRunning) {

                            long currMillis = System.currentTimeMillis() - startMillis;

                            if(currMillis >= quitAfterMillis)
                            {
                                //kill to loop
                                //// TODO: 2/27/2017 Show completed notification
                                break;
                            }

                            long millisPassed = System.currentTimeMillis() - millisLastToggle;

                            if(wifiOn == true)
                            {
                                // wifi is on, is it time to turn it off?
                                if( millisPassed  > wifiOffMillis){
                                    // time to turn it off
                                    wifiOn = false;
                                    wifi.setWifiEnabled(false);
                                    millisLastToggle = System.currentTimeMillis();
                                }
                            } else {
                                // wifi is off, is it time to turn it on?
                                if( millisPassed > wifiOnMillis){
                                    // time to turn it off
                                    wifiOn = true;
                                    wifi.setWifiEnabled(true);
                                    millisLastToggle = System.currentTimeMillis();
                                }
                            }


//                            int state = wifi.getWifiState();
//
//                            if(state == WifiManager.WIFI_STATE_ENABLED) //wifi is enabled
//                            {
//                                wifi.setWifiEnabled(false);
//                            } else {
//                                wifi.setWifiEnabled(true);
//                            }
                            int percent = (int) ( (currMillis * 100) / (quitAfterMillis));
                            mBuilder.setContentText("Progress "+ percent +"%");
                            mBuilder.setProgress(100, percent, false);
                            // Displays the progress bar for the first time.
                            mNotificationUtils.getManager().notify(1, mBuilder.build());
                            // Sleeps the thread, simulating an operation
                            // that takes time
                            try {
                                // Sleep for 10 seconds
                                Thread.sleep(5*1000);
                            } catch (InterruptedException e) {
                                Log.d("WifiToggle", "sleep failure");
                            }
                        }

                        keepRunning = true;
                        mBuilder.setOngoing(false); // make it not persistent
                        // When the loop is finished, updates the notification
                        mBuilder.setContentText("Toggling complete")
                                // Removes the progress bar
                                .setProgress(0,0,false);
                        mNotificationUtils.getManager().notify(1, mBuilder.build());
                        threadIsRunning = false;
                    }
                }
// Starts the thread by calling the run() method in its Runnable
        );

        thread.start();
    }

    public void stopButtonClicked(View view)
    {
        keepRunning = false;
        wifi.setWifiEnabled(true);
        Toast.makeText(context, "WifiToggle stopped", Toast.LENGTH_SHORT).show();
    }
}
