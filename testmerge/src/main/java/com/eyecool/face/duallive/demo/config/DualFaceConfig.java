package com.eyecool.face.duallive.demo.config;

import com.eyecool.utils.Logs;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;

import static com.eyecool.camera.config.CameraConfig.CAMERA_0;
import static com.eyecool.camera.config.CameraConfig.ROTATE_0;

/**
 * 双模人脸检活配置
 * <p>
 * Created by wangzhi on 2017/8/2.
 */
public class DualFaceConfig implements Serializable {

    private static final String TAG = DualFaceConfig.class.getSimpleName();

    public static final String SP_VIS_CAMERA_ID = "vis_camera_id";
    public static final String SP_VIS_ROTATE = "vis_rotate";
    public static final String SP_VIS_PREVIEW_ORIENTATION = "vis_preview_orientation";
    public static final String SP_VIS_PREVIEW_MIRROR = "vis_preview_mirror";
    public static final String SP_VIS_DATA_MIRROR = "vis_data_mirror";

    public static final String SP_NIR_ROTATE = "nir_rotate";
    public static final String SP_NIR_PREVIEW_ORIENTATION = "nir_preview_orientation";
    public static final String SP_NIR_PREVIEW_MIRROR = "nir_preview_mirror";
    public static final String SP_NIR_DATA_MIRROR = "nir_data_mirror";

    public static final String SP_DEBUG = "nir_live_debug";
    public static final String SP_TIMEOUT = "nir_live_timeout";
    public static final String SP_LIVE_THRESHOLD = "nir_live_threshold";
    public static final String SP_LIVE_COUNT = "nir_live_count";
    public static final String SP_MIN_FACE = "min_face_size";
    public static final String SP_MAX_FACE = "max_face_size";

    public static final String SP_YAW_LIMIT = "face_yaw_limit";
    public static final String SP_PITCH_LIMIT = "face_pitch_limit";
    public static final String SP_ROLL_LIMIT = "face_roll_limit";

    public static final String SP_PREVIEW_SCALE = "dual_preview_scale";

    private static DualFaceConfig mDualFaceConfig;

    private DualFaceConfig() {
    }

    public static DualFaceConfig getDualFaceConfig() {
        if (mDualFaceConfig == null) {
            mDualFaceConfig = new DualFaceConfig();
        }
        return mDualFaceConfig;
    }

    // RGB摄像头参数设置
    /**
     * 摄像头id号
     */
    public static int sVisCameraId = CAMERA_0;
    /**
     * 摄像头预览宽
     */
    public static int sPreviewWidth = 640;
    /**
     * 摄像头预览高
     */
    public static int sPreviewHeight = 480;
    /**
     * 摄像头出图旋转角度
     */
    public static int sVisRotate = ROTATE_0;
    /**
     * 摄像头预览角度
     */
    public static int sVisPreviewOrientation = ROTATE_0;
    /**
     * 预览显示是否镜像
     */
    public static boolean isVisPreviewMirror = false;
    /**
     * 数据处理是否镜像
     */
    public static boolean isVisDataMirror = false;

    // NIR摄像头参数设置
    /**
     * 摄像头出图旋转角度
     */
    public static int sNirRotate = ROTATE_0;
    /**
     * 摄像头预览角度
     */
    public static int sNirPreviewOrientation = ROTATE_0;
    /**
     * 预览显示是否镜像
     */
    public static boolean isNirPreviewMirror = false;
    /**
     * 数据处理是否镜像
     */
    public static boolean isNirDataMirror = false;
    /**
     * 预览尺寸比例
     */
    public static PreviewScale sPreviewScale = PreviewScale.NONE;

    public enum PreviewScale {
        NONE(0), SCALE_4_3(1), SCALE_3_4(2);

        private int value;

        PreviewScale(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    private static String sMessage;
    private int mWidth = 640;
    private int mHeight = 480;
    private int mImgCompress = 85; // 图像压缩率（1-100）
    private int mNirCount = 2; // 检活通过次数
    private int isActived = 2; // 检活类型
    private int mTimeout = 10; // 检活超时秒
    private float mThreshold = 0.7f; // 检活默认阈值
    private int mPidL = 0x2210;
    private int mPidR = 0x2209;
    private int mDistanceMin = 60;// 人脸最小尺寸
    private int mDistanceMax = 200;// 人脸最大尺寸
    private int mYawDegree = 10;
    private int mPitchDegree = 10;
    private int mRollDegree = 10;
    private boolean isDebug = false;

    public static String getMessage() {
        return sMessage;
    }

    public static void setMessage(String message) {
        sMessage = message;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getImgCompress() {
        return mImgCompress;
    }

    public void setImgCompress(int imgCompress) {
        mImgCompress = imgCompress;
    }

    public int getNirCount() {
        return mNirCount;
    }

    public void setNirCount(int nirCount) {
        mNirCount = nirCount;
    }

    public int getIsActived() {
        return isActived;
    }

    public void setIsActived(int isActived) {
        this.isActived = isActived;
    }

    public int getTimeout() {
        return mTimeout;
    }

    public void setTimeout(int timeout) {
        mTimeout = timeout;
    }

    public float getThreshold() {
        return mThreshold;
    }

    public void setThreshold(float threshold) {
        mThreshold = threshold;
    }

    public int getPidL() {
        return mPidL;
    }

    public void setPidL(int pidL) {
        mPidL = pidL;
    }

    public int getPidR() {
        return mPidR;
    }

    public void setPidR(int pidR) {
        mPidR = pidR;
    }

    public int getDistanceMin() {
        return mDistanceMin;
    }

    public void setDistanceMin(int distanceMin) {
        mDistanceMin = distanceMin;
    }

    public int getDistanceMax() {
        return mDistanceMax;
    }

    public void setDistanceMax(int distanceMax) {
        mDistanceMax = distanceMax;
    }

    public int getYawDegree() {
        return mYawDegree;
    }

    public void setYawDegree(int yawDegree) {
        mYawDegree = yawDegree;
    }

    public int getPitchDegree() {
        return mPitchDegree;
    }

    public void setPitchDegree(int pitchDegree) {
        mPitchDegree = pitchDegree;
    }

    public int getRollDegree() {
        return mRollDegree;
    }

    public void setRollDegree(int rollDegree) {
        mRollDegree = rollDegree;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    @Override
    public String toString() {
        return "DualFaceConfig{" +
                "mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mImgCompress=" + mImgCompress +
                ", mNirCount=" + mNirCount +
                ", isActived=" + isActived +
                ", mTimeout=" + mTimeout +
                ", mThreshold=" + mThreshold +
                ", mPidL=" + mPidL +
                ", mPidR=" + mPidR +
                ", mDistanceMin=" + mDistanceMin +
                ", mDistanceMax=" + mDistanceMax +
                ", mYawDegree=" + mYawDegree +
                ", mPitchDegree=" + mPitchDegree +
                ", mRollDegree=" + mRollDegree +
                ", isDebug=" + isDebug +
                '}';
    }

    public boolean parseXML(String param) {
        try {
            XmlPullParserFactory pullParserFactory = XmlPullParserFactory
                    .newInstance();
            XmlPullParser xmlPullParser = pullParserFactory.newPullParser();
            StringReader reader = new StringReader(param);
            xmlPullParser.setInput(reader);
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (!checkParams(xmlPullParser, nodeName)) {
                            return false;
                        }
                        break;
                    default:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            sMessage = "解析异常XmlPullParserException，请检查是否有非法参数或是参数越界";
            Logs.e(TAG, e.getMessage());
            return false;
        } catch (Exception e) {
            sMessage = "参数错误，请检查是否有非法参数或是参数越界";
            Logs.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    private boolean checkParams(XmlPullParser xmlPullParser, String nodeName)
            throws XmlPullParserException, IOException {
        try {
            String text = null;
            if ("imgWidth".equals(nodeName)) {
                int imgWidth = Integer.valueOf(xmlPullParser.nextText());
                text = "imgWidth";
                if (!(imgWidth >= 200)) {
                    sMessage = text + "参数错误，请检查是否有非法参数或是参数越界";
                    return false;
                }
                setWidth(imgWidth);
            }
            if ("imgHeight".equals(nodeName)) {
                int imgHeight = Integer.valueOf(xmlPullParser.nextText());
                text = "imgHeight";
                if (!(imgHeight >= 200)) {
                    sMessage = text + "参数错误，请检查是否有非法参数或是参数越界";
                    return false;
                }
                setHeight(imgHeight);
            }
            if ("imgCompress".equals(nodeName)) {
                int imgCompress = Integer.valueOf(xmlPullParser.nextText());
                text = "imgCompress";
                if (!(imgCompress >= 0 && imgCompress <= 100)) {
                    sMessage = text + "参数错误，请检查是否有非法参数或是参数越界";
                    return false;
                }
                setImgCompress(imgCompress);
            }
            if ("NirCount".equals(nodeName)) {
                int nirCount = Integer.valueOf(xmlPullParser.nextText());
                text = "NirCount";
                if (nirCount < 0) {
                    sMessage = text + "参数错误，请检查是否有非法参数或是参数越界";
                    return false;
                }
                setNirCount(nirCount);
            }
            if ("isActived".equals(nodeName)) {
                int isActived = Integer.valueOf(xmlPullParser.nextText());
                text = "isActived";
                if (!(isActived == 0 || isActived == 1 || isActived == 2 || isActived == 3)) {
                    sMessage = text + "参数错误，请检查是否有非法参数或是参数越界";
                    return false;
                }
                setIsActived(isActived);
            }
            if ("timeOut".equals(nodeName)) {
                int timeout = Integer.valueOf(xmlPullParser.nextText());
                text = "timeOut";
                if (!(timeout >= 10 && timeout <= 120)) {
                    sMessage = text + "参数错误，请检查是否有非法参数或是参数越界(10-120)";
                    return false;
                }
                setTimeout(timeout);
            }
            if ("liveThreshold".equals(nodeName)) {
                float liveThreshold = Float.valueOf(xmlPullParser.nextText());
                text = "liveThreshold";
                if (!(liveThreshold >= 0 && liveThreshold <= 1)) {
                    sMessage = text + "参数错误，请检查是否有非法参数或是参数越界(0-1)";
                    return false;
                }
                setThreshold(liveThreshold);
            }
            if ("pupilDistMin".equals(nodeName)) {
                int distanceMin = Integer.valueOf(xmlPullParser.nextText());
                text = "pupilDistMin";
                if (!(distanceMin >= 0 && distanceMin < 200)) {
                    sMessage = text + "参数错误，请检查是否有非法参数或是参数越界(70-200)";
                    return false;
                }
                setDistanceMin(distanceMin);
            }
            if ("pupilDistMax".equals(nodeName)) {
                int distanceMax = Integer.valueOf(xmlPullParser.nextText());
                text = "pupilDistMax";
                if (!(distanceMax >= 30 && distanceMax < 480)) {
                    sMessage = text + "参数错误，请检查是否有非法参数或是参数越界";
                    return false;
                }
                setDistanceMax(distanceMax);
            }
            if ("pidL".equals(nodeName)) {
                int pidL = Integer.parseInt(xmlPullParser.nextText(), 16);
                text = "pidL";
                setPidL(pidL);
            }
            if ("pidR".equals(nodeName)) {
                int pidR = Integer.parseInt(xmlPullParser.nextText(), 16);
                text = "pidR";
                setPidR(pidR);
            }
            if ("debug".equals(nodeName)) {
                int debug = Integer.parseInt(xmlPullParser.nextText(), 16);
                text = "debug";
                setDebug(debug == 0 ? false : true);
            }
        } catch (Exception e) {
            sMessage = "参数格式错误，请检查是否有非法参数或是参数越界";
            return false;
        }

        return true;
    }
}
