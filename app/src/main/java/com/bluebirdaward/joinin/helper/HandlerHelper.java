package com.bluebirdaward.joinin.helper;

import android.os.Handler;

/**
 * Created by md760 on 12/5/15.
 */
public class HandlerHelper {
    public static void run(final IHandlerDo iHandlerDo, int timeInMilis) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                iHandlerDo.doThis();
            }
        }, timeInMilis);
    }

    public interface IHandlerDo{
        void doThis();
    }
}
