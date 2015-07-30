package flit.com.helmet;

import android.app.Application;

/**
 * Created by kfmes on 15. 7. 21..
 */
public class HelmetApp extends Application {


    static BTConnection connection;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static BTConnection getConnection() {
        return connection;
    }

    public static void setConnection(BTConnection connection) {
        HelmetApp.connection = connection;
    }
}
