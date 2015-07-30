package flit.com.helmet;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Created by kfmes on 15. 7. 21..
 */
public class BTConnection
{
    private final String TAG = BTConnection.class.getSimpleName();
    private final BluetoothSocket socket;
    private final InputStream in;
    private final OutputStream out;
    private final BluetoothDevice device;
    private ExceptionHandler exceptionHandler;
    private Thread readerThread;
    private Thread writerThread;


    private List<String> writeQueue =
            Collections.synchronizedList(new ArrayList<String>());

    public void setExceptionHandler(ExceptionHandler handler){
        this.exceptionHandler = handler;
    }

    interface Logging {
        void appendLog(String log);
    }
    interface ExceptionHandler{
        void onException(Exception e);
    }
    private Logging logging;

    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    public OutputStream getOut() {
        return out;
    }


    public BTConnection(BluetoothDevice device, BluetoothSocket socket){
        this.device = device;
        this.socket = socket;
        InputStream tmpin = null;
        OutputStream tmpout = null;
        try {
            tmpin = socket.getInputStream();
            tmpout = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.in = tmpin;
        this.out = tmpout;
    }

    private void appendLog(String log) {
        if (logging != null)
            logging.appendLog(log);
    }

    public void startThread(){

        if(readerThread !=null || writerThread !=null) throw new IllegalStateException("connection thread already running");
        readerThread = new Thread("BT Reader Thread"){
            public void run()
            {
                byte[] buffer = new byte[1024]; // buffer store for the stream
                int bytes; // bytes returned from read()
                // Keep listening to the InputStream until an exception occurs

                Scanner sc = new Scanner(in);
                while (socket.isConnected())
                {
                    while(sc.hasNext()) {
                        String next = sc.next();
                        appendLog(next);
                    }
                }
            }
        };
        readerThread.start();

        writerThread = new Thread("BT Writer Thread"){
            @Override
            public void run() {
                while(socket.isConnected()){

                    if(writeQueue.size()>0){
                        String[] sendArr = writeQueue.toArray(new String[0]);
                        writeQueue.clear();

                        try {
                            for (String s : sendArr) {
                                out.write(s.getBytes());
                                Log.d(TAG, "SEND : " + s);
                            }
                            out.flush();
                        }catch (IOException e){
                            e.printStackTrace();
                            if(exceptionHandler!=null)
                                exceptionHandler.onException(e);
                        }

                    }else{

                        try {
                            sleep(100L);
                        } catch (InterruptedException e) {
                        }

                    }
                }
            }
        };
        writerThread.start();


    }



    public void write(String msg) {
        writeQueue.add(msg);

    }

    /* Call this from the main Activity to send data to the remote device */
    private void write(byte[] bytes)
    {
        try
        {
            out.write(bytes);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /* Call this from the main Activity to shutdown the connection */
    public void cancel()
    {
        try
        {
            readerThread.interrupt();
            writerThread.interrupt();
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
