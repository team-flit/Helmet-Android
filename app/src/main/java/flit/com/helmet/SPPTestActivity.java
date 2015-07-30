package flit.com.helmet;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import nl.littlerobots.bean.Bean;
import nl.littlerobots.bean.BeanDiscoveryListener;
import nl.littlerobots.bean.BeanListener;
import nl.littlerobots.bean.BeanManager;
import nl.littlerobots.bean.message.ScratchData;

/*
import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanDiscoveryListener;
import com.punchthrough.bean.sdk.BeanListener;
import com.punchthrough.bean.sdk.BeanManager;
import com.punchthrough.bean.sdk.message.BeanError;
import com.punchthrough.bean.sdk.message.ScratchBank;
import com.punchthrough.bean.sdk.message.ScratchData;
*/

public class SPPTestActivity extends AppCompatActivity {

    private final static String TAG = SPPTestActivity.class.getSimpleName();
    private BluetoothAdapter btAdapter;

//    private RecyclerView recyclerView;

    private ListView listView;
    private Button btnW;
    private Button btnA;
    private Button btnS;
    private Button btnD;

    private TextView txtBeanConnection;
    private TextView txtBean;
    private TextView txtConnection;
    private TextView txtGpsStat;
    private TextView txtGpsSpeed;

    private BTConnection connection;

    private LocationManager locationManager;
    private LocationListener locationListener;

    float curSpeed, maxSpeed;

    final ArrayList<Bean> beans = new ArrayList<>();

    Handler beanHandler = new Handler(){
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage Bean " + msg.obj);
            final Bean bean = (Bean) msg.obj;
            bean.readScratchData(1, new nl.littlerobots.bean.message.Callback<ScratchData>() {
                @Override
                public void onResult(ScratchData scratchData) {
                    Log.d(TAG, "onResult" + scratchData.getDataAsString());
                    if(scratchData.data().length>0) {
                        byte read = scratchData.data()[0];
                        txtBean.setText("Bean : " + (int) read);
                    }else{
                        Log.d(TAG, "handleMessage Bean : data length 0");
                    }
                    Message newMsg = Message.obtain();
                    newMsg.obj = bean;
//
                    beanHandler.sendMessageDelayed(newMsg, 2000);
                }
            });
//            bean.readScratchData(/ ScratchBank.BANK_1, new com.punchthrough.bean.sdk.message.Callback<ScratchData>() {
//                @Override
//                public void onResult(ScratchData result) {
//                    byte read = result.data()[0];
//                    txtBean.setText("Bean : " + (int)read);
//
//                    beanHandler.sendMessageDelayed(msg, 500);
//                }
//            });

        }
    };
    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_spptest);
//        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
//        recyclerView.setAdapter(btAdapter);
//        this.listView = (ListView) findViewById(R.id.listView);

        btnW = (Button)findViewById(R.id.buttonW);
        btnA = (Button)findViewById(R.id.buttonA);
        btnS = (Button)findViewById(R.id.buttonS);
        btnD = (Button)findViewById(R.id.buttonD);

        txtConnection = (TextView)findViewById(R.id.textView);
        txtBeanConnection = (TextView)findViewById(R.id.textView1);
        txtBean = (TextView) findViewById(R.id.textViewBean);

        txtGpsStat = (TextView)findViewById(R.id.textView2);
        txtGpsSpeed = (TextView)findViewById(R.id.textView3);

        txtGpsSpeed.setText("");
        txtGpsStat.setText("gps init...");

//        listView.setAdapter(btAdapter);
        View.OnClickListener btnClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ViewUtil.onClick(view);
                checkBTConnect();
                String tag = view.getTag().toString();
                if(connection!=null) {
//                    boolean ret =
                        connection.write(tag);
//                    if(ret==false){
//                        Toast.makeText(SPPTestActivity.this, "not sent", Toast.LENGTH_SHORT).show();;
//                        connection.cancel();;
//
//                    }
                }else{
                    Toast.makeText(SPPTestActivity.this, "disconnected", Toast.LENGTH_SHORT).show();;
                }
            }

        };
        btnW.setOnClickListener(btnClickListener);
        btnA.setOnClickListener(btnClickListener);
        btnS.setOnClickListener(btnClickListener);
        btnD.setOnClickListener(btnClickListener);

        curSpeed = maxSpeed = 0;
        checkBTConnect();
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if(location!=null){

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    txtGpsStat.setText("gps ok");
                    curSpeed = location.getSpeed();
                    // location.getSpeed() returns m/s * 3.6 = km/h
                    curSpeed = curSpeed * 3.6f;

                    if(curSpeed > maxSpeed)
                        maxSpeed = curSpeed;

                    String speedText = String.format(
                            "speed : %.2f km/h \nlat : %.4f lng : %.4f",
                            curSpeed, lat, lng);
                    txtGpsSpeed.setText(speedText);

                }
            }
            public void onStatusChanged(String s, int i, Bundle bundle) {}
            public void onProviderEnabled(String s) {}
            public void onProviderDisabled(String s) {}
        };



//            @Override
//            public void onBeanDiscovered(final Bean bean, int rssi) {
//                Log.d(TAG, "onBeanDiscovered " + bean.getDevice().getName() + " " + rssi);
//
//                beans.add(bean);
////                ScratchBank bank =
//            }
//
//            @Override
//            public void onDiscoveryComplete() {
//                Log.d(TAG, "onDiscoveryComplete");
//                try {
//                    Thread.sleep(1000L);
//                } catch (InterruptedException e) {
//                }
//                if(beans.size()>0)
//                    connectBean(beans.get(0));
//            }
//        };




////        java.lang.Thread.sleep(5000);
//        for (Bean bean : beans) {
////            bean.get
////            bean.getDevice().getName()
//            System.out.println(bean.getDevice().getName());
//        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        for(Bean bean : beans){
            if(bean.isConnected())
                bean.disconnect();
        }

        locationManager.removeUpdates(locationListener);

    }

    @Override
    protected void onResume() {
        super.onResume();

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        BeanDiscoveryListener listener = new BeanDiscoveryListener() {
            public void onBeanDiscovered(Bean bean) {
                Log.d(TAG, "onBeanDiscovered " + bean.getDevice().getName());
//
                beans.add(bean);
            }

            public void onDiscoveryComplete() {
                BeanManager.getInstance().cancelDiscovery();
                Log.d(TAG, "onDiscoveryComplete");
//                try {
//                    Thread.sleep(1000L);
//                } catch (InterruptedException e) {
//                }
                if (beans.size() > 0)
                    connectBean(beans.get(0));
            }
        };

        Log.d("SPPTEST", "startDiscovery");
        BeanManager.getInstance().startDiscovery(listener);
    }

    private void checkBTConnect() {
        if(connection == null && HelmetApp.getConnection()!=null){
            connection = HelmetApp.getConnection();
        }

        if(connection==null){
            Toast.makeText(this, "connection error", Toast.LENGTH_SHORT).show();
//            finish();
        }

        if(connection==null)//  // !connection.isAlive() || connection.isInterrupted())
        {
                        txtConnection.setText("BT : disconnected");
        }
//        else{
//            txtConnection.setText("BT : connected");
//        }

    }

    private void connectBean(final Bean bean){
//        bean.getDevice().
//        bean.
        bean.connect(context, new BeanListener() {
            public void onConnected() {
                Log.d(TAG, "onConnected");
//                Message msg = Message.obtain();
//                msg.obj = bean;
//                beanHandler.sendMessage(msg);
            }


            public void onConnectionFailed() {
                Log.d(TAG, "onConnectionFailed");

            }

            public void onDisconnected() {
                Log.d(TAG, "onDisconnected");

            }

            public void onSerialMessageReceived(byte[] bytes) {
                String str = new String(bytes, 0 , bytes.length);

                Log.d(TAG, "*onSerialMessageReceived b:"+bytes.length+ " " + str);
            }

            public void onScratchValueChanged(int i, byte[] bytes) {

                Log.d(TAG, "**onScratchValueChanged " + i + " " + Integer.toHexString(bytes[0]));

                if(i==0 ){
                    int value = bytes[0];
                    String send = "";
                    if(value==4)
                        send = "A";
                    else if(value==6)
                        send = "D";

                    if(send.length()>0 && connection!=null)
                        connection.write(send);
                    displayDirection(send);

                }

            }


//            @Override
//            public void onConnected() {
//                Log.d(TAG, "onConnected");
//                Message msg = Message.obtain();
//                msg.obj = bean;
//                beanHandler.sendMessage(msg);
//            }
//
//            @Override
//            public void onConnectionFailed() {
//                Log.d(TAG, "onConnectionFailed");
//            }
//
//            @Override
//            public void onDisconnected() {
//                Log.d(TAG, "onDisconnected");
//
//            }
//
//            @Override
//            public void onSerialMessageReceived(byte[] data) {
//
//                Log.d(TAG, "onSerialMessageReceived " +
//                        Integer.toHexString(data[0]));
//
//            }
//
//            @Override
//            public void onScratchValueChanged(ScratchBank bank, byte[] value) {
//
//            }
//
//            @Override
//            public void onError(BeanError error) {
//                Log.d(TAG, "onError");
//            }
        });
    }

    private void displayDirection(String value) {
        txtBean.setText(value);
        txtBean.postDelayed(new Runnable(){
            public void run() {
                txtBean.setText("");
            }
        }, 1000L);
    }

}
