package flit.com.helmet;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;


public class SettingsActivity extends AppCompatActivity {

    private BTService btService;
    private BluetoothAdapter btAdapter;
    private CheckBox useBreak;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
//        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);
        btService = new BTService(getApplicationContext());
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        useBreak = (CheckBox)findViewById(R.id.useBreak);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        useBreak.setChecked(prefs.getBoolean("useLowSpeedBreak", true));
        useBreak.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean("useLowSpeedBreak", b).commit();
            }
        });

    }

    public void onClickRiding(View view){
        startActivity(new Intent(this, RidingActivity.class));
    }
    public void onClickSetting(View view){
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(intent, 10);
    }

    public void onClickTest(View view){
        startActivity(new Intent(this, SPPTestActivity.class));
    }

    public void onClickTestBean(View v){
        startActivity(new Intent(this, BeanListActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){
            return;
        }

        if(requestCode==10){
            String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            Log.i(" | onActivityResult", "|==" + address + "|");
            if (TextUtils.isEmpty(address))
                return;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString("btDeviceAddress", address).commit();

            BluetoothDevice device = btAdapter.getRemoteDevice(address);
//            btService.connect(device);

        }
    }
}
