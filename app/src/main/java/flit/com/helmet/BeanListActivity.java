package flit.com.helmet;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.littlerobots.bean.Bean;
import nl.littlerobots.bean.BeanDiscoveryListener;
import nl.littlerobots.bean.BeanListener;
import nl.littlerobots.bean.BeanManager;
import nl.littlerobots.bean.message.Callback;
import nl.littlerobots.bean.message.RadioConfig;


public class BeanListActivity extends ActionBarActivity {


    private static final String TAG = BeanListActivity.class.getSimpleName();
    // create a listener
    BeanDiscoveryListener listener ;
    private ListView listView;
// Assuming "this" is an activity or service:
    private BaseAdapter adapter ;
    private List<Bean> beans;
    private Context context;
    private TextView textLog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        setContentView(R.layout.activity_beacon_list);
        
        this.listView = (ListView)findViewById(R.id.listView);
        this.textLog = (TextView)findViewById(R.id.textLog);

        beans = new ArrayList<>();

        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                System.out.println("getCount " + beans.size());
                return beans.size();
            }

            @Override
            public Object getItem(int position) {
                return beans.get(position);
            }
            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view ;
                if(convertView==null){
                    view = getLayoutInflater().inflate(R.layout.listitem_bean, null);
                }else
                    view = convertView;
                TextView textview = (TextView) view.findViewById(R.id.textView);

                Bean bean = (Bean) adapter.getItem(position);
                BluetoothDevice device = bean.getDevice();
                String name = device.getName();
                String addr = device.getAddress();
//                String d = device.get

                textview.setText( name + " : " + addr);// bean.toString());
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final Bean bean = (Bean) adapter.getItem(position);

                BluetoothDevice device = bean.getDevice();
                String address = device.getAddress();
                Toast.makeText(context, bean.toString() + "\n" + address, Toast.LENGTH_SHORT).show();

//                bean.getDevice().connectGatt()
//
                bean.connect(context, new BeanListener(){


                    @Override
                    public void onConnected() {
//                        Toast.makeText(context, "connected" , Toast.LENGTH_SHORT).show();;
                        Log.d(TAG, "onConnected");


                        bean.readRadioConfig(new Callback<RadioConfig>() {
                            @Override
                            public void onResult(RadioConfig radioConfig) {

                                Toast.makeText(context, "onResult " + radioConfig.toString() , Toast.LENGTH_SHORT).show();;
                                Log.d(TAG, "onResult " + radioConfig.toString());


                                StringBuilder sb = new StringBuilder();
                                sb.append("-------------");
                                sb
                                        .append(radioConfig.name()).append("\n")
                                        .append(String.valueOf( radioConfig.beaconUuid())).append("\n")
                                        .append(
                                                Integer.toHexString(radioConfig.beaconUuid()))
                                                        .append("\n")
                                ;
//                                        .append(deviceInfo.softwareVersion() ).append("\n");
                                sb.append("-------------");
                                textLog.setText(sb.toString());

                                bean.disconnect();;
                            }
                        });
                    }

                    @Override
                    public void onConnectionFailed() {
                        Log.d(TAG, "onConnectionFailed");
                    }

                    @Override
                    public void onDisconnected() {
                        Log.d(TAG, "onDisconnected");
                    }

                    @Override
                    public void onSerialMessageReceived(byte[] bytes) {
                    }

                    @Override
                    public void onScratchValueChanged(int i, byte[] bytes) {
                    }
                });


            }
        });

        listener = new BeanDiscoveryListener(){
            @Override
            public void onBeanDiscovered(Bean bean) {
                System.out.println("onBeanDiscovered " + bean);
                beans.add(bean);
                adapter.notifyDataSetChanged();;
            }

            @Override
            public void onDiscoveryComplete() {
                System.out.println("onDiscoveryComplete " );
//                Collection<Bean> beansNew = BeanManager.getInstance().getBeans();
//                beans.clear();
//                beans.addAll(beansNew);
//                adapter.notifyDataSetInvalidated();
            }
        };


    }

    public void onClickScan(View v){
        beans.clear();
        BeanManager.getInstance().startDiscovery(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BeanManager.getInstance().cancelDiscovery();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_beacon_list, menu);
//        return true;
//    }

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

    public void onClickRefresh(View view) {
        Collection<Bean> beansNew = BeanManager.getInstance().getBeans();
        beans.clear();
        System.out.println("new : " + beansNew.size());

        beans.addAll(beansNew);
        adapter.notifyDataSetInvalidated();
    }
}
