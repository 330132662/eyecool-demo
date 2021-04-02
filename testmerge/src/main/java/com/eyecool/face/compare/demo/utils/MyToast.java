package com.eyecool.face.compare.demo.utils;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.widget.Toast;

public class MyToast {
    private Context mContext;
    private Handler mHandler;
    private Toast myToast;
    private Runnable toastRunnable = new Runnable() {

        @Override
        public void run() {
            myToast.show();
        }
    };

    public MyToast(Context context) {
        this.mContext = context;
        this.mHandler = new Handler(mContext.getMainLooper());
        this.myToast = Toast.makeText(mContext, "", Toast.LENGTH_LONG);
    }

    public void showToast(String string) {
        if (string == null)
            string = "Toast is null";
        if (myToast == null) {
            this.myToast = Toast.makeText(mContext, string, Toast.LENGTH_LONG);
        }
        this.myToast.setText(string);
        mHandler.post(toastRunnable);
    }

    public void showToast(@StringRes int resId) {
        if (myToast == null) {
            this.myToast = Toast.makeText(mContext, resId, Toast.LENGTH_LONG);
        }
        this.myToast.setText(resId);
        mHandler.post(toastRunnable);
    }

    public void cancel() {
        mHandler.removeCallbacks(toastRunnable);
        myToast.cancel();
    }

}
