package pyornez.mci.tu_dresden.de.forcevibrotactilebelt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

// for further explanation of the bluetooth implementation
// https://frag-duino.de/index.php/maker-faq/33-verwenden-eines-bluetooth-moduls-mit-einem-arduino-und-android


public class MainActivity extends AppCompatActivity {

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String BLUE_TAG = "FRAGDUINO";

    // Variablen
    private BluetoothAdapter adapter = null;
    private BluetoothSocket socket = null;
    private OutputStream stream_out = null;
    private InputStream stream_in = null;
    private boolean fsrConnected = false;
    private static String mac_adresse ="20:16:04:20:86:36"; // MAC Adresse des Bluetooth Adapters
    private FSRReceiveThread mFSRReceiveThread;
    private FSRHandler fsrHandler;
    private Boolean blueActive=false; //stops the bluetooth reception thread when set false

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d(BLUE_TAG, "Bluetooth: OnCreate");

        // Verbindung mit Bluetooth-Adapter herstellen
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            Toast.makeText(this, "Bitte Bluetooth aktivieren",
                    Toast.LENGTH_LONG).show();
            Log.d(BLUE_TAG,
                    "onCreate: Bluetooth Fehler: Deaktiviert oder nicht vorhanden");
            finish();
            return;
        } else
            Log.d(BLUE_TAG, "onCreate: Bluetooth-Adapter ist bereit");
        fsrHandler = new FSRHandler(this);
    }

    public void fsrConnect(View view) {
        if(fsrConnected){
            fsrDisconnect();
            return;
        }
        Log.d(BLUE_TAG, "Verbinde mit " + mac_adresse);

        BluetoothDevice remote_device = adapter.getRemoteDevice(mac_adresse);

        // Socket erstellen
        try {
            socket = remote_device.createInsecureRfcommSocketToServiceRecord(uuid);
            Log.d(BLUE_TAG, "Socket erstellt");
        } catch (Exception e) {
            Log.e(BLUE_TAG, "Socket Erstellung fehlgeschlagen: " + e.toString());
        }

        adapter.cancelDiscovery();

        // Socket verbinden
        try {
            socket.connect();
            Log.d(BLUE_TAG, "Socket verbunden");
            fsrConnected = true;
        } catch (IOException e) {
            fsrConnected = false;
            Log.e(BLUE_TAG, "Socket kann nicht verbinden: " + e.toString());
        }

        // Socket beenden, falls nicht verbunden werden konnte
        if (!fsrConnected) {
            try {
                socket.close();
            } catch (Exception e) {
                Log.e(BLUE_TAG,
                        "Socket kann nicht beendet werden: " + e.toString());
            }
        }

        // Outputstream erstellen:
        try {
            stream_out = socket.getOutputStream();
            Log.d(BLUE_TAG, "OutputStream erstellt");
        } catch (IOException e) {
            Log.e(BLUE_TAG, "OutputStream Fehler: " + e.toString());
            fsrConnected = false;
        }

        // Inputstream erstellen
        try {
            stream_in = socket.getInputStream();
            Log.d(BLUE_TAG, "InputStream erstellt");
        } catch (IOException e) {
            Log.e(BLUE_TAG, "InputStream Fehler: " + e.toString());
            fsrConnected = false;
        }

        if (fsrConnected) {
            Toast.makeText(this, "Verbunden mit " + mac_adresse,
                    Toast.LENGTH_LONG).show();
            ((Button) findViewById(R.id.btnFsrCon))
                    .setBackgroundColor(Color.GREEN);
            ((Button) findViewById(R.id.btnFsrCon))
                    .setText(R.string.fsr_disconnect);
            receiveData();
        } else {
            Toast.makeText(this, "Verbindungsfehler mit " + mac_adresse,
                    Toast.LENGTH_LONG).show();
            ((Button) findViewById(R.id.btnFsrCon))
                    .setBackgroundColor(Color.RED);
        }
    }

    /**
     * disconnection of bluetooth
     */
    private void fsrDisconnect() {
        blueActive=false;
        if (fsrConnected && stream_out != null) {
            fsrConnected = false;
            ((Button) findViewById(R.id.btnFsrCon))
                    .setBackgroundColor(Color.RED);
            ((Button) findViewById(R.id.btnFsrCon))
                    .setText(R.string.fsr_connect);
            Log.d(BLUE_TAG, "Trennen: Beende Verbindung");
            try {
                stream_out.flush();
                socket.close();
            } catch (IOException e) {
                Log.e(BLUE_TAG,
                        "Fehler beim beenden des Streams und schliessen des Sockets: "
                                + e.toString());
            }
            if (mFSRReceiveThread != null) {
                mFSRReceiveThread= null;
                Log.i(BLUE_TAG, "ReceiveThread canceled");
            }
        } else
            Log.d(BLUE_TAG, "Trennen: Keine Verbindung zum beenden");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(BLUE_TAG, "onDestroy. Trenne Verbindung, falls vorhanden");
        fsrDisconnect();
    }


    /**
     * start a new Thread receiving the bluetooth data
     */
    private void receiveData(){
        blueActive=true;
        mFSRReceiveThread = new FSRReceiveThread(socket);
        mFSRReceiveThread.start();
    }


    /**
     * Thread for receiving bluetooth signals
     */
    private class FSRReceiveThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;

        public FSRReceiveThread(BluetoothSocket socket){
            this.mmSocket=socket;
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(BLUE_TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
        }

        public void run() {
            Log.i(BLUE_TAG, "BEGIN mFSRReceiveThread");
            byte[] buffer = new byte[1024];
            String msg="";
            int length;
            // Keep listening to the InputStream while connected
            while (true) {
                if(!blueActive)break;
                try {
                    if (mmInStream.available() > 0) {
                        length = mmInStream.read(buffer);
                        //put together the message
                        for (int i = 0; i < length; i++) {
                            if ((char) buffer[i] == '#') {
                                if (!msg.isEmpty()) {
                                    Log.i(BLUE_TAG, "Message: " + msg);
                                    //handle message
                                    fsrHandler.obtainMessage(msg);
                                    msg = "";
                                }
                            } else {
                                msg += (char) buffer[i];
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(BLUE_TAG, "Fehler beim Empfangen: " + e.toString());
                }
            }
        }
    }
}
