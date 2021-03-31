package com.eyecool.face.detect.demo.config;

import static com.eyecool.camera.config.CameraConfig.CAMERA_0;
import static com.eyecool.camera.config.CameraConfig.ROTATE_0;

public class DetectConfig {

    public static final String SP_CAMERA_ID = "camera_id";
    public static final String SP_ROTATE = "rotate";
    public static final String SP_PREVIEW_ORIENTATION = "preview_orientation";
    public static final String SP_PREVIEW_SCALE = "preview_scale";
    public static final String SP_PREVIEW_MIRROR = "preview_mirror";
    public static final String SP_DATA_MIRROR = "data_mirror";

    /**
     * 摄像头id号
     */
    public static int sCameraId = CAMERA_0;
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
    public static int sRotate = ROTATE_0;
    /**
     * 摄像头预览角度
     */
    public static int sPreviewOrientation = ROTATE_0;
    /**
     * 预览显示是否镜像
     */
    public static boolean isPreviewMirror = false;
    /**
     * 数据处理是否镜像
     */
    public static boolean isDataMirror = false;
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
}
