package com.eyecool.face.detect.demo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecool.face.api.FaceApi;
import com.eyecool.utils.Logs;
import com.eyecool.utils.SharedPreferenceUtil;
import com.juhuiwangluo.testmerge.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 人脸检测1
 */
public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSIONS_CODE = 11;
    private static final String APP_ID_NAME = "eyecool_face_appid";

    private TextView mHintTv;
    private EditText mAppIdEt;
    private ProgressDialog mProgressDialog;

    private String mAppId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_detect);

        SharedPreferenceUtil.getInstance().init(getApplicationContext());
        mAppId = SharedPreferenceUtil.getInstance().getString(APP_ID_NAME);

        mHintTv = findViewById(R.id.hintTv);
        mAppIdEt = findViewById(R.id.appIdEdit);
        findViewById(R.id.startBtn).setOnClickListener(view -> startDetectCamera());
        findViewById(R.id.detectJpgBtn).setOnClickListener(view -> startDetectJpg());
        findViewById(R.id.grantBtn).setOnClickListener(v -> initFaceSDK());

        mAppIdEt.setText(mAppId);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.text_initialization));
        mProgressDialog.setCancelable(false);

        // 显示调试日志
        Logs.setsIsLogEnabled(true);

        requestPermissions();
    }

    /**
     * 初始化人脸算法
     */
    private void initFaceSDK() {
        SharedPreferenceUtil.getInstance().init(getApplicationContext());
        mAppId = mAppIdEt.getText().toString();

        mProgressDialog.show();

        /**
         * 设置绑定设备ID
         * 默认使用： UniqueType.ANDROID_ID
         * FaceConfig.setUniqueType(UniqueType.MAC);
         */
        // 设置最大检测人脸数量
        FaceApi.getInstance().setDetectMaxFaceNum(1);
        // 设置最大检测人脸尺寸(单位：px)
        FaceApi.getInstance().setDetectMinFaceSize(80, 80);
        FaceApi.getInstance().init(getApplicationContext(), mAppId, new FaceApi.FaceCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "初始化算法成功", Toast.LENGTH_SHORT).show();
                mHintTv.setText("初始化算法成功");
                SharedPreferenceUtil.getInstance().saveString(APP_ID_NAME, mAppId);
                mProgressDialog.dismiss();
            }

            @Override
            public void onError(int errorCode, String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                mHintTv.setText(message + " errorCode:" + errorCode);
                mProgressDialog.dismiss();
            }
        });
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = null;
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissions = new ArrayList<>();
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (permissions == null) {
                    permissions = new ArrayList<>();
                }
                permissions.add(Manifest.permission.CAMERA);
            }
            if (permissions == null) {
                initFaceSDK();
            } else {
                String[] permissionArray = new String[permissions.size()];
                permissions.toArray(permissionArray);
                // Request the permission. The result will be received
                // in onRequestPermissionResult()
                requestPermissions(permissionArray, REQUEST_PERMISSIONS_CODE);
            }
        } else {
            initFaceSDK();
        }
    }


    private void startDetectJpg() {
        startActivity(new Intent(this, DetectJpgActivity.class));
    }

    private void startDetectCamera() {
        Intent intent = new Intent(this, FaceTestActivity.class);
        startActivity(intent);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            // Request for WRITE_EXTERNAL_STORAGE permission.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initFaceSDK();
            } else {
                // Permission request was denied.
                Toast.makeText(this, R.string.txt_error_permission, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
