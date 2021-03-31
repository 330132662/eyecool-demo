package com.eyecool.face.detect.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.eyecool.face.api.FaceApi;
import com.eyecool.face.model.FaceInfo;
import com.eyecool.utils.FileUtils;
import com.eyecool.utils.Logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 检测本地jpg图片
 * created by xiaozhi 2018/5/23
 */
public class DetectJpgActivity extends AppCompatActivity {

    private static final String TAG = DetectJpgActivity.class.getSimpleName();

    ImageView mImageView;
    TextView mHintTv;

    Bitmap mBitmap;

    String mFaceImgPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "faceDetectImg.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_jpg);

        mImageView = findViewById(R.id.faceIv);
        mHintTv = findViewById(R.id.hintTv);

        copyAssetsFileToSD();

        byte[] jpgBytes = FileUtils.readFile(mFaceImgPath);
        long start = System.currentTimeMillis();
        mBitmap = BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.length);
        Logs.i(TAG, "转Bitmap耗时：" + (System.currentTimeMillis() - start) + "ms");
        mImageView.setImageBitmap(mBitmap);

        new DetectTask().execute(jpgBytes);
    }

    private class DetectTask extends AsyncTask<byte[], Void, FaceInfo> {

        @Override
        protected FaceInfo doInBackground(byte[]... bytes) {
            // 检测jpg中是否存在人脸
            byte[] rgb24 = FaceApi.getInstance().jpgToRgb24(bytes[0], mBitmap.getWidth(), mBitmap.getHeight());
            FaceInfo faceInfo = FaceApi.getInstance().detectByRGB24(rgb24, mBitmap.getWidth(), mBitmap.getHeight());

            return faceInfo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(FaceInfo faceInfo) {
            super.onPostExecute(faceInfo);
            if (faceInfo.isHasFaces()) {
                int[] faceRect = faceInfo.getFaceList().get(0).getFaceRect();
                int yaw = faceInfo.getFaceList().get(0).getYawDegree();
                int pitch = faceInfo.getFaceList().get(0).getPitchDegree();
                int roll = faceInfo.getFaceList().get(0).getRollDegree();
                Logs.i(TAG, "hasFace:" + faceInfo.isHasFaces());
                drawFaceRect(mBitmap, faceRect);

                StringBuffer hintText = new StringBuffer();
                hintText.append(getString(R.string.text_is_has_face) + "：" + faceInfo.isHasFaces() + "\n");
                hintText.append(getString(R.string.text_face_coordinates) + "：{x:" + faceRect[0] + ", y:" + faceRect[1] + ", w:" + faceRect[2] + ", h:" + faceRect[3] + "}\n");
                hintText.append("姿态：" + "{yaw: " + yaw + ", pitch: " + pitch + ", roll: " + roll + "}");

                mHintTv.setText(hintText.toString());
            } else {
                mHintTv.setText(R.string.text_no_face_detected);
            }
        }
    }

    /**
     * 画出人脸矩形框
     *
     * @param bitmap
     * @param faceRect
     */
    private void drawFaceRect(Bitmap bitmap, int[] faceRect) {
        Bitmap tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(tempBitmap);

        // 画出人脸位置
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#0080ff"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        canvas.drawRect(faceRect[0], faceRect[1], faceRect[0] + faceRect[2], faceRect[1] + faceRect[3], paint);
        mImageView.setImageBitmap(tempBitmap);
    }

    /**
     * 将assets中的文件拷贝到SD卡下
     */
    private void copyAssetsFileToSD() {
        File file = new File(mFaceImgPath);
        if (!file.exists()) {
            try {
                InputStream is = getAssets()
                        .open("face.jpg");
                FileOutputStream fos = new FileOutputStream(mFaceImgPath);
                byte[] buffer = new byte[1024];
                int count;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.flush();
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
