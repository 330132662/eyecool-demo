package com.eyecool.face.duallive.demo.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecool.camera.CameraInterface;
import com.eyecool.camera.config.CameraConfig;
import com.eyecool.camera.view.CameraTextureView;
import com.eyecool.face.api.FaceApi;
import com.eyecool.face.api.FaceLiveApi;
import com.eyecool.face.duallive.demo.config.DualFaceConfig;
import com.eyecool.face.duallive.demo.view.FrameFaceView;
import com.eyecool.face.model.Face;
import com.eyecool.face.model.FaceInfo;
import com.eyecool.face.model.FaceLiveInfo;
import com.eyecool.utils.FileUtils;
import com.eyecool.utils.Logs;
import com.eyecool.utils.SharedPreferenceUtil;
import com.juhuiwangluo.testmerge.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DualSysCameraFragment extends Fragment {


    public static final int ERROR_CODE_CANCEL = -1;
    public static final int ERROR_CODE_BUSY = -2;
    public static final int ERROR_CODE_TIMEOUT = -3;
    public static final int ERROR_CODE_PARAM = -4;

    public static String EYECOOL_FACE_PATH = Environment.getExternalStorageDirectory() + "/EyeCool_Face";
    public static final String EYECOOL_FACE_DEBUG_DIR = Environment.getExternalStorageDirectory() + "/EyeCool_Face_Debug";
    public static String EYECOOL_FACE_PATH_DEBUG = EYECOOL_FACE_DEBUG_DIR;
    /**
     * 晃动方差缓存数量
     */
    static final int QUEUE_CACHE_SIZE = 10;
    /**
     * 晃动方差阈值
     */
    static final int VARIANCE_VALUE = 7;
    private static final boolean DEBUG = true;  // FIXME set false when production
    private static final String TAG = DualSysCameraFragment.class.getSimpleName();
    private static final int CAMERA_LIMIT_RETRY_TIME = 6;
    byte[] mTempJpgBytes = null;
    String mEditParams;
    ExecutorService mFaceExecutorService = null;
    int index = 0;
    int mPreviewWidth = 640;
    int mPreviewHeight = 480;
    Queue<Integer> xQueue = new LinkedList<>();
    Queue<Integer> yQueue = new LinkedList<>();
    private int mCameraReTryTime = 0;
    private FaceLiveApi mFaceLiveApi;
    private DualFaceConfig mDualFaceConfig;
    private long mDetectTime;
    private Handler mHandler;
    private TextView mHintTv;
    private TextView mInfoTv;
    private CameraTextureView mCameraTextureViewNir;
    private CameraTextureView mCameraTextureViewVis;
    private FrameFaceView mFrameFaceView;
    private FrameLayout mCameraLayout;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private DetectCallback mDetectCallback;

    public interface DetectCallback {

        void onSuccess(List<byte[]> images);

        void onError(int errCode, String msg);
    }

    CameraTextureView.CameraCallback leftCallback = new CameraTextureView.CameraCallback() {
        @Override
        public void cameraOpen() {
            Log.i(TAG, "left camera open...");
        }

        @Override
        public void cameraOpenError(int error) {
            Log.e(TAG, "left camera open error:" + error);
            retryOpenCamera(mCameraTextureViewNir);
        }

        @Override
        public void cameraPreviewError() {
            Log.e(TAG, "left camera preview error...");
            retryOpenCamera(mCameraTextureViewNir);
        }

        @Override
        public void cameraClose() {
            Log.i(TAG, "left camera close...");
        }
    };
    CameraTextureView.CameraCallback rightCallback = new CameraTextureView.CameraCallback() {
        @Override
        public void cameraOpen() {
            Log.i(TAG, "right camera open...");
        }

        @Override
        public void cameraOpenError(int error) {
            Log.e(TAG, "right camera open error:" + error);
            retryOpenCamera(mCameraTextureViewVis);
        }

        @Override
        public void cameraPreviewError() {
            Log.e(TAG, "right camera preview error...");
            retryOpenCamera(mCameraTextureViewVis);
        }

        @Override
        public void cameraClose() {
            Log.i(TAG, "right camera close...");
        }
    };
    private DetectFaceTask mDetectFaceTask;
    private byte[] curYuv1 = null;
    private byte[] curYuv2 = null;
    private boolean isLiveDetecting = false;

    /**
     * 只遍历数组一次求方差，利用公式DX^2=EX^2-(EX)^2
     *
     * @param a
     * @return
     */
    private static double computeVariance(Queue<Integer> a) {
        double variance = 0;//方差
        double sum = 0, sum2 = 0;
        int len = a.size();
        for (Integer value : a) {
            sum += value;
            sum2 += value * value;
        }
        variance = sum2 / len - (sum / len) * (sum / len);
        return variance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EYECOOL_FACE_PATH = getActivity().getExternalFilesDir("EcFace") + File.separator;

        EYECOOL_FACE_PATH_DEBUG = EYECOOL_FACE_DEBUG_DIR + "/" + getNowDate();

        mFaceExecutorService = Executors.newFixedThreadPool(4);
        mHandler = new Handler(getActivity().getMainLooper());

        mTempJpgBytes = new byte[mPreviewWidth * mPreviewHeight];
        mFaceLiveApi = FaceLiveApi.getInstance();

        // vis参数设置
        DualFaceConfig.sVisCameraId = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_VIS_CAMERA_ID);
        DualFaceConfig.sVisPreviewOrientation = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_VIS_PREVIEW_ORIENTATION);
        DualFaceConfig.sVisRotate = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_VIS_ROTATE);
        DualFaceConfig.isVisPreviewMirror = SharedPreferenceUtil.getInstance().getBoolean(DualFaceConfig.SP_VIS_PREVIEW_MIRROR);
        DualFaceConfig.isVisDataMirror = SharedPreferenceUtil.getInstance().getBoolean(DualFaceConfig.SP_VIS_DATA_MIRROR);
        // nir参数设置
        DualFaceConfig.sNirPreviewOrientation = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_NIR_PREVIEW_ORIENTATION);
        DualFaceConfig.sNirRotate = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_NIR_ROTATE);
        DualFaceConfig.isNirPreviewMirror = SharedPreferenceUtil.getInstance().getBoolean(DualFaceConfig.SP_NIR_PREVIEW_MIRROR);
        DualFaceConfig.isNirDataMirror = SharedPreferenceUtil.getInstance().getBoolean(DualFaceConfig.SP_NIR_DATA_MIRROR);

        DualFaceConfig.getDualFaceConfig().setDebug(SharedPreferenceUtil.getInstance().getBoolean(DualFaceConfig.SP_DEBUG, DualFaceConfig.getDualFaceConfig().isDebug()));
        DualFaceConfig.getDualFaceConfig().setTimeout(SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_TIMEOUT, DualFaceConfig.getDualFaceConfig().getTimeout()));
        DualFaceConfig.getDualFaceConfig().setThreshold(SharedPreferenceUtil.getInstance().getFloat(DualFaceConfig.SP_LIVE_THRESHOLD, DualFaceConfig.getDualFaceConfig().getThreshold()));
        DualFaceConfig.getDualFaceConfig().setDistanceMin(SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_MIN_FACE, DualFaceConfig.getDualFaceConfig().getDistanceMin()));
        DualFaceConfig.getDualFaceConfig().setDistanceMax(SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_MAX_FACE, DualFaceConfig.getDualFaceConfig().getDistanceMax()));

        DualFaceConfig.getDualFaceConfig().setYawDegree(SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_YAW_LIMIT, DualFaceConfig.getDualFaceConfig().getYawDegree()));
        DualFaceConfig.getDualFaceConfig().setPitchDegree(SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_PITCH_LIMIT, DualFaceConfig.getDualFaceConfig().getPitchDegree()));
        DualFaceConfig.getDualFaceConfig().setRollDegree(SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_ROLL_LIMIT, DualFaceConfig.getDualFaceConfig().getRollDegree()));

        DualFaceConfig.getDualFaceConfig().setNirCount(SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_LIVE_COUNT, DualFaceConfig.getDualFaceConfig().getNirCount()));

        int scale = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_PREVIEW_SCALE);
        switch (scale) {
            case 0:
                DualFaceConfig.sPreviewScale = DualFaceConfig.PreviewScale.NONE;
                break;
            case 1:
                DualFaceConfig.sPreviewScale = DualFaceConfig.PreviewScale.SCALE_4_3;
                break;
            case 2:
                DualFaceConfig.sPreviewScale = DualFaceConfig.PreviewScale.SCALE_3_4;
                break;
        }

        Logs.i(TAG, DualFaceConfig.getDualFaceConfig().toString());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dual_sys_camera, container, false);

        mCameraTextureViewNir = view.findViewById(R.id.cameraTvNir);
        mCameraTextureViewVis = view.findViewById(R.id.cameraTvVis);

        mCameraTextureViewNir.getCameraInterface().setPreviewSize(mPreviewWidth, mPreviewHeight);
        mCameraTextureViewVis.getCameraInterface().setPreviewSize(mPreviewWidth, mPreviewHeight);

        mCameraLayout = view.findViewById(R.id.cameraLayout);
        mFrameFaceView = view.findViewById(R.id.frameFaceView);

        mCameraTextureViewNir.setCameraCallback(leftCallback);
        mCameraTextureViewVis.setCameraCallback(rightCallback);

        mHintTv = view.findViewById(R.id.hintTv);
        mInfoTv = view.findViewById(R.id.infoTv);

        layoutView();

        return view;
    }

    private void layoutView() {
        mCameraTextureViewNir.getCameraInterface().setCameraId(DualFaceConfig.sVisCameraId);
        mCameraTextureViewNir.getCameraInterface().setDisplayOrientation(DualFaceConfig.sNirPreviewOrientation);
        mCameraTextureViewNir.setMirror(DualFaceConfig.isNirPreviewMirror);

        mCameraTextureViewVis.getCameraInterface().setCameraId(DualFaceConfig.sVisCameraId == 0 ? 1 : 0);
        mCameraTextureViewVis.getCameraInterface().setDisplayOrientation(DualFaceConfig.sVisPreviewOrientation);
        mCameraTextureViewVis.setMirror(DualFaceConfig.isVisPreviewMirror);

        mFrameFaceView.setMirror(DualFaceConfig.isVisDataMirror);

        mCameraLayout.post(new Runnable() {
            @Override
            public void run() {
                Logs.i(TAG, "layoutView...");
                int width = mCameraLayout.getMeasuredWidth();
                int height = mCameraLayout.getMeasuredHeight();

                Logs.i("Layout", "width: " + width + " height: " + height);

                int realWidth = Math.max(width, height);
                int realHeight = realWidth;

                Logs.i("Layout", "realWidth: " + realWidth + " realHeight: " + realHeight);

                switch (DualFaceConfig.sVisPreviewOrientation) {
                    case CameraConfig.ROTATE_0:
                    case CameraConfig.ROTATE_180:
                        realHeight = realWidth * 3 / 4;
                        mFrameFaceView.setpreviewSize(mPreviewWidth, mPreviewHeight);
                        break;
                    case CameraConfig.ROTATE_90:
                    case CameraConfig.ROTATE_270:
                        realWidth = realHeight * 3 / 4;
                        mFrameFaceView.setpreviewSize(mPreviewHeight, mPreviewWidth);
                        break;
                    default:
                        break;
                }

                // 强制预览比例
                if (DualFaceConfig.sPreviewScale == DualFaceConfig.PreviewScale.SCALE_4_3) {
                    if (realWidth < realHeight) {
                        int tempSize = realHeight;
                        realHeight = realWidth;
                        realWidth = tempSize;

                        mFrameFaceView.setpreviewSize(mPreviewWidth, mPreviewHeight);
                    }
                } else if (DualFaceConfig.sPreviewScale == DualFaceConfig.PreviewScale.SCALE_3_4) {
                    if (realWidth > realHeight) {
                        int tempSize = realHeight;
                        realHeight = realWidth;
                        realWidth = tempSize;

                        mFrameFaceView.setpreviewSize(mPreviewHeight, mPreviewWidth);
                    }
                }

                Logs.i("Layout", "realWidth: " + realWidth + " realHeight: " + realHeight);

                ViewGroup.LayoutParams cameraLp = mCameraLayout.getLayoutParams();
                cameraLp.width = realWidth;
                cameraLp.height = realHeight;
                mCameraLayout.setLayoutParams(cameraLp);

                ViewGroup.LayoutParams rgbLp = mCameraTextureViewVis.getLayoutParams();
                rgbLp.width = realWidth;
                rgbLp.height = realHeight;
                mCameraTextureViewVis.setLayoutParams(rgbLp);

                ViewGroup.LayoutParams infoLp = mInfoTv.getLayoutParams();
                infoLp.width = realWidth;
                infoLp.height = realHeight;
                mInfoTv.setLayoutParams(infoLp);

                ViewGroup.LayoutParams frameLp = mFrameFaceView.getLayoutParams();
                frameLp.width = realWidth;
                frameLp.height = realHeight;
                mFrameFaceView.setLayoutParams(frameLp);

                ViewGroup.LayoutParams nirLp = mCameraTextureViewNir.getLayoutParams();
                switch (DualFaceConfig.sNirPreviewOrientation) {
                    case CameraConfig.ROTATE_0:
                    case CameraConfig.ROTATE_180:
                        nirLp.width = Math.max(realWidth, realHeight) / 4;
                        nirLp.height = nirLp.width * 3 / 4;
                        break;
                    case CameraConfig.ROTATE_90:
                    case CameraConfig.ROTATE_270:
                        nirLp.height = Math.max(realWidth, realHeight) / 4;
                        nirLp.width = nirLp.height * 3 / 4;
                        break;
                    default:
                        break;
                }
                mCameraTextureViewNir.setLayoutParams(nirLp);

                // 强制预览比例
                if (DualFaceConfig.sPreviewScale == DualFaceConfig.PreviewScale.SCALE_4_3 || DualFaceConfig.sPreviewScale == DualFaceConfig.PreviewScale.SCALE_3_4) {
                    nirLp.width = realWidth / 4;
                    nirLp.height = realHeight / 4;
                    mCameraTextureViewNir.setLayoutParams(nirLp);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startDetectThread();
        mCameraTextureViewNir.onResume();
        mCameraTextureViewVis.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopDetectThread();
        mCameraTextureViewNir.onPause();
        mCameraTextureViewVis.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDetect();
    }

    public void startCamera() {
        mCameraTextureViewNir.onResume();
        mCameraTextureViewVis.onResume();
    }

    public void stopCamera() {
        mCameraTextureViewNir.onPause();
        mCameraTextureViewVis.onPause();
        stopDetect();
    }

    public void startDetect(DualFaceConfig config, DetectCallback detectCallback) {
        if (isLiveDetecting) {
            Toast.makeText(getActivity(), R.string.text_being_tested, Toast.LENGTH_SHORT).show();
            error(ERROR_CODE_BUSY, "正在检测中");
            return;
        }
        if (config == null) {
            error(ERROR_CODE_PARAM, "配置参数不能为空");
            return;
        }
        logFileName = getNowDateTime();
        mDualFaceConfig = config;
        mDetectCallback = detectCallback;
        mHintTv.setText(R.string.text_start_test);
        isLiveDetecting = true;
        index = 0;
        mDetectTime = System.currentTimeMillis();
    }

    public void stopDetect() {
        isLiveDetecting = false;
        mHintTv.setText(R.string.text_stop_test);
    }

    boolean isSwitchCamera = false;

    public void switchCamera() {
        isSwitchCamera = true;
        stopCamera();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CameraInterface cameraInterface1 = mCameraTextureViewNir.getCameraInterface();
                CameraInterface cameraInterface2 = mCameraTextureViewVis.getCameraInterface();

                int tempId = cameraInterface1.getCameraId();
                cameraInterface1.setCameraId(cameraInterface2.getCameraId());
                cameraInterface2.setCameraId(tempId);

                startCamera();

                isSwitchCamera = false;
            }
        }, 1000);
    }

    /**
     * 尝试重新打开Camera
     *
     * @param cameraTextureView
     */
    private void retryOpenCamera(final CameraTextureView cameraTextureView) {
        if (mCameraReTryTime >= CAMERA_LIMIT_RETRY_TIME) {
            Toast.makeText(getContext(), getString(R.string.text_camera_open_error_hint) + "：" + mCameraReTryTime, Toast.LENGTH_SHORT).show();
            return;
        }
        cameraTextureView.onPause();
        cameraTextureView.postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraTextureView.onResume();
            }
        }, 1000);
        mCameraReTryTime++;
    }

    /**
     * 启动检测人脸线程
     */
    private void startDetectThread() {
        if (!FaceApi.getInstance().isInitOk()) {
            return;
        }
        if (mDetectFaceTask == null) {
            mDetectFaceTask = new DetectFaceTask();
            mFaceExecutorService.execute(mDetectFaceTask);
        }
    }

    /**
     * 关闭检测人脸线程
     */
    private void stopDetectThread() {
        if (mDetectFaceTask != null) {
            mDetectFaceTask.stop();
            mDetectFaceTask = null;
        }
    }

    private void runOnUiThread(Runnable runnable, long delayTime) {
        mHandler.postDelayed(runnable, delayTime);
    }

    private byte[] cropFaceRgb24ToJpg(Face face, byte[] rgb24, int width, int height) {
        // 裁剪图片
        int[] cropRect = new int[4];
        System.arraycopy(face.getFaceRect(), 0, cropRect, 0, cropRect.length);
        cropRect[0] = cropRect[0] - cropRect[2] / 4; // left
        cropRect[1] = cropRect[1] - cropRect[3] * 9 / 10; // top
        cropRect[2] = cropRect[2] * 3 / 2; // width
        cropRect[3] = cropRect[3] * 2; // height

        byte[] cropRgb24 = FaceApi.getInstance().cropRgb24(rgb24, width, height, cropRect);
        if (cropRgb24 != null) {
            // 获取jpg数据可发送服务器做比对
            byte[] jpg = FaceApi.getInstance().rgb24ToJpg(cropRgb24, cropRect[2], cropRect[3], 0, mDualFaceConfig.getImgCompress());
            return jpg;
        }
        return null;
    }

    /**
     * 检测晃动
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isVarianceOk(int x, int y) {
        xQueue.offer(x);
        yQueue.offer(y);

        if (xQueue.size() > QUEUE_CACHE_SIZE) {
            double xVariance = computeVariance(xQueue);
            double yVariance = computeVariance(yQueue);
//            Logs.e(TAG, "x方差：" + xVariance);
//            Logs.e(TAG, "y方差：" + yVariance);
            xQueue.remove();
            yQueue.remove();

            if (xVariance > VARIANCE_VALUE || yVariance > VARIANCE_VALUE) {
                Logs.w(TAG, "请不要晃动");
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private void sleep(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final class DetectFaceTask implements Runnable {

        private boolean isStoped = false;

        public void stop() {
            isStoped = true;
        }

        @Override
        public void run() {
            while (true) {
                if (isStoped) {
                    break;
                }
                // 红外摄像头数据
                byte[] nirYuvBytes = mCameraTextureViewNir.getCameraInterface().getCameraBytes();
                // 可见光摄像头数据
                byte[] visYuvBytes = mCameraTextureViewVis.getCameraInterface().getCameraBytes();

                if (nirYuvBytes == null || visYuvBytes == null) {
                    sleep(100);
                    continue;
                }

                // 检测人脸
                FaceInfo visFaceInfo = FaceApi.getInstance().detectByNV21(visYuvBytes, mPreviewWidth, mPreviewHeight, DualFaceConfig.sVisRotate);

                if (isLiveDetecting && System.currentTimeMillis() - mDetectTime > mDualFaceConfig.getTimeout() * 1000) {
                    isLiveDetecting = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHintTv.setText(R.string.text_detect_timeout);
                        }
                    }, 0);
                    error(ERROR_CODE_TIMEOUT, "超时");
                }

                if (visFaceInfo.isHasFaces()) {
                    Face face = visFaceInfo.getFaceList().get(0);
                    int[] facePoints = face.getFacePoints();
                    int[] rect = face.getFaceRect();
                    Logs.i(TAG, "Turn:" + face.getYawDegree());
                    Logs.i(TAG, "Nod:" + face.getPitchDegree());

                    // 有人脸画框
                    mFrameFaceView.setLocFace(rect);

                    if (isLiveDetecting) {
                        if (!isVarianceOk(facePoints[60], facePoints[61])) {
                            mHintTv.post(new Runnable() {
                                @Override
                                public void run() {
                                    mHintTv.setText(R.string.text_do_not_shake);
                                }
                            });
                            continue;
                        } else {
                            if (rect[2] < mDualFaceConfig.getDistanceMin()) {
                                mHintTv.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mHintTv.setText(R.string.text_be_closer);
                                    }
                                });
                                continue;
                            } else if (rect[2] > mDualFaceConfig.getDistanceMax()) {
                                mHintTv.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mHintTv.setText(R.string.text_be_further);
                                    }
                                });
                                continue;
                            } else if (face.getYawDegree() >= mDualFaceConfig.getYawDegree() || face.getYawDegree() <= -mDualFaceConfig.getYawDegree()) {
                                mHintTv.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mHintTv.setText(R.string.text_face_facing_camera);
                                    }
                                });
                                continue;
                            } else if (face.getPitchDegree() >= mDualFaceConfig.getPitchDegree() || face.getPitchDegree() <= -mDualFaceConfig.getPitchDegree()) {
                                mHintTv.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mHintTv.setText(R.string.text_face_facing_camera);
                                    }
                                });
                                continue;
                            } else if (face.getRollDegree() >= mDualFaceConfig.getRollDegree() || face.getRollDegree() <= -mDualFaceConfig.getRollDegree()) {
                                mHintTv.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mHintTv.setText(R.string.text_face_facing_camera);
                                    }
                                });
                                continue;
                            }
                            mHintTv.post(new Runnable() {
                                @Override
                                public void run() {
                                    mHintTv.setText(R.string.text_look_directly_at_the_camera);
                                }
                            });

                            // 红外检活
                            FaceInfo nirFaceInfo = FaceApi.getInstance().detectByNV21(nirYuvBytes, mPreviewWidth, mPreviewHeight, DualFaceConfig.sNirRotate);
                            liveDetect(nirFaceInfo, visFaceInfo);
                        }
                    }
                } else {
                    mFrameFaceView.setLocFace(null);
                }
            }
        }
    }

    public void liveDetect(FaceInfo nirFaceInfo, FaceInfo visFaceInfo) {
        if (!nirFaceInfo.isHasFaces()) {
            printLog("红外未检测到人脸", RED);
            if (mDualFaceConfig.isDebug()) {
                final byte[] jpgB = FaceApi.getInstance().rgb24ToJpg(nirFaceInfo.getRgb24(), nirFaceInfo.getWidth(), nirFaceInfo.getHeight(), 0, 100);
                String name = System.currentTimeMillis() + "";

                FileUtils.writeFile(EYECOOL_FACE_PATH_DEBUG + "/NoFace_" + logFileName + "/" + name + ".jpg", jpgB);
            }
            return;
        }

        FaceLiveInfo faceLiveInfo = FaceLiveApi.getInstance().nirLive(nirFaceInfo);

        int[] faceRect = nirFaceInfo.getFaceList().get(0).getFaceRect();
        Logs.i(TAG, "x:" + faceRect[0] + " y:" + faceRect[1] + " w:" + faceRect[2] + " h:" + faceRect[3]);

        int[] nirRect = nirFaceInfo.getFaceList().get(0).getFaceRect();
        int[] visRect = visFaceInfo.getFaceList().get(0).getFaceRect();

        if (!compareRect(nirRect, visRect, 0)) {
            Logs.e(TAG, "双目坐标位置不正确...");

            printLog("双目坐标位置不正确...", RED);
            Logs.e(TAG, "nirRect [ " + nirRect[0] + ", " + nirRect[1] + ", " + nirRect[2] + ", " + nirRect[3] + "]");
            Logs.e(TAG, "visRect [ " + visRect[0] + ", " + visRect[1] + ", " + visRect[2] + ", " + visRect[3] + "]");
            return;
        }

        printLog("检活分数: " + faceLiveInfo.getScore()[0], GREEN);

        if (faceLiveInfo.getScore()[0] > mDualFaceConfig.getThreshold()) {
            index++;
        } else {
            index = 0;

            if (mDualFaceConfig.isDebug()) {
                String name = System.currentTimeMillis() + "";
                final byte[] jpgB = FaceApi.getInstance().rgb24ToJpg(nirFaceInfo.getRgb24(), nirFaceInfo.getWidth(), nirFaceInfo.getHeight(), 0, 100);
                FileUtils.writeFile(EYECOOL_FACE_PATH_DEBUG + "/NoLive_" + logFileName + "/" + name + ".jpg", jpgB);
                FileUtils.writeFile(EYECOOL_FACE_PATH_DEBUG + "/NoLive_" + logFileName + "/" + name + "_" + faceLiveInfo.getScore()[0] + ".bin", nirFaceInfo.getRgb24());
            }
        }

        if (index >= mDualFaceConfig.getNirCount()) {
            String filePath1 = EYECOOL_FACE_PATH + "/TempFace1.jpg";
            String filePath2 = EYECOOL_FACE_PATH + "/TempFace2.jpg";
            String cropImgFilePath2 = EYECOOL_FACE_PATH + "/crop_face2.jpg";

            final byte[] jpgBytes1 = FaceApi.getInstance().rgb24ToJpg(nirFaceInfo.getRgb24(), nirFaceInfo.getWidth(), nirFaceInfo.getHeight(), 0, mDualFaceConfig.getImgCompress());
            final byte[] jpgBytes2 = FaceApi.getInstance().rgb24ToJpg(visFaceInfo.getRgb24(), visFaceInfo.getWidth(), visFaceInfo.getHeight(), 0, mDualFaceConfig.getImgCompress());
            final byte[] jpgCropByte2 = cropFaceRgb24ToJpg(visFaceInfo.getFaceList().get(0), visFaceInfo.getRgb24(), visFaceInfo.getWidth(), visFaceInfo.getHeight());

            List<byte[]> images = new ArrayList<>();
            images.add(jpgBytes1);
            images.add(jpgBytes2);
            images.add(jpgCropByte2);

            success(images);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHintTv.setText(R.string.text_live_success);
                }
            }, 0);

            FileUtils.writeFile(filePath1, jpgBytes1);
            FileUtils.writeFile(filePath2, jpgBytes2);
            FileUtils.writeFile(cropImgFilePath2, jpgCropByte2);

            isLiveDetecting = false;
        }
    }

    private void success(List<byte[]> images) {
        if (mDetectCallback != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDetectCallback.onSuccess(images);
                }
            }, 0);
        }
    }

    private void error(int errCode, String msg) {
        if (mDetectCallback != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDetectCallback.onError(errCode, msg);
                }
            }, 0);
        }
    }

    public static boolean compareRect(int[] nir_face_rect, int[] color_face_rect, int nFlip) {
        int nir_x = nir_face_rect[0], nir_y = nir_face_rect[1], nir_w = nir_face_rect[2], nir_h = nir_face_rect[3];
        int col_x = color_face_rect[0], col_y = color_face_rect[1], col_w = color_face_rect[2], col_h = color_face_rect[3];

        //初步判断所有的数值必须大于等于0
        if ((nir_x < 0) || (nir_y < 0) || (nir_w <= 20) || (nir_h <= 20)) {
            return false;
        }
        if ((col_x < 0) || (col_y < 0) || (col_w <= 20) || (col_h <= 20)) {
            return false;
        }

        // 两个矩形框：只要有重合就行
        if (nir_x > col_x + col_w ||
                nir_x + nir_w < col_x ||
                nir_y > col_y + col_h ||
                nir_y + nir_h < col_y) {
            return false;
        }

        //判断人脸宽的限制
        if ((float) (Math.abs(nir_w - col_w) * 1.0 / nir_w) > 0.2) {
            return false;
        }

        //人脸框中心点的x,y值， delta_y, delta_x
        int center_nir_x = nir_x + nir_w / 2;
        int center_nir_y = nir_y + nir_h / 2;
        int center_color_x = col_x + col_w / 2;
        int center_color_y = col_y + col_h / 2;

        // 近红外中心点必须在可见光的左上方（x要小）
        // 未翻转
        if (nFlip == 0 && (center_nir_x > center_color_x + 10)) {
            if (Math.abs(center_nir_x - center_color_x) > 60) {
                return false;
            }
        }

        // 翻转
        if (nFlip == 1 && (center_color_x > center_nir_x + 10))    // 左右相对关系反了
        {
            if (Math.abs(center_nir_x - center_color_x) > 60) {
                return false;
            }
        }

        // 两个中心点的纵坐标差值不宜过大
        if (Math.abs(center_nir_y - center_color_y) > 60) {
            return false;
        }

        return true;
    }

    String RED = "#FF0000";
    String GREEN = "#00FF00";

    int lineNum = 0;

    private String logFileName = "";

    private void printLog(String msg, String color) {
        if (mDualFaceConfig.isDebug()) {
            Date date = new Date();
            String nowTime = mDateFormat.format(date);
            String log = nowTime + " > " + msg;
            FileUtils.writeFile(EYECOOL_FACE_PATH_DEBUG + "/" + logFileName + ".log", log + "\n", true);
            lineNum++;
            mInfoTv.post(() -> {
                if (lineNum >= 20) {
                    mInfoTv.setText("");
                    lineNum = 0;
                }

                String htmlMsg = textToHtml(msg + "<br>", color);
                Spanned spanned = Html.fromHtml(htmlMsg);
                mInfoTv.append(spanned);
            });
        }
    }

    private String textToHtml(String msg, String color) {
        return "<font color=\"" + color + "\">" + msg + "</font>";
    }

    private String getNowDate() {
        //时间格式化
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private String getNowDateTime() {
        //时间格式化
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
