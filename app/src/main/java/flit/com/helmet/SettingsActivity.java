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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class SettingsActivity extends AppCompatActivity {

    private BTService btService;
    private BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
//        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);
        btService = new BTService(getApplicationContext());
        btAdapter = BluetoothAdapter.getDefaultAdapter();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
