package flit.com.helmet;

import android.view.View;

/**
 * Created by kfmes on 15. 7. 21..
 */
public class ViewUtil {

    public static void onClick(final View v) {
        if(v==null) return;
        v.setClickable(false);
        v.postDelayed(new Runnable() {
            public void run() {
                v.setClickable(true);
            }
        }, 300L);

    }
}
