package com.eyecool.face.detect.demo.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.eyecool.utils.Logs;

import java.util.List;

/**
 * Created by wangzhi on 2016/5/23.
 */
public class FrameFaceView extends View {

    private static final String TAG = FrameFaceView.class.getSimpleName();

    private int previewWidth = 640;
    private int previewHeight = 480;

    private Paint mPaint;

    private int[] mLocFace;

    private List<int[]> mLocFaces;

    private int mWidth = 0;

    private int mHeight = 0;

    private float mDx;

    private float mDy;

    private int mBitmapWidth;

    private int mBitmapHeight;

    private int mCameraId;

    private boolean isMirror = false;

    private Context mContext;

    public FrameFaceView(Context context) {
        super(context);
        init(context);
    }

    public FrameFaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FrameFaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public void setCameraId(int cameraId) {
        mCameraId = cameraId;
    }

    public void setpreviewSize(int previewWidth, int previewHeight) {
        this.previewWidth = previewWidth;
        this.previewHeight = previewHeight;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void setMirror(boolean mirror) {
        isMirror = mirror;
    }

    private void init(Context context) {
        mContext = context;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#f0d701"));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Logs.i(TAG, "获取显示区域参数");
        int desiredWidth = previewWidth;
        int desiredHeight = previewHeight;

        float radio = (float) desiredWidth / (float) desiredHeight;

        Log.i(TAG, "获取显示区域参数 radio:" + radio);

        /**
         * 每个MeasureSpec均包含两种数据，尺寸和设定类型，需要通过 MeasureSpec.getMode和getSize进行提取
         */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 参考值竖屏 800 1214
        // 参考值横屏 1280 734
        int layout_width = 0;
        int layout_height = 0;

        if (widthMode == MeasureSpec.EXACTLY) {
            // 精确值情况
            layout_width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // 范围值情况，哪个小取哪个？
            layout_width = Math.min(desiredWidth, widthSize);
        } else {
            // 没设定就是默认的了，呵呵
            layout_width = desiredWidth;
        }
        // 高度设定同上
        if (heightMode == MeasureSpec.EXACTLY) {
            layout_height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            layout_height = Math.min(desiredHeight, heightSize);
        } else {
            layout_height = desiredHeight;
        }

        Logs.i(TAG, "layout_height:" + layout_height);// 让我们来输出他们

        float layout_radio = (float) layout_width / (float) layout_height;

        if (layout_radio > radio) {
            layout_height = (int) (layout_width / radio);
        } else {
            layout_width = (int) (layout_height * radio);
        }

        mWidth = layout_width;
        mHeight = layout_height;
        setMeasuredDimension(layout_width, layout_height);
        Logs.i(TAG, "CSV设定宽度:" + widthSize + "  设定高度:" + heightSize);// 让我们来输出他们
        Logs.i(TAG, "CSV实际宽度:" + layout_width + "  实际高度:" + layout_height);// 让我们来输出他们
        Logs.i(TAG, "显示比例：" + ((float) layout_width / (float) layout_height));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLocFaces == null)
            return;

        for (int[] locFace : mLocFaces) {
            float right;
            float left;

            if (!isMirror) {
                left = getX(locFace[0]);
                right = left + (float) locFace[2] * mWidth / previewWidth;
            } else {
                right = getX(locFace[0]);
                left = right - (float) locFace[2] * mWidth / previewWidth;
            }

            float top = getY(locFace[1]);
            float bottom = top + (float) locFace[3] * mHeight / previewHeight;

            canvas.drawRect(new Rect((int) left, (int) top, (int) right, (int) bottom), mPaint);
            Logs.i(TAG, "draw... left:" + left + " right:" + right + " top:" + top + "bottom:" + bottom);
        }


    }

    private float getY(int y) {
        return (float) y * mHeight / previewHeight;
    }

    private float getX(int x) {
        if (!isMirror) {
            return (float) x * mWidth / previewWidth;
        } else {
            return mWidth - (float) x * mWidth / previewWidth;
        }
    }

    public int[] getLocFace() {
        return mLocFace;
    }

    public void setLocFace(int[] locFace) {
        mLocFace = locFace;
        postInvalidate();
    }

    public void setLocFaces(List<int[]> locFaces) {
        mLocFaces = locFaces;
        postInvalidate();
    }
}
