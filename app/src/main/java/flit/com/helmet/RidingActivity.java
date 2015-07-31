package flit.com.helmet;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import nl.littlerobots.bean.Bean;
import nl.littlerobots.bean.BeanDiscoveryListener;
import nl.littlerobots.bean.BeanListener;
import nl.littlerobots.bean.BeanManager;

public class RidingActivity extends AppCompatActivity
        implements BTConnection.ExceptionHandler
{

    private final static  String TAG = RidingActivity.class.getSimpleName();
    private BTService btService;
    private BluetoothAdapter btAdapter;
    private BTConnection connection;

    private Bean bean;
    private List<Bean> beans = new ArrayList<>();

    private LocationManager locationManager;
    private LocationListener locationListener;

    float curSpeed, maxSpeed;

    private TextView textSpeed ;
    private TextView textPlace;

    private TextView textLeft;
    private TextView textRight;
    private ImageView viewLeft;
    private ImageView viewRight;

    private long addressSetTime ;
    private Context context;


    private ImageView imgBean;
    private ImageView imgEdison;


    private SharedPreferences prefs;
    private BluetoothDevice btDevice;
    private boolean useLowSpeedBreak ;

    private Handler displayHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what==-1){ //exit
                displayHandler.removeMessages(0);
                displayHandler.removeMessages(1);
                return;
            }
            updateBtStatus();
//            Log.d(TAG, "handle : " + connection + " " + curSpeed);
            if(useLowSpeedBreak && connection!=null && curSpeed<10){
                connection.write("S");
            }

            sendEmptyMessageDelayed(1, 1000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
//        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_riding);
        context = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        btService = new BTService(getApplicationContext());

        textSpeed = (TextView)findViewById(R.id.textSpeed);
        textPlace = (TextView)findViewById(R.id.textPlace);

        imgBean = (ImageView)findViewById(R.id.imgBean);
        imgEdison = (ImageView)findViewById(R.id.imgEdison);

        textLeft = (TextView) findViewById(R.id.textLeft);
        textRight = (TextView) findViewById(R.id.textRight);
        viewLeft = (ImageView)findViewById(R.id.viewLeft);
        viewRight = (ImageView)findViewById(R.id.viewRight);
        useLowSpeedBreak = prefs.getBoolean("useLowSpeedBreak", true);


        curSpeed = maxSpeed = 0;
//        checkBTConnect();
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if(location!=null){

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
//                    txtGpsStat.setText("gps ok");
                    curSpeed = location.getSpeed();
                    // location.getSpeed() returns m/s * 3.6 = km/h
                    curSpeed = curSpeed * 3.6f;
                    int intSpeed = (int) curSpeed;

                    if(curSpeed > maxSpeed)
                        maxSpeed = curSpeed;

//                    String speedText = String.format(
//                            "speed : %.2f km/h \nlat : %.4f lng : %.4f",
//                            curSpeed, lat, lng);
//                    String speedText = String.format("%.2f", curSpeed);
//
                    textSpeed.setText(String.valueOf(intSpeed));

                    long curTime = System.currentTimeMillis();
                    if(addressSetTime + TimeUnit.SECONDS.toMillis(60)<curTime){
                        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                        Address address;
                        String result = null;


                        List<Address> list = null;
                        try {
                            list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            address = list.get(0);
                            result = address.getAddressLine(0);// + ", " + address.getLocality();
                            result = result.substring(0, result.lastIndexOf(' '));
                            result = result.replaceAll("대한민국|한국|특별시","");

                            addressSetTime = System.currentTimeMillis();
                            textPlace.setText(result);

                        } catch (IOException e) {
                            e.printStackTrace();;
                        }

                    }

                }
            }
            public void onStatusChanged(String s, int i, Bundle bundle) {}
            public void onProviderEnabled(String s) {}
            public void onProviderDisabled(String s) {}
        };

    }



    @Override
    protected void onResume() {
        super.onResume();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        useLowSpeedBreak = prefs.getBoolean("useLowSpeedBreak", true);
        updateBtStatus();


        String lastBtAddress = prefs.getString("btDeviceAddress","");

        if(lastBtAddress!=null && lastBtAddress.length()>0){
            btDevice = btAdapter.getRemoteDevice(lastBtAddress);
            btService.connect(btDevice);
        }else{

            AlertDialog.Builder builder =new AlertDialog.Builder(this);
            builder.setMessage( "please check edison bluetooth connection")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onClickSettings(null);
                            }
                        }
                    )
                    .create().show();
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, locationListener);


        beans.clear();
        BeanDiscoveryListener listener = new BeanDiscoveryListener() {
            public void onBeanDiscovered(Bean bean) {

                Log.d(TAG, "onBeanDiscovered " + bean.getDevice().getName());
//
                beans.add(bean);
//                updateBtStatus();
                connectBean(beans.get(0));
                BeanManager.getInstance().cancelDiscovery();
            }

            public void onDiscoveryComplete() {
                BeanManager.getInstance().cancelDiscovery();
                Log.d(TAG, "onDiscoveryComplete");
                if (beans.size() > 0)
                    connectBean(beans.get(0));
            }
        };


        BeanManager.getInstance().startDiscovery(listener);
        Log.d(TAG, "startDiscovery");
        updateBtStatus();

        displayHandler.sendEmptyMessage(0);
    }

    private void updateBtStatus() {
        int btStatus =0;
        int beanStatus = 0;

        if(connection == null && HelmetApp.getConnection()!=null){
            connection = HelmetApp.getConnection();
            connection.setExceptionHandler(this);
        }


        if(btDevice==null){
            imgEdison.setImageResource(R.drawable.edison_disconnected);
        }else if(btDevice!=null && connection==null){
            btStatus = 1;
            imgEdison.setImageResource(R.drawable.edison_connecting);
        }else if(connection!=null){
            btStatus =2 ;
            imgEdison.setImageResource(R.drawable.edison_connected);
        }


//        Log.d(TAG, "bean : " + beans.size() + " , " + bean);

        if(beans.size()==0 ){//bean==null){
            imgBean.setImageResource(R.drawable.bean_disconnected);
//            Log.d(TAG, "bean disconnected");
        }else if(beans.size()>0 && bean==null) // (bean ==null bean!=null && !bean.isConnected()))
        {
            beanStatus = 1;
            imgBean.setImageResource(R.drawable.bean_connecting);
        }else if(bean!=null){
            beanStatus = 2;
            imgBean.setImageResource(R.drawable.bean_connected);
        }

        if(!(btStatus==2 && beanStatus==2))
            Log.d(TAG, "bean : " + beanStatus + " / " + "bt : " + btStatus);


    }


    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
        displayHandler.sendEmptyMessage(-1);

        if(bean!=null){
            if(bean.isConnected())
                bean.disconnect();
        }

    }

    public void onClickLeft(View v){
        ViewUtil.onClick(v);
        String send = "A";

        if(connection!=null)
            connection.write(send);
        displayDirection(send);

    }

    public void onClickRight(View v){
        ViewUtil.onClick(v);
        String send = "D";
        if(connection!=null)
            connection.write(send);
        displayDirection(send);

    }


    private void connectBean(final Bean bean){
//        bean.getDevice().
//        bean.
        this.bean = bean;
        bean.connect(context, new BeanListener() {
            public void onConnected() {
                Log.d(TAG, "onConnected");
                updateBtStatus();
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

        });
    }

    private void displayDirection(String value) {

        if("A".equals(value)){ //left

            viewLeft.setImageResource(R.drawable.btn_direction_blinking);

            viewLeft.postDelayed(new Runnable() {
                public void run() {
                    viewLeft.setImageResource(R.drawable.btn_direction);
                }
            }, 2000L);
        }

        if("D".equals(value)){ //right
            viewRight.setImageResource(R.drawable.btn_direction_blinking);

            viewRight.postDelayed(new Runnable() {
                public void run() {
                    viewRight.setImageResource(R.drawable.btn_direction);
                }
            }, 2000L);
        }

    }


    @Override
    public void onBackPressed() {
        // confirm exit
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setMessage("종료하시겠습니까?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        shutdown();
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();;
    }

    private void shutdown(){
        // clean up bluetooth
        if(bean!=null && bean.isConnected()){
            bean.disconnect();;
        }

        if(connection!=null)
            connection.cancel();

    }


    public void onException(Exception e){
        if(e instanceof IOException){
            Log.d(TAG, "onException : " + e);
            connection.cancel();
            HelmetApp.setConnection(null);
            connection = null;
            btService.connect(btDevice);
        }
    }


    public void onClickSettings(View v){
        ViewUtil.onClick(v);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
