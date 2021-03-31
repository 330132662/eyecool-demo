package com.eyecool.face.detect.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.eyecool.face.detect.demo.config.DetectConfig;
import com.eyecool.utils.SharedPreferenceUtil;

import static com.eyecool.camera.config.CameraConfig.CAMERA_0;
import static com.eyecool.camera.config.CameraConfig.CAMERA_1;
import static com.eyecool.camera.config.CameraConfig.ROTATE_0;
import static com.eyecool.camera.config.CameraConfig.ROTATE_180;
import static com.eyecool.camera.config.CameraConfig.ROTATE_270;
import static com.eyecool.camera.config.CameraConfig.ROTATE_90;

public class SettingActivity extends AppCompatActivity {

    private static final String TAG = SettingActivity.class.getSimpleName();

    private RadioGroup mCameraRadioGroup;
    private RadioGroup mPreviewRadioGroup;
    private RadioGroup mRotateRadioGroup;
    private RadioGroup mScaleRadioGroup;
    private Switch mPreviewMirrorSwitch;
    private Switch mDataMirrorSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mCameraRadioGroup = findViewById(R.id.cameraRadioGroup);
        mPreviewRadioGroup = findViewById(R.id.previewRadioGroup);
        mRotateRadioGroup = findViewById(R.id.rotateRadioGroup);
        mScaleRadioGroup = findViewById(R.id.scaleRadioGroup);
        mPreviewMirrorSwitch = findViewById(R.id.previewMirror);
        mDataMirrorSwitch = findViewById(R.id.dataMirror);
        findViewById(R.id.saveBtn).setOnClickListener(v -> save());

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

        switch (DetectConfig.sCameraId) {
            case CAMERA_0:
                mCameraRadioGroup.check(R.id.camera0);
                break;
            case CAMERA_1:
                mCameraRadioGroup.check(R.id.camera1);
                break;
        }
        switch (DetectConfig.sPreviewOrientation) {
            case ROTATE_0:
                mPreviewRadioGroup.check(R.id.previewRadio0);
                break;
            case ROTATE_90:
                mPreviewRadioGroup.check(R.id.previewRadio90);
                break;
            case ROTATE_180:
                mPreviewRadioGroup.check(R.id.previewRadio180);
                break;
            case ROTATE_270:
                mPreviewRadioGroup.check(R.id.previewRadio270);
                break;
        }
        switch (DetectConfig.sRotate) {
            case ROTATE_0:
                mRotateRadioGroup.check(R.id.rotateRadio0);
                break;
            case ROTATE_90:
                mRotateRadioGroup.check(R.id.rotateRadio90);
                break;
            case ROTATE_180:
                mRotateRadioGroup.check(R.id.rotateRadio180);
                break;
            case ROTATE_270:
                mRotateRadioGroup.check(R.id.rotateRadio270);
                break;
        }
        switch (DetectConfig.sPreviewScale) {
            case NONE:
                mScaleRadioGroup.check(R.id.scaleRadio0);
                break;
            case SCALE_4_3:
                mScaleRadioGroup.check(R.id.scaleRadio1);
                break;
            case SCALE_3_4:
                mScaleRadioGroup.check(R.id.scaleRadio2);
                break;
        }
        mPreviewMirrorSwitch.setChecked(DetectConfig.isPreviewMirror);
        mDataMirrorSwitch.setChecked(DetectConfig.isDataMirror);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, FaceTestActivity.class));
        finish();
    }

    private void save() {
        switch (mCameraRadioGroup.getCheckedRadioButtonId()) {
            case R.id.camera0:
                DetectConfig.sCameraId = CAMERA_0;
                break;
            case R.id.camera1:
                DetectConfig.sCameraId = CAMERA_1;
                break;
        }
        switch (mPreviewRadioGroup.getCheckedRadioButtonId()) {
            case R.id.previewRadio0:
                DetectConfig.sPreviewOrientation = ROTATE_0;
                break;
            case R.id.previewRadio90:
                DetectConfig.sPreviewOrientation = ROTATE_90;
                break;
            case R.id.previewRadio180:
                DetectConfig.sPreviewOrientation = ROTATE_180;
                break;
            case R.id.previewRadio270:
                DetectConfig.sPreviewOrientation = ROTATE_270;
                break;
        }
        switch (mRotateRadioGroup.getCheckedRadioButtonId()) {
            case R.id.rotateRadio0:
                DetectConfig.sRotate = ROTATE_0;
                break;
            case R.id.rotateRadio90:
                DetectConfig.sRotate = ROTATE_90;
                break;
            case R.id.rotateRadio180:
                DetectConfig.sRotate = ROTATE_180;
                break;
            case R.id.rotateRadio270:
                DetectConfig.sRotate = ROTATE_270;
                break;
        }
        switch (mScaleRadioGroup.getCheckedRadioButtonId()) {
            case R.id.scaleRadio0:
                DetectConfig.sPreviewScale = DetectConfig.PreviewScale.NONE;
                break;
            case R.id.scaleRadio1:
                DetectConfig.sPreviewScale = DetectConfig.PreviewScale.SCALE_4_3;
                break;
            case R.id.scaleRadio2:
                DetectConfig.sPreviewScale = DetectConfig.PreviewScale.SCALE_3_4;
                break;
        }
        DetectConfig.isPreviewMirror = mPreviewMirrorSwitch.isChecked();
        DetectConfig.isDataMirror = mDataMirrorSwitch.isChecked();

        SharedPreferenceUtil.getInstance().saveInt(DetectConfig.SP_CAMERA_ID, DetectConfig.sCameraId);
        SharedPreferenceUtil.getInstance().saveInt(DetectConfig.SP_PREVIEW_ORIENTATION, DetectConfig.sPreviewOrientation);
        SharedPreferenceUtil.getInstance().saveInt(DetectConfig.SP_ROTATE, DetectConfig.sRotate);
        SharedPreferenceUtil.getInstance().saveInt(DetectConfig.SP_PREVIEW_SCALE, DetectConfig.sPreviewScale.value());
        SharedPreferenceUtil.getInstance().saveBoolean(DetectConfig.SP_PREVIEW_MIRROR, DetectConfig.isPreviewMirror);
        SharedPreferenceUtil.getInstance().saveBoolean(DetectConfig.SP_DATA_MIRROR, DetectConfig.isDataMirror);

        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, FaceTestActivity.class));
        finish();
    }
}
