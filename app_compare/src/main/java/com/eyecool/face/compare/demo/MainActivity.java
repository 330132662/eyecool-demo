package com.eyecool.face.compare.demo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecool.face.api.FaceRecognize;
import com.eyecool.face.api.interfaces.FaceInterface.FaceException;
import com.eyecool.face.compare.demo.utils.AppUtil;
import com.eyecool.face.compare.demo.utils.MeasureUtil;
import com.eyecool.face.compare.demo.utils.MyToast;
import com.eyecool.utils.FileUtils;
import com.eyecool.utils.ImageTools;
import com.eyecool.utils.Logs;
import com.eyecool.utils.SharedPreferenceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // 常量定义
    public static final int REQUEST_TAKE_A_PICTURE = 10;
    public static final int REQUEST_SELECET_A_PICTURE_ALL = 20;
    public static final int REQUEST_PERMISSIONS_CODE = 11;
    // 保存图片本地路径
    private static final String TEMP_TAKE_PHOTO_FILE_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
    private static final String APP_ID_NAME = "eyecool_face_appid";
    private final String TAG = this.getClass().getName();
    int mFaceNum;
    // 保存算法句柄，保存内容为指针，使用时作为传参，此处为2线程
    long[] multithreadSupport = new long[1];
    ProgressDialog mProgressDialog;
    private String mAlbumPicturePath = null;
    private String tempImgPath;
    private Context mContext;
    private LinearLayout mContentLayout;
    private Button mHintBtn;
    private ImageView mFaceImageView1;
    private ImageView mFaceImageView2;
    private TextView mHintTv;
    private TextView mVersionTv;
    private EditText mAppIdEdit;
    private byte[] mFeature1;
    private byte[] mFeatuer2;
    private String mAppId;
    private MyToast myToast;
    // 算法SDK实例
    private FaceRecognize mFaceRecognize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferenceUtil.getInstance().init(getApplicationContext());
        mAppId = SharedPreferenceUtil.getInstance().getString(APP_ID_NAME);

        Logs.setsIsLogEnabled(true);
        mContext = this;
        mFaceRecognize = FaceRecognize.getInstance();
        myToast = new MyToast(mContext);

        // 设置最大支持线程数
        FaceRecognize.getInstance().setThreadMaxNum(2);

        initViews();

        requestPermissions();
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

    private void initViews() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.text_init));
        mProgressDialog.setCancelable(false);
        int[] screenSize = MeasureUtil.getScreenSize(this);
        Logs.i(TAG, "width:" + screenSize[0]);
        Logs.i(TAG, "height:" + screenSize[1]);

        mContentLayout = findViewById(R.id.contentLayout);
        mFaceImageView1 = findViewById(R.id.face1);
        mFaceImageView2 = findViewById(R.id.face2);
        mHintBtn = findViewById(R.id.hintBtn);
        mHintTv = findViewById(R.id.hintTv);
        mVersionTv = findViewById(R.id.versionTv);
        mVersionTv.setText("ssnow:" + mFaceRecognize.getVersion());
        mAppIdEdit = findViewById(R.id.appIdEdit);

        mAppIdEdit.setText(mAppId);
        findViewById(R.id.matchBtn).setOnClickListener(v -> matchBtnClick());
        mHintBtn.setOnClickListener(v -> hintBtnClick());
        mFaceImageView1.setOnClickListener(v -> faceBtn1Click());
        mFaceImageView2.setOnClickListener(v -> faceBtn2Click());

        if (screenSize[0] > screenSize[1]) {
            ViewGroup.LayoutParams lp = mContentLayout.getLayoutParams();
            lp.width = screenSize[0] / 3;
            mContentLayout.setLayoutParams(lp);
        }
        mFaceImageView1.post(() -> {
            LayoutParams lp1 = (LayoutParams) mFaceImageView1
                    .getLayoutParams();
            LayoutParams lp2 = (LayoutParams) mFaceImageView2
                    .getLayoutParams();
            lp1.height = mFaceImageView1.getWidth();
            lp2.height = mFaceImageView2.getWidth();
            mFaceImageView1.setLayoutParams(lp1);
            mFaceImageView2.setLayoutParams(lp2);
        });
    }

    /**
     * 初始化人脸算法
     */
    private void initFaceSDK() {
        mAppId = mAppIdEdit.getText().toString();
        if (TextUtils.isEmpty(mAppId)) {
            Toast.makeText(MainActivity.this, "Please input appid", Toast.LENGTH_SHORT).show();
            return;
        }

        /**
         * 设置绑定设备ID
         * 默认使用： UniqueType.ANDROID_ID
         * FaceConfig.setUniqueType(UniqueType.MAC);
         */
        mProgressDialog.show();
        // 第一次开启应用时尝试激活并初始化
        mFaceRecognize.init(mContext, mAppId, multithreadSupport, new FaceRecognize.FaceCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(mContext, R.string.text_activation_success, Toast.LENGTH_LONG).show();
                mHintTv.setText(R.string.text_activated);
                SharedPreferenceUtil.getInstance().saveString(APP_ID_NAME, mAppId);
                mProgressDialog.dismiss();
            }

            @Override
            public void onError(int code, String message) {
                mHintTv.setText("error:" + code + " " + message);
                mProgressDialog.dismiss();
            }
        });
    }

    protected void hintBtnClick() {
        initFaceSDK();
    }

    protected void faceBtn1Click() {
        showOptionDialog(1);
    }

    protected void faceBtn2Click() {
        showOptionDialog(2);
    }

    protected void matchBtnClick() {
        if (mFeature1 == null || mFeatuer2 == null) {
            Toast.makeText(mContext, R.string.text_select_face_img, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int score = mFaceRecognize.compareFeature(mFeature1, mFeatuer2);
            mHintTv.setText(getString(R.string.text_compare_score) + score);
        } catch (FaceException e) {
            if (e.getMessage().equals("-31"))
                mHintTv.setText(R.string.text_not_active);
            else
                mHintBtn.setText("error：" + e.getMessage());
        }
    }

    private void showOptionDialog(final int num) {
        new AlertDialog.Builder(this).setItems(
                R.array.array_photo_source,
                (dialog, which) -> {
                    if (which == 0)
                        selectImageForAll(REQUEST_SELECET_A_PICTURE_ALL, num);
                    else
                        selectImageForAll(REQUEST_TAKE_A_PICTURE, num);
                }).show();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // 长时间切入后台请重新初始化
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logs.i(TAG, "销毁资源占用");
        for (long thread : multithreadSupport) {
            mFaceRecognize.deInit(thread);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int num = 0;
        num = requestCode % 10;
        requestCode = requestCode - num;
        Log.e("onActivityResult", "num = " + num
                + ", requestCode = " + requestCode);

        mFaceNum = num;

        if (requestCode == REQUEST_SELECET_A_PICTURE_ALL && resultCode == RESULT_OK) {
            if (data == null) {
                Log.e("onActivityResult", "返回data为空");
                return;
            }
            Uri uri = data.getData();
            mAlbumPicturePath = AppUtil.getPath(mContext, uri);
            Logs.i(TAG, "PicturePath = " + mAlbumPicturePath);

            new FeatureTask().execute(mAlbumPicturePath);
            if (num == 1) {
                mFaceImageView1.setImageBitmap(AppUtil.bitmapFactorySmall(
                        Uri.fromFile(new File(mAlbumPicturePath)), 400));
            } else {
                mFaceImageView2.setImageBitmap(AppUtil.bitmapFactorySmall(
                        Uri.fromFile(new File(mAlbumPicturePath)), 400));
            }
        } else if (requestCode == REQUEST_TAKE_A_PICTURE && resultCode == RESULT_OK) {
            new FeatureTask().execute(tempImgPath);
            if (num == 1) {
                mFaceImageView1.setImageBitmap(AppUtil.bitmapFactorySmall(tempImgPath,
                        400));
            } else if (num == 2) {
                mFaceImageView2.setImageBitmap(AppUtil.bitmapFactorySmall(tempImgPath,
                        400));
            }
        } else if (resultCode == RESULT_CANCELED) {
            myToast.showToast(R.string.text_cancel_head_image_settings);
        }
    }

    /**
     * <br>
     * 功能简述:兼容性裁剪图片方法实现---------------------- 相册 <br>
     * 功能详细描述: <br>
     * 注意:
     */
    private void selectImageForAll(int getPicTpye, int num) {
        if (getPicTpye == REQUEST_SELECET_A_PICTURE_ALL) {
            Intent intent = new Intent(Intent.ACTION_PICK); // ACTION_PICK可以兼容大多数版本
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "image/*");
            startActivityForResult(intent, REQUEST_SELECET_A_PICTURE_ALL + num);
        } else if (getPicTpye == REQUEST_TAKE_A_PICTURE) {
            PackageManager packageManager = getPackageManager();

            // if device support camera?
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                // yes
                Log.i("camera", "This device has camera!");
                take(num);
            } else {
                // no
                Log.i("camera", "This device has no camera!");
                Toast.makeText(this, "This device has no camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 调用照相机
     */
    public void take(int num) {
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File myImageDir = new File(TEMP_TAKE_PHOTO_FILE_PATH + "");
        // 创建图片保存目录
        if (!myImageDir.exists()) {
            myImageDir.mkdirs();
        }
        // 根据时间来命名
        File imageFile = null;
        imageFile = new File(TEMP_TAKE_PHOTO_FILE_PATH + "/crop.jpg");
        Log.e(TAG, "imageFile=" + imageFile);
        tempImgPath = imageFile.getPath();
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", imageFile);
        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(takeIntent, REQUEST_TAKE_A_PICTURE + num);
    }

    private class FeatureTask extends AsyncTask<String, Void, byte[]> {

        @Override
        protected byte[] doInBackground(String... params) {
            String path = params[0];
            Logs.i(TAG, "path = " + path);
            Bitmap bitmap = AppUtil.bitmapFactorySmall(path);
            bitmap = ImageTools.rotateBitmap(bitmap, path);
            byte[] feature = null;
            try {
                feature = mFaceRecognize.getFaceFeatureBinByBitmap(bitmap);
            } catch (FaceException e) {
                e.printStackTrace();
                if (e.getMessage().equals("-31"))
                    myToast.showToast(R.string.text_not_active);
                feature = null;
            }

            return feature;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            myToast.showToast(R.string.text_get_feature);
        }

        @Override
        protected void onPostExecute(byte[] result) {
            super.onPostExecute(result);

            if (result == null) {
                myToast.showToast(R.string.text_failure_to_get_feature);
                return;
            }
            myToast.showToast(R.string.text_successful_to_get_feature);
            Log.i(TAG, "特征：" + result);
            FileUtils.writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/face_fea.txt", Base64.encodeToString(result, Base64.NO_WRAP));
            if (mFaceNum == 1) {
                mFeature1 = result;
            } else if (mFaceNum == 2) {
                mFeatuer2 = result;
            }
        }
    }
}
