package com.eyecool.face.duallive.demo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.eyecool.face.duallive.demo.config.DualFaceConfig;
import com.eyecool.face.duallive.demo.fragment.DualSysCameraFragment;
import com.eyecool.utils.SharedPreferenceUtil;

import static com.eyecool.camera.config.CameraConfig.CAMERA_0;
import static com.eyecool.camera.config.CameraConfig.CAMERA_1;
import static com.eyecool.camera.config.CameraConfig.ROTATE_0;
import static com.eyecool.camera.config.CameraConfig.ROTATE_180;
import static com.eyecool.camera.config.CameraConfig.ROTATE_270;
import static com.eyecool.camera.config.CameraConfig.ROTATE_90;

public class SettingActivity extends AppCompatActivity {

    /**
     * RGB摄像头
     */
    private RadioGroup mRgbCameraRadioGroup;
    private RadioGroup mRgbPreviewRadioGroup;
    private RadioGroup mRgbRotateRadioGroup;
    private Switch mRgbPreviewMirrorSwitch;
    private Switch mRgbDataMirrorSwitch;
    /**
     * NIR摄像头
     */
    private RadioGroup mNirPreviewRadioGroup;
    private RadioGroup mNirRotateRadioGroup;
    private Switch mNirPreviewMirrorSwitch;
    private Switch mNirDataMirrorSwitch;

    private Switch mDebugSwitch;
    private EditText mTimeoutEt;
    private EditText mLiveThresholdEt;
    private EditText mLiveCountEt;
    private EditText mMinFaceEt;
    private EditText mMaxFaceEt;

    /**
     * 姿态限制
     */
    private EditText mYawEt;
    private EditText mPitchEt;
    private EditText mRollEt;

    private RadioGroup mScaleRadioGroup;

    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAlertDialog = new AlertDialog.Builder(this).setMessage("开启调试模式，日志及图片保存路径如下：\n" + DualSysCameraFragment.EYECOOL_FACE_DEBUG_DIR)
                .setPositiveButton("确定", (dialog, which) -> dialog.dismiss()).create();

        mRgbCameraRadioGroup = findViewById(R.id.rgbCameraRadioGroup);
        mRgbPreviewRadioGroup = findViewById(R.id.rgbPreviewRadioGroup);
        mRgbRotateRadioGroup = findViewById(R.id.rgbRotateRadioGroup);
        mRgbPreviewMirrorSwitch = findViewById(R.id.rgbPreviewMirror);
        mRgbDataMirrorSwitch = findViewById(R.id.rgbDataMirror);

        mNirPreviewRadioGroup = findViewById(R.id.nirPreviewRadioGroup);
        mNirRotateRadioGroup = findViewById(R.id.nirRotateRadioGroup);
        mNirPreviewMirrorSwitch = findViewById(R.id.nirPreviewMirror);
        mNirDataMirrorSwitch = findViewById(R.id.nirDataMirror);

        mDebugSwitch = findViewById(R.id.debugSwitch);
        mTimeoutEt = findViewById(R.id.timeoutEt);
        mLiveThresholdEt = findViewById(R.id.livethresholdEt);
        mLiveCountEt = findViewById(R.id.liveCountEt);
        mMinFaceEt = findViewById(R.id.minFaceEt);
        mMaxFaceEt = findViewById(R.id.maxFaceEt);

        mYawEt = findViewById(R.id.yawEt);
        mPitchEt = findViewById(R.id.pitchEt);
        mRollEt = findViewById(R.id.rollEt);

        mScaleRadioGroup = findViewById(R.id.scaleRadioGroup);

        findViewById(R.id.saveBtn).setOnClickListener(v -> save());

        initRgb();

        initNir();

        boolean isDebug = SharedPreferenceUtil.getInstance().getBoolean(DualFaceConfig.SP_DEBUG);
        mDebugSwitch.setChecked(isDebug);
        DualFaceConfig.getDualFaceConfig().setDebug(isDebug);
        mDebugSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mAlertDialog.show();
            }
        });

        long timeout = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_TIMEOUT, DualFaceConfig.getDualFaceConfig().getTimeout());
        mTimeoutEt.setText(timeout + "");

        float livethreshold = SharedPreferenceUtil.getInstance().getFloat(DualFaceConfig.SP_LIVE_THRESHOLD, DualFaceConfig.getDualFaceConfig().getThreshold());
        mLiveThresholdEt.setText(livethreshold + "");

        int liveCount = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_LIVE_COUNT, DualFaceConfig.getDualFaceConfig().getNirCount());
        mLiveCountEt.setText(liveCount + "");

        int minFace = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_MIN_FACE, DualFaceConfig.getDualFaceConfig().getDistanceMin());
        mMinFaceEt.setText(minFace + "");

        int maxFace = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_MAX_FACE, DualFaceConfig.getDualFaceConfig().getDistanceMax());
        mMaxFaceEt.setText(maxFace + "");

        int yaw = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_YAW_LIMIT, DualFaceConfig.getDualFaceConfig().getYawDegree());
        mYawEt.setText(yaw + "");

        int pitch = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_PITCH_LIMIT, DualFaceConfig.getDualFaceConfig().getPitchDegree());
        mPitchEt.setText(pitch + "");

        int roll = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_ROLL_LIMIT, DualFaceConfig.getDualFaceConfig().getRollDegree());
        mRollEt.setText(roll + "");

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
        switch (DualFaceConfig.sPreviewScale) {
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, DualSysCameraActivity.class));
    }

    private void initRgb() {
        /**
         * rgb参数设置
         */
        DualFaceConfig.sVisCameraId = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_VIS_CAMERA_ID);
        DualFaceConfig.sVisPreviewOrientation = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_VIS_PREVIEW_ORIENTATION);
        DualFaceConfig.sVisRotate = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_VIS_ROTATE);
        DualFaceConfig.isVisPreviewMirror = SharedPreferenceUtil.getInstance().getBoolean(DualFaceConfig.SP_VIS_PREVIEW_MIRROR);
        DualFaceConfig.isVisDataMirror = SharedPreferenceUtil.getInstance().getBoolean(DualFaceConfig.SP_VIS_DATA_MIRROR);
        switch (DualFaceConfig.sVisCameraId) {
            case CAMERA_0:
                mRgbCameraRadioGroup.check(R.id.rgbCamera0);
                break;
            case CAMERA_1:
                mRgbCameraRadioGroup.check(R.id.rgbCamera1);
                break;
        }
        switch (DualFaceConfig.sVisPreviewOrientation) {
            case ROTATE_0:
                mRgbPreviewRadioGroup.check(R.id.rgbPreviewRadio0);
                break;
            case ROTATE_90:
                mRgbPreviewRadioGroup.check(R.id.rgbPreviewRadio90);
                break;
            case ROTATE_180:
                mRgbPreviewRadioGroup.check(R.id.rgbPreviewRadio180);
                break;
            case ROTATE_270:
                mRgbPreviewRadioGroup.check(R.id.rgbPreviewRadio270);
                break;
        }
        switch (DualFaceConfig.sVisRotate) {
            case ROTATE_0:
                mRgbRotateRadioGroup.check(R.id.rgbRotateRadio0);
                break;
            case ROTATE_90:
                mRgbRotateRadioGroup.check(R.id.rgbRotateRadio90);
                break;
            case ROTATE_180:
                mRgbRotateRadioGroup.check(R.id.rgbRotateRadio180);
                break;
            case ROTATE_270:
                mRgbRotateRadioGroup.check(R.id.rgbRotateRadio270);
                break;
        }
        mRgbPreviewMirrorSwitch.setChecked(DualFaceConfig.isVisPreviewMirror);
        mRgbDataMirrorSwitch.setChecked(DualFaceConfig.isVisDataMirror);
    }

    private void initNir() {
        /**
         * nir参数设置
         */
        DualFaceConfig.sNirPreviewOrientation = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_NIR_PREVIEW_ORIENTATION);
        DualFaceConfig.sNirRotate = SharedPreferenceUtil.getInstance().getInt(DualFaceConfig.SP_NIR_ROTATE);
        DualFaceConfig.isNirPreviewMirror = SharedPreferenceUtil.getInstance().getBoolean(DualFaceConfig.SP_NIR_PREVIEW_MIRROR);
        DualFaceConfig.isNirDataMirror = SharedPreferenceUtil.getInstance().getBoolean(DualFaceConfig.SP_NIR_DATA_MIRROR);

        switch (DualFaceConfig.sNirPreviewOrientation) {
            case ROTATE_0:
                mNirPreviewRadioGroup.check(R.id.nirPreviewRadio0);
                break;
            case ROTATE_90:
                mNirPreviewRadioGroup.check(R.id.nirPreviewRadio90);
                break;
            case ROTATE_180:
                mNirPreviewRadioGroup.check(R.id.nirPreviewRadio180);
                break;
            case ROTATE_270:
                mNirPreviewRadioGroup.check(R.id.nirPreviewRadio270);
                break;
        }
        switch (DualFaceConfig.sNirRotate) {
            case ROTATE_0:
                mNirRotateRadioGroup.check(R.id.nirRotateRadio0);
                break;
            case ROTATE_90:
                mNirRotateRadioGroup.check(R.id.nirRotateRadio90);
                break;
            case ROTATE_180:
                mNirRotateRadioGroup.check(R.id.nirRotateRadio180);
                break;
            case ROTATE_270:
                mNirRotateRadioGroup.check(R.id.nirRotateRadio270);
                break;
        }
        mNirPreviewMirrorSwitch.setChecked(DualFaceConfig.isNirPreviewMirror);
        mNirDataMirrorSwitch.setChecked(DualFaceConfig.isNirDataMirror);
    }

    private void save() {
        switch (mRgbCameraRadioGroup.getCheckedRadioButtonId()) {
            case R.id.rgbCamera0:
                DualFaceConfig.sVisCameraId = CAMERA_0;
                break;
            case R.id.rgbCamera1:
                DualFaceConfig.sVisCameraId = CAMERA_1;
                break;
        }
        switch (mRgbPreviewRadioGroup.getCheckedRadioButtonId()) {
            case R.id.rgbPreviewRadio0:
                DualFaceConfig.sVisPreviewOrientation = ROTATE_0;
                break;
            case R.id.rgbPreviewRadio90:
                DualFaceConfig.sVisPreviewOrientation = ROTATE_90;
                break;
            case R.id.rgbPreviewRadio180:
                DualFaceConfig.sVisPreviewOrientation = ROTATE_180;
                break;
            case R.id.rgbPreviewRadio270:
                DualFaceConfig.sVisPreviewOrientation = ROTATE_270;
                break;
        }
        switch (mRgbRotateRadioGroup.getCheckedRadioButtonId()) {
            case R.id.rgbRotateRadio0:
                DualFaceConfig.sVisRotate = ROTATE_0;
                break;
            case R.id.rgbRotateRadio90:
                DualFaceConfig.sVisRotate = ROTATE_90;
                break;
            case R.id.rgbRotateRadio180:
                DualFaceConfig.sVisRotate = ROTATE_180;
                break;
            case R.id.rgbRotateRadio270:
                DualFaceConfig.sVisRotate = ROTATE_270;
                break;
        }
        DualFaceConfig.isVisPreviewMirror = mRgbPreviewMirrorSwitch.isChecked();
        DualFaceConfig.isVisDataMirror = mRgbDataMirrorSwitch.isChecked();

        SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_VIS_CAMERA_ID, DualFaceConfig.sVisCameraId);
        SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_VIS_PREVIEW_ORIENTATION, DualFaceConfig.sVisPreviewOrientation);
        SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_VIS_ROTATE, DualFaceConfig.sVisRotate);
        SharedPreferenceUtil.getInstance().saveBoolean(DualFaceConfig.SP_VIS_PREVIEW_MIRROR, DualFaceConfig.isVisPreviewMirror);
        SharedPreferenceUtil.getInstance().saveBoolean(DualFaceConfig.SP_VIS_DATA_MIRROR, DualFaceConfig.isVisDataMirror);

        switch (mNirPreviewRadioGroup.getCheckedRadioButtonId()) {
            case R.id.nirPreviewRadio0:
                DualFaceConfig.sNirPreviewOrientation = ROTATE_0;
                break;
            case R.id.nirPreviewRadio90:
                DualFaceConfig.sNirPreviewOrientation = ROTATE_90;
                break;
            case R.id.nirPreviewRadio180:
                DualFaceConfig.sNirPreviewOrientation = ROTATE_180;
                break;
            case R.id.nirPreviewRadio270:
                DualFaceConfig.sNirPreviewOrientation = ROTATE_270;
                break;
        }
        switch (mNirRotateRadioGroup.getCheckedRadioButtonId()) {
            case R.id.nirRotateRadio0:
                DualFaceConfig.sNirRotate = ROTATE_0;
                break;
            case R.id.nirRotateRadio90:
                DualFaceConfig.sNirRotate = ROTATE_90;
                break;
            case R.id.nirRotateRadio180:
                DualFaceConfig.sNirRotate = ROTATE_180;
                break;
            case R.id.nirRotateRadio270:
                DualFaceConfig.sNirRotate = ROTATE_270;
                break;
        }
        DualFaceConfig.isNirPreviewMirror = mNirPreviewMirrorSwitch.isChecked();
        DualFaceConfig.isNirDataMirror = mNirDataMirrorSwitch.isChecked();

        SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_NIR_PREVIEW_ORIENTATION, DualFaceConfig.sNirPreviewOrientation);
        SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_NIR_ROTATE, DualFaceConfig.sNirRotate);
        SharedPreferenceUtil.getInstance().saveBoolean(DualFaceConfig.SP_NIR_PREVIEW_MIRROR, DualFaceConfig.isNirPreviewMirror);
        SharedPreferenceUtil.getInstance().saveBoolean(DualFaceConfig.SP_NIR_DATA_MIRROR, DualFaceConfig.isNirDataMirror);

        boolean debug = mDebugSwitch.isChecked();
        SharedPreferenceUtil.getInstance().saveBoolean(DualFaceConfig.SP_DEBUG, debug);
        DualFaceConfig.getDualFaceConfig().setDebug(debug);

        String timeoutStr = mTimeoutEt.getText().toString();
        if (TextUtils.isEmpty(timeoutStr)) {
            Toast.makeText(this, "超时时间不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int timeout = Integer.parseInt(timeoutStr);
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_TIMEOUT, timeout);
            DualFaceConfig.getDualFaceConfig().setTimeout(timeout);
        } catch (Exception e) {
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_TIMEOUT, DualFaceConfig.getDualFaceConfig().getTimeout());
        }

        String livethresholdStr = mLiveThresholdEt.getText().toString();
        if (TextUtils.isEmpty(livethresholdStr)) {
            Toast.makeText(this, "检活阈值不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            float livethreshold = Float.parseFloat(livethresholdStr);
            SharedPreferenceUtil.getInstance().saveFloat(DualFaceConfig.SP_LIVE_THRESHOLD, livethreshold);
            DualFaceConfig.getDualFaceConfig().setThreshold(livethreshold);
        } catch (Exception e) {
            SharedPreferenceUtil.getInstance().saveFloat(DualFaceConfig.SP_LIVE_THRESHOLD, DualFaceConfig.getDualFaceConfig().getThreshold());
        }

        String liveCountStr = mLiveCountEt.getText().toString();
        if (TextUtils.isEmpty(liveCountStr)) {
            Toast.makeText(this, "检活次数不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int liveCount = Integer.parseInt(liveCountStr);
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_LIVE_COUNT, liveCount);
            DualFaceConfig.getDualFaceConfig().setNirCount(liveCount);
        } catch (Exception e) {
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_LIVE_COUNT, DualFaceConfig.getDualFaceConfig().getNirCount());
        }

        String minFaceStr = mMinFaceEt.getText().toString();
        if (TextUtils.isEmpty(minFaceStr)) {
            Toast.makeText(this, "最小人脸尺寸不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int minFace = Integer.parseInt(minFaceStr);
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_MIN_FACE, minFace);
            DualFaceConfig.getDualFaceConfig().setDistanceMin(minFace);
        } catch (Exception e) {
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_MIN_FACE, DualFaceConfig.getDualFaceConfig().getDistanceMin());
        }

        String maxFaceStr = mMaxFaceEt.getText().toString();
        if (TextUtils.isEmpty(maxFaceStr)) {
            Toast.makeText(this, "最大人脸尺寸不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int maxFace = Integer.parseInt(maxFaceStr);
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_MAX_FACE, maxFace);
            DualFaceConfig.getDualFaceConfig().setDistanceMax(maxFace);
        } catch (Exception e) {
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_MAX_FACE, DualFaceConfig.getDualFaceConfig().getDistanceMax());
        }

        String yawStr = mYawEt.getText().toString();
        if (TextUtils.isEmpty(yawStr)) {
            Toast.makeText(this, "摇头角度不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int yaw = Integer.parseInt(yawStr);
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_YAW_LIMIT, yaw);
            DualFaceConfig.getDualFaceConfig().setYawDegree(yaw);
        } catch (Exception e) {
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_YAW_LIMIT, DualFaceConfig.getDualFaceConfig().getYawDegree());
        }

        String pitchStr = mPitchEt.getText().toString();
        if (TextUtils.isEmpty(pitchStr)) {
            Toast.makeText(this, "点头角度不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int pitch = Integer.parseInt(pitchStr);
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_PITCH_LIMIT, pitch);
            DualFaceConfig.getDualFaceConfig().setPitchDegree(pitch);
        } catch (Exception e) {
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_PITCH_LIMIT, DualFaceConfig.getDualFaceConfig().getPitchDegree());
        }

        String rollStr = mRollEt.getText().toString();
        if (TextUtils.isEmpty(rollStr)) {
            Toast.makeText(this, "摇头角度不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int roll = Integer.parseInt(rollStr);
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_ROLL_LIMIT, roll);
            DualFaceConfig.getDualFaceConfig().setRollDegree(roll);
        } catch (Exception e) {
            SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_ROLL_LIMIT, DualFaceConfig.getDualFaceConfig().getRollDegree());
        }

        switch (mScaleRadioGroup.getCheckedRadioButtonId()) {
            case R.id.scaleRadio0:
                DualFaceConfig.sPreviewScale = DualFaceConfig.PreviewScale.NONE;
                break;
            case R.id.scaleRadio1:
                DualFaceConfig.sPreviewScale = DualFaceConfig.PreviewScale.SCALE_4_3;
                break;
            case R.id.scaleRadio2:
                DualFaceConfig.sPreviewScale = DualFaceConfig.PreviewScale.SCALE_3_4;
                break;
        }
        SharedPreferenceUtil.getInstance().saveInt(DualFaceConfig.SP_PREVIEW_SCALE, DualFaceConfig.sPreviewScale.value());

        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, DualSysCameraActivity.class));
        finish();
    }
}
