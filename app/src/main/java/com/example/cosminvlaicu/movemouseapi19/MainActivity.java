package com.example.cosminvlaicu.movemouseapi19;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.util.Pair;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, android.hardware.SensorEventListener {
    boolean release = true;
    float dX;
    float dY;
    int lastAction;

    int xCoord;
    int yCoord;

    private SensorManager mSensorManager;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private float default_orientation = 0;


    int beginX;
    int beginY;

    int currentX;
    int currentY;

    DatagramSocket c;
    InetAddress foundInetAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //*** these layouts sense touch events ***//
        LinearLayout l1 = (LinearLayout) findViewById(R.id.ll1);
        LinearLayout l2 = (LinearLayout) findViewById(R.id.ll2);

        //*** for screen sizes ***//
        setBounds();

        //*** for sensing touch events ***//
        l1.setOnTouchListener(this);
        l2.setOnTouchListener(this);

        //*** get server IP ***//
        discoverServer("RESPONSE");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }



    public void discoverServer(final String serverName){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                new Thread() {
                    @Override
                    public void run() {
                        try{
                            c = new DatagramSocket();
                            c.setBroadcast(true);

                            byte[] sendData = "DISCOVER_SERVER".getBytes();

                            //*** try broadcast 255 ***//
                            try{
                                DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getByName("255.255.255.255"),8888);
                                c.send(sendPacket);

                                System.out.println(getClass().getName() + "Request sent to 255.255.255.255");
                            }catch(Exception e){

                            }

                            foundInetAddress=InetAddress.getByName("255.255.255.255");



                            //*** try all other interfaces ***//
                            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
                            while(interfaces.hasMoreElements()){
                                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                                //not self
                                if(networkInterface.isLoopback() || !networkInterface.isUp()){
                                    continue;
                                }

                                for(InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()){
                                    InetAddress broadcast = interfaceAddress.getBroadcast();

                                    //*** no error ***//
                                    if(broadcast==null){
                                        continue;
                                    }

                                    //*** send ***//
                                    try{
                                        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,broadcast,8888);
                                        c.send(sendPacket);
                                    }catch(Exception e){
                                    }

                                }

                                //*** prepare for receive ***//
                                byte[] recvBuf = new byte[15000];
                                DatagramPacket receivePacket = new DatagramPacket(recvBuf,recvBuf.length);
                                c.receive(receivePacket);

                                //*** response ***//
                                System.out.println(getClass().getName() + "response from server: " + receivePacket.getAddress().getHostAddress());  //get ip string in textual representation
                                String message = new String(receivePacket.getData()).trim();    //leading and trailing whitespaces omitted


                                //*** here you can name multiple SERVERS, to only pick the desired device ***//
                                //*** first server to send RESPONSE is the desired one, it exists a connection so break and remember the address ***//
                                if(message.equals(serverName)){
                                    byte[] a = receivePacket.getAddress().getAddress();
                                    foundInetAddress = receivePacket.getAddress();
                                    break;
                                }

                            }

                            //*** send device screen dimensions to server ***//
                            sendData = ("DIMENSIONS/"+(int)500+"/"+(int)500).getBytes();
                            DatagramPacket sendDimensions = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);
                            c.send(sendDimensions);


                        }catch(IOException ex){
                            System.err.println(ex);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }finally{
                            c.close();
                        }
                    }
                }.start();
            }
        }, 100);
    }

    public void setBounds(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        yCoord = size.y;
        xCoord = size.x;
    }

    public void click(View view){
        byte[] sendData = "CLICK/padding/padding".getBytes();
        final DatagramPacket sendClick = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

        new Thread() {
            @Override
            public void run() {
                try {
                    c.send(sendClick);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();




    }

    @Override
    public void onDestroy(){
        c.close();  //always close the sockets, otherwise reboot needed if exception occurs
        super.onDestroy();
    }

    public void sendText(View view){
        EditText editText = (EditText) findViewById(R.id.editText);
        String message  = "TEXT/"+editText.getText().toString()+"/padding";
        editText.setText("");


        byte[] sendData = message.getBytes();
        final DatagramPacket sendText = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

        new Thread() {
            @Override
            public void run() {
                try {
                    c.send(sendText);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    public void youtube(View view){
        EditText editText = (EditText) findViewById(R.id.editText);
        String message  = "YOUTUBE/"+editText.getText().toString()+"/padding";
        editText.setText("");


        byte[] sendData = message.getBytes();
        final DatagramPacket sendText = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

        new Thread() {
            @Override
            public void run() {
                try {
                    c.send(sendText);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    public void google(View view){
        EditText editText = (EditText) findViewById(R.id.editText);
        String message  = "GOOGLE/"+editText.getText().toString()+"/padding";
        editText.setText("");


        byte[] sendData = message.getBytes();
        final DatagramPacket sendText = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

        new Thread() {
            @Override
            public void run() {
                try {
                    c.send(sendText);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    public void link(View view){
        EditText editText = (EditText) findViewById(R.id.editText);
        String message  = "LINK"+editText.getText().toString();
        editText.setText("");


        byte[] sendData = message.getBytes();
        final DatagramPacket sendText = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

        new Thread() {
            @Override
            public void run() {
                try {
                    c.send(sendText);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    public void scrollUp(View view){
        String message  = "UP/padding/padding";


        byte[] sendData = message.getBytes();
        final DatagramPacket sendText = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

        new Thread() {
            @Override
            public void run() {
                try {
                    c.send(sendText);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void scrollDown(View view){
        String message  = "DOWN/padding/padding";


        byte[] sendData = message.getBytes();
        final DatagramPacket sendText = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

        new Thread() {
            @Override
            public void run() {
                try {
                    c.send(sendText);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void enter(View view){
        String message  = "ENTER/padding/padding";


        byte[] sendData = message.getBytes();
        final DatagramPacket sendText = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

        new Thread() {
            @Override
            public void run() {
                try {
                    c.send(sendText);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void backspace(View view){
        String message  = "BACKSPACE/padding/padding";


        byte[] sendData = message.getBytes();
        final DatagramPacket sendText = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

        new Thread() {
            @Override
            public void run() {
                try {
                    c.send(sendText);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {

        float x;
        float y;

        byte[] sendData;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                lastAction = MotionEvent.ACTION_DOWN;

                x = event.getRawX();
                y = event.getRawY();

                if(release){
                    release=false;
                    beginX = (int)x;
                    beginY = (int)y;
                }

                break;

            case MotionEvent.ACTION_MOVE:
                x = event.getRawX();
                y = event.getRawY();

                lastAction = MotionEvent.ACTION_MOVE;
                currentX = (int)x;
                currentY = (int)y;

                int sendX = currentX - beginX;
                int sendY = currentY - beginY;

                sendData = (""+sendX+"/"+sendY).getBytes();
                final DatagramPacket sendPosition = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            c.send(sendPosition);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();


                break;

            case MotionEvent.ACTION_UP:
                if (lastAction == MotionEvent.ACTION_DOWN) {
                    sendData = "CLICK/padding/padding".getBytes();
                    final DatagramPacket sendClick = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                c.send(sendClick);
                            } catch (IOException e) {
                                e.printStackTrace();
                                c.close();
                            }
                        }
                    }.start();

                    default_orientation = updateOrientationAngles();
                    release=true;
                }
                else{
                    release=true;

                    String message = "STOP/"+currentX+"/"+currentY;

                    sendData = message.getBytes();
                    final DatagramPacket sendStop = new DatagramPacket(sendData,sendData.length,foundInetAddress,8888);

                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                c.send(sendStop);
                            } catch (IOException e) {
                                e.printStackTrace();
                                c.close();
                            }
                        }
                    }.start();

                }
                break;

            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onResume() {     //discover the server again (slow for general purpose, so I deleted it) (just copy paste new Thread from Handler, onCreate)
        super.onResume();
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        mSensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading,
                    0, mAccelerometerReading.length);
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, mMagnetometerReading.length);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }
    //counterclockwise rotation
    Pair<Integer,Integer> newPoint(float x, float y){

        float orientation = updateOrientationAngles();
        double new_x = Math.cos(orientation)*x + Math.sin(orientation)*y;       //here replace -sin for clockwise
        double new_y = -Math.sin(orientation)*x + Math.cos(orientation)*y;

        Pair<Integer,Integer> result = new Pair<Integer,Integer>((int)new_x,(int)new_y);
        return result;
    }

    public float updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        mSensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);

        // "mRotationMatrix" now has up-to-date information.

        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);     //mOrientationAngles has 3 floats, first is z
        // "mOrientationAngles" now has up-to-date information.

        return mOrientationAngles[0] - default_orientation;
    }
}
