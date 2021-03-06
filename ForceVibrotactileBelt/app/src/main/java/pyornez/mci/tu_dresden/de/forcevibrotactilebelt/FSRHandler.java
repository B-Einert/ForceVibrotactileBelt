package pyornez.mci.tu_dresden.de.forcevibrotactilebelt;

import android.app.Activity;
import android.widget.TextView;

/**
 * Created by Bjoern 1 on 11.12.2016.
 */

public class FSRHandler {
    private TextView fsr1, fsr2, fsr3, fsr4, fsr5, fsr6, fsr7, fsr8, roll, pitch, yaw, target;
    private long millis; //timestamp = milliseconds beginning from powering the arduino
    private Activity activity;
    private String message = "";
    private static final String FSR_TAG = "FSR";
    private int imu1, imu2;
    private boolean ready=false;

    public FSRHandler(Activity activity) {
        //initiation of the apps changing textviews
        this.activity = activity;
        this.fsr1 = ((TextView) activity.findViewById(R.id.fsr1));
        this.fsr2 = ((TextView) activity.findViewById(R.id.fsr2));
        this.fsr3 = ((TextView) activity.findViewById(R.id.fsr3));
        this.fsr4 = ((TextView) activity.findViewById(R.id.fsr4));
        this.fsr5 = ((TextView) activity.findViewById(R.id.fsr5));
        this.fsr6 = ((TextView) activity.findViewById(R.id.fsr6));
        this.fsr7 = ((TextView) activity.findViewById(R.id.fsr7));
        this.fsr8 = ((TextView) activity.findViewById(R.id.fsr8));
        this.roll = ((TextView) activity.findViewById(R.id.roll_field));
        this.pitch = ((TextView) activity.findViewById(R.id.pitch_field));
        this.yaw = ((TextView) activity.findViewById(R.id.yaw_field));
    }

    /**
     * handling of a obtained bluetooth message
     * @param msg - obtained message
     * @return true - successful handling of the message
     */
    public boolean obtainMessage(String msg) {
        ready=false;
        message = msg;
        //handle messages from imu Bsp: YPR:50.80,25.25,-18.21
        if (msg.charAt(0) == 'Y') {
            imu1 = msg.indexOf(",");
            imu2 = msg.indexOf(",", imu1 + 1);
            try {
                synchronized (this) {
                    wait(100);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            yaw.setText(message.substring(4, imu1));
                            pitch.setText(message.substring(imu1 + 1, imu2));
                            roll.setText(message.substring(imu2 + 1));
                            ready=true;
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //handle messages from FSRs Bsp: 0250(FSR:02, Value:50)
        //imu data can be read similarly but the Arduino implementation to construct the fitting
        // messages is circumstantial and needs much time to run
        else {
            try {
                //assignment of the Textview which is to be changed
                switch (Integer.parseInt(msg.substring(0, 2))) {
                    case 1:
                        target = fsr1;
                        break;
                    case 2:
                        target = fsr2;
                        break;
                    case 3:
                        target = fsr3;
                        break;
                    case 4:
                        target = fsr4;
                        break;
                    case 5:
                        target = fsr5;
                        break;
                    case 6:
                        target = fsr6;
                        break;
                    case 7:
                        target = fsr7;
                        break;
                    case 8:
                        target = fsr8;
                        break;
                    case 9:
                        target = roll;
                        break;
                    case 10:
                        target = pitch;
                        break;
                    case 11:
                        target = yaw;
                        break;
                    default:
                        target = null;
                        break;
                }
            } catch (NumberFormatException e){
                return false;
            }
            //timestamp
            millis=Long.parseLong(message.substring(message.indexOf(",")+1));
            System.out.println(millis);

            try {
                synchronized (this) {
                    wait(100);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //changing of the targets value
                            target.setText(message.substring(2,message.indexOf(",")));
                            ready=true;
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(true){
            if(!isReady()){
                continue;
            }
            break;
        }
        return true;
    }

    /**
     * indicates if the thread rewriting a textviews value is occupied
     * @return false if occupied, true if not
     */
    public boolean isReady(){
        return ready;
    }
}
