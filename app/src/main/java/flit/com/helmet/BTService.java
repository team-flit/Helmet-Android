package flit.com.helmet;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BTService extends Service {

    private BluetoothAdapter adapter;
    private Context context;

    public BTService(){

    }


    public BTService(Context context){
        super();
        this.context = context;
        adapter = BluetoothAdapter.getDefaultAdapter();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void connect(String address){
        BluetoothDevice device = adapter.getRemoteDevice(address);
        connect(device);
    }

    public void connect(BluetoothDevice device){
        ConnectThread t= new ConnectThread(device);
        t.start();
    }
    private void manageConnectedSocket(BluetoothDevice device, BluetoothSocket socket)
    {
        Log.i("BTService.java | manageConnectedSocket", "|==" + socket.getRemoteDevice().getName() + "|" + socket.getRemoteDevice().getAddress());
//        PreferenceUtil.putLastRequestDeviceAddress(socket.getRemoteDevice().getAddress());
        adapter.cancelDiscovery();
//        ConnectedThread thread = new ConnectedThread(socket);
//        thread.start();
        HelmetApp app = (HelmetApp) getApplication();
        BTConnection connection = new BTConnection(device, socket);
        app.setConnection(connection);
        connection.startThread();
//        Toast.makeText(context, "connected : " + socket.getRemoteDevice().getName(), Toast.LENGTH_LONG).show();
    }

    private class ConnectThread extends Thread{
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device){
            BluetoothSocket tmpSock = null ;
            this.device = device;

            try{
                UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                // MY_UUID is the app's UUID string, also used by the server code
                tmpSock = device.createRfcommSocketToServiceRecord(MY_UUID);
            }catch (IOException e){
                e.printStackTrace();;
            }
            this.socket = tmpSock;

        }

        public void run()
        {
            // Cancel discovery because it will slow down the connection
            adapter.cancelDiscovery();
            try
            {
                // Connect the device through the socket. This will block until it succeeds or throws an exception
                socket.connect();
            }
            catch (Exception e1)
            {
                Log.e("BTService.java | run", "|==" + "connect fail" + "|");

                e1.printStackTrace();
                // Unable to connect; close the socket and get out
                try
                {
                    if (socket.isConnected())
                        socket.close();
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }
                return;
            }
            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(device,socket);
        }


        /** Will cancel an in-progress connection, and close the socket */
        public void cancel()
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }


    }
//
//    protected class ConnectedThread extends Thread
//    {
//        private final BluetoothSocket socket;
//        private final InputStream in;
//        private final OutputStream out;
//
//        public ConnectedThread(BluetoothSocket socket){
//            this.socket = socket;
//            InputStream tmpin = null;
//            OutputStream tmpout = null;
//            try {
//                tmpin = socket.getInputStream();
//                tmpout = socket.getOutputStream();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            this.in = tmpin;
//            this.out = tmpout;
//        }
//        public void run()
//        {
//            byte[] buffer = new byte[1024]; // buffer store for the stream
//            int bytes; // bytes returned from read()
//            // Keep listening to the InputStream until an exception occurs
//
//            while (true)
//            {
//                try
//                {
//                    // Read from the InputStream
//                    bytes = in.read(buffer);
//                    // Send the obtained bytes to the UI Activity
////               mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
//                    Log.i("BTService.java | run", "|==" + bytes2String(buffer, bytes) + "|");
//
//                    Intent intent = new Intent("com.flit.helmet.receive");
//                    intent.putExtra("signal", bytes2String(buffer, bytes));
//                    context.sendBroadcast(intent);
//                }
//                catch (Exception e)
//                {
//                    e.printStackTrace();
//                    break;
//                }
//            }
//        }
//
//
//        private String bytes2String(byte[] b, int count)
//        {
//            ArrayList<String> result = new ArrayList<String>();
//            for (int i = 0; i < count; i++)
//            {
//                String myInt = Integer.toHexString((int) (b[i] & 0xFF));
////                result.add("0x" + myInt);
//                result.add( myInt);
//            }
//            return TextUtils.join(" ", result);
//        }
//
//
//      /* Call this from the main Activity to send data to the remote device */
//      public void write(byte[] bytes)
//      {
//         try
//         {
//            out.write(bytes);
//         }
//         catch (IOException e)
//         {
//         }
//      }
//
//        /* Call this from the main Activity to shutdown the connection */
//        public void cancel()
//        {
//            try
//            {
//                socket.close();
//            }
//            catch (IOException e)
//            {
//            }
//        }
//
//    }

}
