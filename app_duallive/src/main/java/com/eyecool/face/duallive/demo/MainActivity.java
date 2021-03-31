package com.eyecool.face.duallive.demo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecool.face.api.FaceApi;
import com.eyecool.face.api.FaceLiveApi;
import com.eyecool.utils.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String APP_ID_NAME = "eyecool_face_appid";
    private TextView mHintTv;
    private EditText mAppIdEt;
    int index = 0;
    private ProgressDialog mProgressDialog;
    private int REQUEST_PERMISSIONS_CODE = 11;
    private String mAppId;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        SharedPreferenceUtil.getInstance().init(getApplicationContext());
        mAppId = SharedPreferenceUtil.getInstance().getString(APP_ID_NAME);

        findViewById(R.id.startSysbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, DualSysCameraActivity.class));
            }
        });


        findViewById(R.id.grantBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initFaceSDK();
            }
        });


        mHintTv = findViewById(R.id.hintTv);
        mAppIdEt = findViewById(R.id.appIdEdit);

        mAppIdEt.setText(mAppId);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.text_initialization));
        mProgressDialog.setCancelable(false);

        requestPermissions();
    }

    /**
     * 初始化人脸算法
     */
    private void initFaceSDK() {
        mAppId = mAppIdEt.getText().toString();

        mProgressDialog.show();

        /**
         * 设置绑定设备ID
         * 默认使用： UniqueType.ANDROID_ID
         * FaceConfig.setUniqueType(UniqueType.MAC);
         */
        // 初始化人脸检活
        FaceLiveApi faceLiveApi = FaceLiveApi.getInstance();
        faceLiveApi.init(getApplicationContext(), mAppId, new FaceLiveApi.FaceLiveCallback() {
            @Override
            public void onSuccess() {
                mHintTv.setText(R.string.text_activation_success);
                SharedPreferenceUtil.getInstance().saveString(APP_ID_NAME, mAppId);
                mProgressDialog.dismiss();
                initFaceApi();
            }

            @Override
            public void onError(int errorCode, String message) {
                mHintTv.setText(getString(R.string.text_activation_fails) + " errorCode:" + errorCode);
                mProgressDialog.dismiss();
            }
        });
    }

    private void initFaceApi() {
        // 初始化人脸检测
        FaceApi.getInstance().init(getApplicationContext(), mAppId, new FaceApi.FaceCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode, String message) {
                Toast.makeText(mContext, "FaceApi初始化错误：" + errorCode, Toast.LENGTH_SHORT).show();
                mHintTv.append("\nFaceApi初始化错误：" + errorCode);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
