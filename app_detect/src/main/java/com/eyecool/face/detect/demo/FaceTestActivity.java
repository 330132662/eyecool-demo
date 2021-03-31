package com.eyecool.face.detect.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecool.camera.CameraInterface;
import com.eyecool.camera.config.CameraConfig;
import com.eyecool.camera.view.CameraTextureView;
import com.eyecool.face.api.FaceApi;
import com.eyecool.face.detect.demo.config.DetectConfig;
import com.eyecool.face.detect.demo.view.FrameFaceView;
import com.eyecool.face.model.Face;
import com.eyecool.face.model.FaceInfo;
import com.eyecool.utils.ExecutorServiceUtll;
import com.eyecool.utils.Logs;
import com.eyecool.utils.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 人脸检测
 * created by xiaozhi 2018/4/20
 */
public class FaceTestActivity extends AppCompatActivity implements CameraTextureView.CameraCallback {

    private static final String TAG = FaceTestActivity.class.getSimpleName();

    private RelativeLayout mParentLayout;
    private FrameLayout mFrameLayout;
    private CameraTextureView mCameraView;
    private CameraInterface mCameraInterface;
    private FrameFaceView mFrameFaceView;
    private TextView mHintTv;

    private DetectTask mDetectTask;
    byte[] faceRgb24 = null;
    private boolean mIsPostFaceData = false;
    private int mPreviewWidth = DetectConfig.sPreviewWidth;
    private int mPreviewHeight = DetectConfig.sPreviewHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_test);

        mParentLayout = findViewById(R.id.parentLayout);
        mFrameLayout = findViewById(R.id.frameLayout);
        mCameraView = findViewById(R.id.cameraView);
        mFrameFaceView = findViewById(R.id.frameView);
        mHintTv = findViewById(R.id.hintTv);
        findViewById(R.id.settingIv).setOnClickListener(v -> startSetting());

        mCameraView.setCameraCallback(this);
        mCameraInterface = mCameraView.getCameraInterface();

        Logs.i(TAG, "算法版本：" + FaceApi.getInstance().getVersion());
        mCameraInterface.setPreviewSize(mPreviewWidth, mPreviewHeight);

        /**
         * 获取摄像头参数
         */
        DetectConfig.sCameraId = SharedPreferenceUtil.getInstance().getInt(DetectConfig.SP_CAMERA_ID);
        DetectConfig.sPreviewOrientation = SharedPreferenceUtil.getInstance().getInt(DetectConfig.SP_PREVIEW_ORIENTATION);
        DetectConfig.sRotate = SharedPreferenceUtil.getInstance().getInt(DetectConfig.SP_ROTATE);
        int scale = SharedPreferenceUtil.getInstance().getInt(DetectConfig.SP_PREVIEW_SCALE);
        switch (scale) {
            case 0:
                DetectConfig.sPreviewScale = DetectConfig.PreviewScale.NONE;
                break;
            case 1:
                DetectConfig.sPreviewScale = DetectConfig.PreviewScale.SCALE_4_3;
                break;
            case 2:
                DetectConfig.sPreviewScale = DetectConfig.PreviewScale.SCALE_3_4;
                break;
        }
        DetectConfig.isPreviewMirror = SharedPreferenceUtil.getInstance().getBoolean(DetectConfig.SP_PREVIEW_MIRROR);
        DetectConfig.isDataMirror = SharedPreferenceUtil.getInstance().getBoolean(DetectConfig.SP_DATA_MIRROR);

        layoutView();
    }

    private void startSetting() {
        startActivity(new Intent(FaceTestActivity.this, SettingActivity.class));
        finish();
    }

    private void layoutView() {
        // 设置Camera预览旋转等参数，注：要在开启摄像头之前设置
        // 设置打开前后置摄像头（0：后置 1：前置）
        mCameraInterface.setCameraId(DetectConfig.sCameraId);
        mCameraInterface.setDisplayOrientation(DetectConfig.sPreviewOrientation);
        mFrameFaceView.setMirror(DetectConfig.isDataMirror);
        mCameraView.setMirror(DetectConfig.isPreviewMirror);

        mFrameLayout.post(() -> {
            Logs.i(TAG, "layoutView...");

            int width = mParentLayout.getMeasuredWidth();
            int height = mParentLayout.getMeasuredHeight();

            if (width > height) {
                width = height;
            } else {
                height = width;
            }

            int realWidth = width;
            int realHeight = height;
            switch (DetectConfig.sPreviewOrientation) {
                case CameraConfig.ROTATE_0:
                case CameraConfig.ROTATE_180:
                    realWidth = width;
                    realHeight = width * 3 / 4;
                    mFrameFaceView.setpreviewSize(mPreviewWidth, mPreviewHeight);
                    break;
                case CameraConfig.ROTATE_90:
                case CameraConfig.ROTATE_270:
                    realHeight = width;
                    realWidth = width * 3 / 4;
                    mFrameFaceView.setpreviewSize(mPreviewHeight, mPreviewWidth);
                    break;
                default:
                    break;
            }

            // 强制预览比例
            if (DetectConfig.sPreviewScale == DetectConfig.PreviewScale.SCALE_4_3) {
                if (realWidth < realHeight) {
                    int tempSize = realHeight;
                    realHeight = realWidth;
                    realWidth = tempSize;

                    mFrameFaceView.setpreviewSize(mPreviewWidth, mPreviewHeight);
                }
            } else if (DetectConfig.sPreviewScale == DetectConfig.PreviewScale.SCALE_3_4) {
                if (realWidth > realHeight) {
                    int tempSize = realHeight;
                    realHeight = realWidth;
                    realWidth = tempSize;

                    mFrameFaceView.setpreviewSize(mPreviewHeight, mPreviewWidth);
                }
            }

            ViewGroup.LayoutParams lp = mFrameLayout.getLayoutParams();
            lp.width = realWidth;
            lp.height = realHeight;
            mFrameLayout.setLayoutParams(lp);

            ViewGroup.LayoutParams lp1 = mCameraView.getLayoutParams();
            lp1.width = realWidth;
            lp1.height = realHeight;
            mCameraView.setLayoutParams(lp1);

            ViewGroup.LayoutParams lp2 = mFrameFaceView.getLayoutParams();
            lp2.width = realWidth;
            lp2.height = realHeight;
            mFrameFaceView.setLayoutParams(lp2);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logs.i(TAG, "onResume...");
        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logs.i(TAG, "onPause...");
        mCameraView.onPause();
        stopDetect();
    }

    @Override
    public void cameraOpen() {
        showToast(R.string.text_camera_open);
        faceRgb24 = new byte[mCameraInterface.getPreviewWidth() * mCameraInterface.getPreviewHeight() * 3];
        startDetect();
    }

    @Override
    public void cameraOpenError(int error) {

    }

    @Override
    public void cameraPreviewError() {

    }

    @Override
    public void cameraClose() {
        showToast(R.string.text_camera_close);
    }

    private void showToast(@StringRes int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    /**
     * 启动检测人脸线程
     */
    private void startDetect() {
        if (!FaceApi.getInstance().isInitOk()) {
            return;
        }
        if (mDetectTask == null) {
            mDetectTask = new DetectTask();
        }
        ExecutorServiceUtll.execute(mDetectTask);
    }

    /**
     * 停止检测人脸线程
     */
    private void stopDetect() {
        if (mDetectTask != null) {
            mDetectTask.stop();
            mDetectTask = null;
        }
    }

    private class DetectTask implements Runnable {

        private boolean isStop = false;

        @Override
        public void run() {
            while (true) {
                if (isStop) {
                    break;
                }
                byte[] yuvBytes = mCameraInterface.getCameraBytes();
                if (yuvBytes == null)
                    continue;

                // 检测人脸
                FaceInfo faceInfo = FaceApi.getInstance().detectByNV21(yuvBytes, mCameraInterface.getPreviewWidth(), mCameraInterface.getPreviewHeight(), DetectConfig.sRotate);

                if (faceInfo.isHasFaces()) {
                    // 有人脸画框
                    List<Face> faceList = faceInfo.getFaceList();
                    List<int[]> faceRects = new ArrayList<>();
                    for (Face face : faceList) {
                        faceRects.add(face.getFaceRect());
                    }
                    Face first = faceList.get(0);
                    mFrameFaceView.setLocFaces(faceRects);
                    Logs.i(TAG, "FaceId:" + first.getFaceId() + " Hack:" + first.getHackScore());

                    StringBuffer info = new StringBuffer();
//                    info.append("faceId: " + first.getFaceId() + "\n");
                    info.append("Yaw: " + first.getYawDegree() + "\n");
                    info.append("Pitch: " + first.getPitchDegree() + "\n");
                    info.append("Roll: " + first.getRollDegree());

                    mHintTv.post(() -> mHintTv.setText(info.toString()));
                    postFaceData(faceInfo);
                } else {
                    mHintTv.post(() -> mHintTv.setText(""));
                    mFrameFaceView.setLocFaces(null);
                }
            }
            Logs.e(TAG, "检测线程停止...");
        }

        public void stop() {
            isStop = true;
        }
    }

    /**
     * 发送人脸数据到服务器
     * (以下均为示例，可根据业务自行处理)
     */
    private void postFaceData(final FaceInfo faceInfo) {
        if (mIsPostFaceData)
            return;
        mIsPostFaceData = true;
        // 发送人脸数据
        new Thread(() -> {
            byte[] rgb24 = faceInfo.getRgb24();

//      // 获取jpg数据可发送服务器做比对
//      byte[] jpg = FaceApi.getInstance().rgb24ToJpg(rgb24, faceInfo.getWidth(), faceInfo.getHeight(), 0, 0);
//      // 对jpg做Base64
//      String jpgBase64 = Base64.encodeToString(jpg, Base64.NO_WRAP);
//      // 保存jpg到本地
//      String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "faceJpg.jpg";
//      FileUtils.writeFile(filePath, jpg);

            mIsPostFaceData = false;
        }).start();
    }
}
