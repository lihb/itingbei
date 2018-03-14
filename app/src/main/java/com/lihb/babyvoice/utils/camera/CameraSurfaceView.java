package com.lihb.babyvoice.utils.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.lihb.babyvoice.R;
import com.lihb.babyvoice.utils.CommonToast;
import com.lihb.babyvoice.utils.DimensionUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by caijw on 2015/3/27.
 */
public class CameraSurfaceView extends SurfaceView implements SensorEventListener, SurfaceHolder.Callback {
    private static final String TAG = "CameraSurfaceView";
    private static final float HIGH_MARK = 6.5f;                // 用于判断手机持向
    private static final float LOW_MARK = 2.5f;                 // 用于判断手机持向
    @SuppressWarnings("FieldCanBeLocal")
    private static float MOTIONLESS_ACC_IN_THRESHOLD = 0.35f;
    @SuppressWarnings("FieldCanBeLocal")
    private static float MOTIONLESS_ACC_OUT_THRESHOLD = 0.7f;
    @SuppressWarnings("FieldCanBeLocal")
    private static int MOTIONLESS_KEEP_TIME = 400;              // 静止此段时间后，自动对焦
    /**
     * 对焦成功的提示处理
     */
    Timer mFocusCompletedTimer = null;
    private SurfaceHolder mCameraSurfaceHolder;
    private boolean mIsTakingPicture;
    private Point mFocusPoint = new Point();
    private Point mPreviewPictureSize = new Point();
    private Point mFullPictureSize = new Point();
    private Rect mClipRect;
    private int mPreferredWidth = 9999;
    private int mPreferredHeight = 9999;
    private int mCurrentRotation = -1;
    private OnRotationListener mOnRotationListener;
    // 静止触发自动对焦
    private SensorManager mSensorManager;
    private float mLastX;
    private float mLastY;
    private float mLastZ;
    private long mLastMotionlessTime;
    private boolean mIsFocused;
    private Paint mFocusPaint;
    private Paint mGuidesPaint;
    private FocusState mFocusState = FocusState.FOCUS_READY;
    private CameraHelper cameraHelper = new CameraHelper();

    public CameraSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public static int calculateInSampleSize(final int rawWidth, final int rawHeight, final int reqWidth, final int reqHeight) {
        // Raw height and width of image
        int inSampleSize = 1;
        if (rawHeight > reqHeight || rawWidth > reqWidth) {

            final int halfHeight = rawHeight / 2;
            final int halfWidth = rawWidth / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            long totalPixels = rawWidth / inSampleSize * rawHeight / inSampleSize;

            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }

        return inSampleSize;
    }

    public boolean isFrontCamera() {
        return cameraHelper.isFrontCamera();
    }

    /**
     * 设置是否使用前置摄像头
     *
     * @param frontCamera 是否使用前置摄像头
     */
    public void setFrontCamera(boolean frontCamera) {
        cameraHelper.setFrontCamera(frontCamera);
    }

    public boolean isPortraitMode() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public Rect getClipRect() {
        return mClipRect;
    }

    /**
     * 设置CameraSurfaceView实际相机预览区域
     *
     * @param clipRect 实际预览区域
     */
    public void setClipRect(Rect clipRect) {
        mClipRect = clipRect;
    }

    /**
     * 设置最佳输出拍照尺寸
     * 须在开启相机之前调用
     *
     * @param preferredWidth
     * @param preferredHeight
     */
    public void setPreferredSize(int preferredWidth, int preferredHeight) {
        mPreferredWidth = preferredWidth;
        mPreferredHeight = preferredHeight;
    }

    @SuppressWarnings("deprecation")
    private void init() {
        float scale = getResources().getDisplayMetrics().density;

        mGuidesPaint = new Paint();
        mGuidesPaint.setStyle(Paint.Style.STROKE);
        mGuidesPaint.setColor(Color.parseColor("#BBFFFFFF"));
        mGuidesPaint.setStrokeWidth(1);

        mFocusPaint = new Paint();
        mFocusPaint.setStyle(Paint.Style.STROKE);
        mFocusPaint.setStrokeWidth(scale * 2f);

        mCameraSurfaceHolder = getHolder();
        mCameraSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // deprecated
        mCameraSurfaceHolder.addCallback(this);

        // 选取加速度感应器，用于自动对焦
        if (!isInEditMode()) {
            mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        }
    }

    public int getCurrentRotation() {
        return mCurrentRotation;
    }

    public void setCurrentRotation(int currentRotation) {
        if (mCurrentRotation != currentRotation) {
            Log.v(TAG, "rotate to " + currentRotation * 90);
            int oldRotation = mCurrentRotation;
            mCurrentRotation = currentRotation;
            if (mOnRotationListener != null) {
                mOnRotationListener.onRotate(currentRotation, oldRotation);
            }
        }
    }

    public boolean setFlashMode(CameraHelper.FlashMode mode) {
        return cameraHelper.setFlashMode(mode);
    }

    private void start() {
        if (cameraHelper.isCamera()) {
            return;
        }
        if (!initCamera()) {
            return;
        }
        startCamera();
    }

    /**
     * 拍照
     *
     * @param callback 拍照结果
     */
    public void takePicture(final TakePictureCallback callback) {

        start();

        if (!cameraHelper.isCamera()) {
            if (callback != null) {
                callback.onError(new Exception("Connect camera failed"));
            }
        } else {
            mIsTakingPicture = true;

            changeFocusState(FocusState.FOCUS_READY);   // 隐藏对焦框
            invalidate();

            Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
                @Override
                public void onShutter() {
                    // 不处理，但会产生快门音
                }
            };

            Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    camera.stopPreview();  // 冻结画面
                    if (callback != null) {
                        if (bytes != null) {
                            Bitmap output = processPictureData(bytes);
                            callback.onSuccess(output);
                        } else {
                            callback.onError(new Exception("onPictureTaken() returns null data"));
                        }
                    }
                }
            };
            final Camera camera = cameraHelper.getCamera();
            camera.takePicture(shutterCallback, null, jpegCallback);
        }
    }

    /**
     * 照片处理
     *
     * @param bytes
     */
    private Bitmap processPictureData(byte[] bytes) {
        // 获得图片原始尺寸，降低采样，提升性能，防止OOM
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;  // 照片数据，使用RGB_565足够，节约内存
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        Log.d(TAG, String.format("take picture step 1: picture original width = %d, height = %d", options.outWidth, options.outHeight));
        Point sampleSize = getPreferredSampleSize();
        options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, sampleSize.x, sampleSize.y);
        Log.d(TAG, String.format("sampleWidth = %d, sampleHeight = %d, inSampleSize = %d", sampleSize.x, sampleSize.y, options.inSampleSize));

        // 解码图片
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        //利用inBitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            addInBitmapOptions(options);
        }
        Bitmap source = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Log.d(TAG, "Put source bitmap into cameraLruCache bitmp is %s" + source.toString());
        CameraCacheHelper.getMemoryCache().put(timeStamp, source);
        Log.d(TAG, String.format("take picture step 2: picture output width = %d, height = %d", options.outWidth, options.outHeight));

        // 根据 ClipRect 裁剪输出
        Matrix outputMatrix = new Matrix();
        int outX, outY, outWidth, outHeight;
        if (!isFrontCamera()) {
            if (!isPortraitMode()) {
                // 后置横屏
                outputMatrix.postRotate(0);
                outX = mClipRect.left * source.getWidth() / getWidth();
                outY = mClipRect.top * source.getHeight() / getHeight();
                outWidth = mClipRect.width() * source.getWidth() / getWidth();
                outHeight = mClipRect.height() * source.getHeight() / getHeight();
            } else {
                // 后置竖屏
                outputMatrix.postRotate(90);
                outX = mClipRect.top * source.getWidth() / getHeight();
                outY = (getWidth() - mClipRect.left - mClipRect.width()) * source.getHeight() / getWidth();
                outWidth = mClipRect.height() * source.getWidth() / getHeight();
                outHeight = mClipRect.width() * source.getHeight() / getWidth();
            }
        } else {
            if (!isPortraitMode()) {
                // 前置横屏
                outputMatrix.postRotate(0);
                outX = (getWidth() - mClipRect.left - mClipRect.width()) * source.getWidth() / getWidth();
                outY = mClipRect.top * source.getHeight() / getHeight();
                outWidth = mClipRect.width() * source.getWidth() / getWidth();
                outHeight = mClipRect.height() * source.getHeight() / getHeight();
            } else {
                // 前置竖屏
                outputMatrix.postRotate(270);
                outX = (getHeight() - mClipRect.top - mClipRect.height()) * source.getWidth() / getHeight();
                outY = (getWidth() - mClipRect.left - mClipRect.width()) * source.getHeight() / getWidth();
                outWidth = mClipRect.height() * source.getWidth() / getHeight();
                outHeight = mClipRect.width() * source.getHeight() / getWidth();
            }
        }

        Log.v(TAG, String.format("createBitmap x=%d, y=%d, width=%d, height=%d, matrix=%s",
                outX, outY, outWidth, outHeight, outputMatrix.toString()));
        Bitmap output = Bitmap.createBitmap(source, outX, outY, outWidth, outHeight, outputMatrix, false);

        Log.d(TAG, "take picture step 3: on bitmap cropped");
        return output;
    }

    private Point getPreferredSampleSize() {
        long sampleWidth, sampleHeight;
        if (isPortraitMode()) {
            sampleWidth = mPreferredHeight * getHeight() / mClipRect.height();
            sampleHeight = mPreferredWidth * getWidth() / mClipRect.width();
        } else {
            sampleHeight = mPreferredHeight * getHeight() / mClipRect.height();
            sampleWidth = mPreferredWidth * getWidth() / mClipRect.width();
        }
        return new Point((int) sampleWidth, (int) sampleHeight);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mClipRect == null) {
            mClipRect = new Rect(0, 0, width, height);
        }
        Log.d(TAG, "surfaceChanged");
        start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");

        stopCamera();
    }

    /**
     * 点击画面触发自动对焦
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!cameraHelper.isCamera()) {
            return true;
        }

        if (event.getPointerCount() != 1) {
            Log.d(TAG, "multi touch");
            return true;
        }

        if (event.getAction() != MotionEvent.ACTION_UP) {
            Log.d(TAG, "ACTION -> " + event.getAction());
            return true;
        }

        try2Focus((int) event.getX(), (int) event.getY(), true);

        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float dx = x - mLastX;
        float dy = y - mLastY;
        float dz = z - mLastZ;
        mLastX = x;
        mLastY = y;
        mLastZ = z;
        double delta = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // 持握朝向判断
        int newRotation = mCurrentRotation;
        if (Math.abs(y) > HIGH_MARK && Math.abs(x) < LOW_MARK) {
            newRotation = y > 0 ? Surface.ROTATION_0 : Surface.ROTATION_180;
        } else if (Math.abs(x) > HIGH_MARK && Math.abs(y) < LOW_MARK) {
            // Landscape
            newRotation = x > 0 ? Surface.ROTATION_270 : Surface.ROTATION_90;
        }

        setCurrentRotation(newRotation);

        // 自动对焦处理
        long nowTime = System.currentTimeMillis();
        if (mFocusState == FocusState.FOCUS_READY && !mIsFocused && delta > MOTIONLESS_ACC_IN_THRESHOLD) {
            // 检测到摇晃，须要重新对焦
            mLastMotionlessTime = nowTime;
        } else if (mFocusState == FocusState.FOCUS_READY && mIsFocused && delta > MOTIONLESS_ACC_OUT_THRESHOLD) {
            // 处理同上，触发自动对焦后，为方便用户手动重新对焦，提高触发阀值
            Log.v(TAG, String.format("big shake detected = %f, reset focus!", delta));
            mIsFocused = false;
            mLastMotionlessTime = nowTime;
        } else if (!mIsTakingPicture && !mIsFocused && mLastMotionlessTime != 0 && nowTime - mLastMotionlessTime > MOTIONLESS_KEEP_TIME) {
            // 静止时间超过设定，自动触发中央对焦
            Log.v(TAG, String.format("delta = %f, auto refocus!", delta));
            try2Focus(mClipRect.centerX(), mClipRect.centerY(), false);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * 初始化摄像头
     *
     * @return
     */
    private boolean initCamera() {
        int ret = cameraHelper.init(mCameraSurfaceHolder);
        /*if (ret == CameraHelper.kCameraException) {
            EnvTestHelper.permissionWarning(getContext(), EnvTestHelper.kNoCamera);
        } else */
        if (ret == CameraHelper.kCameraIdInvalid) {
            CommonToast.showShortToast(R.string.camera_not_found);
        } else if (ret == 0) {
            setWillNotDraw(false); // 调用onDraw
            return true;
        }
        return false;
    }

    private boolean startCamera() {

        if (!cameraHelper.isCamera()) {
            return false;
        }
        // 注册重力感应，静止时自动对焦
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

        mIsTakingPicture = false;
        mIsFocused = false;

        setCameraDisplayOrientation();
        setBestPreviewSize();
        setBestPictureSize();
        setFlashMode(cameraHelper.getFlashMode());   // 回到相机界面时恢复之前闪关灯状态

        try {
            cameraHelper.start();

        } catch (Exception ex) {
            CommonToast.showShortToast(R.string.camera_start_preview_failed);
            Log.e(TAG, "startPreview failed", ex);

            return false;
        }

        return true;
    }

    private void stopCamera() {
        mSensorManager.unregisterListener(this);
        cameraHelper.release();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float scale = getResources().getDisplayMetrics().density;

        // 基准线
        if (mClipRect != null) {
            for (int i = 1; i <= 2; i++) {
                canvas.drawLine(mClipRect.left, mClipRect.top + i * mClipRect.height() / 3, mClipRect.right, mClipRect.top + i * mClipRect.height() / 3, mGuidesPaint);
            }

            for (int i = 1; i <= 2; i++) {
                canvas.drawLine(mClipRect.left + i * mClipRect.width() / 3, mClipRect.top, mClipRect.left + i * mClipRect.width() / 3, mClipRect.bottom, mGuidesPaint);
            }
        }

        // 对焦框
        if (!isFrontCamera() && cameraHelper.isCamera() && mFocusState != FocusState.FOCUS_READY) { // 前置摄像头不显示对焦框，因为一般不支持自动对焦
            int size = (int) (scale * 40f);
            if (mFocusState == FocusState.FOCUSING) {
                mFocusPaint.setColor(Color.WHITE);
            } else if (mFocusState == FocusState.FOCUS_COMPLETE) {
                mFocusPaint.setColor(Color.GREEN);
            } else if (mFocusState == FocusState.FOCUS_FAILED) {
                mFocusPaint.setColor(Color.RED);
            }
            canvas.drawRect(mFocusPoint.x - size, mFocusPoint.y - size, mFocusPoint.x + size, mFocusPoint.y + size, mFocusPaint);
        }

        super.onDraw(canvas);
    }

    private void setCameraDisplayOrientation() {
        cameraHelper.setDisplayOrientation(isPortraitMode() ? 90 : 0);
    }

    /**
     * 设置预览尺寸
     */
    private void setBestPreviewSize() {
        if (!cameraHelper.isCamera()) {
            return;
        }
        final Camera camera = cameraHelper.getCamera();
        try {
            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();

            Point screenSize = new Point();
            DimensionUtil.getScreenSize(getContext(), screenSize);
            float screenRatio = getRatio(screenSize.x, screenSize.y);

            float previewRatio = 0;
            Point previewSize = new Point();
            for (Camera.Size size : previewSizes) {
                float sizeRatio = getRatio(size.width, size.height);
                Log.v(TAG, String.format("Supported preview size > width = %d, height = %d, ratio = %f", size.width, size.height, sizeRatio));

                boolean accept = false;
                if (!almostEqual(previewRatio, screenRatio) && almostEqual(sizeRatio, screenRatio)) {
                    // 若找到与屏幕比例一致的配置，指定它
                    accept = true;
                } else if (!almostEqual(previewRatio, screenRatio) && size.width > previewSize.x) {
                    // 若比例不一致，则优先使用更高尺寸
                    accept = true;
                } else if (almostEqual(sizeRatio, screenRatio) && size.width > previewSize.x) {
                    // 若比例一致，尽可能找分辨率更高的
                    accept = true;
                }

                if (accept
                        && size.width <= Math.max(screenSize.x, screenSize.y)) { // 预览分辨率不用大于频率分辨率，小米Note上会导致无法输出照片
                    previewSize.set(size.width, size.height);
                    previewRatio = sizeRatio;
                }
            }

            mPreviewPictureSize.set(previewSize.x, previewSize.y);
            params.setPreviewSize(mPreviewPictureSize.x, mPreviewPictureSize.y);

            camera.setParameters(params);
        } catch (Exception ex) {
            Log.e(TAG, "setBestPreviewSize error:", ex);
        }
    }

    /**
     * 设置拍照尺寸
     */
    private void setBestPictureSize() {
        if (!cameraHelper.isCamera()) {
            return;
        }

        final Camera camera = cameraHelper.getCamera();
        try {
            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();
            float previewRatio = getRatio(mPreviewPictureSize.x, mPreviewPictureSize.y);

            // 根据相机配置，尽可能设置与预览尺寸一致的更高分辨率的配置
            float pictureRatio = 0;
            Point pictureSize = new Point(0, 0);
            for (Camera.Size size : supportedSizes) {
                float sizeRatio = getRatio(size.width, size.height);
                Log.v(TAG, String.format("Supported picture size > width = %d, height = %d, ratio = %f", size.width, size.height, sizeRatio));

                boolean accept = false;
                if (!almostEqual(pictureRatio, previewRatio) && almostEqual(sizeRatio, previewRatio)) {
                    // 若找到与预览比例一致的配置，指定它
                    accept = true;
                } else if (!almostEqual(pictureRatio, previewRatio) && size.width > pictureSize.x
                        && size.width < mPreferredWidth && size.height < mPreferredHeight) {
                    // 若比例不一致，则优先使用更高尺寸（但不必超过用户设定的最佳尺寸）
                    accept = true;
                } else if (almostEqual(sizeRatio, previewRatio) && size.width > pictureSize.x
                        && size.width < mPreferredWidth && size.height < mPreferredHeight) {
                    // 若比例一致，尽可能找分辨率更高的
                    accept = true;
                }

                if (accept) {
                    pictureSize.set(size.width, size.height);
                    pictureRatio = sizeRatio;
                }
            }

            mFullPictureSize = pictureSize;
            params.setPictureSize(mFullPictureSize.x, mFullPictureSize.y);

            camera.setParameters(params);
        } catch (Exception ex) {
            Log.e(TAG, "setBestPictureSize error:", ex);
        }
    }

    /**
     * 返回两个比例是否近似一致
     *
     * @param a
     * @param b
     * @return
     */
    private boolean almostEqual(float a, float b) {
        return Math.abs(a - b) <= 0.001f;
    }

    private ArrayList<Camera.Area> getCameraAreasFromPreview(float x, float y) {
        Matrix camera2prevMatrix = new Matrix();
        Log.v(TAG, "camera2prevMatrix reset" + camera2prevMatrix.toString());
        camera2prevMatrix.postRotate(0);
        Log.v(TAG, "camera2prevMatrix postRotate(0)" + camera2prevMatrix.toString());
        camera2prevMatrix.postScale(getWidth() / 2000f, getHeight() / 2000f);
        Log.v(TAG, String.format("camera2prevMatrix postScale(%f, %f) = %s", getWidth() / 2000f, getHeight() / 2000f, camera2prevMatrix.toString()));
        camera2prevMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
        Log.v(TAG, String.format("camera2prevMatrix postTranslate(%f, %f) = %s", getWidth() / 2f, getHeight() / 2f, camera2prevMatrix.toString()));

        Matrix preview2cameraMatrix = new Matrix();
        if (!camera2prevMatrix.invert(preview2cameraMatrix)) {
            Log.w(TAG, "failed to invert matrix !");
        }
        Log.v(TAG, "preview2cameraMatrix " + preview2cameraMatrix.toString());

        float[] coords = {x, y};
        Log.v(TAG, "x => " + coords[0] + ", y => " + coords[1]);
        preview2cameraMatrix.mapPoints(coords);
        Log.v(TAG, "cx => " + coords[0] + ", cy => " + coords[1]);

        Rect rect = new Rect();
        rect.left = (int) coords[0] - 50;
        rect.right = (int) coords[0] + 50;
        rect.top = (int) coords[1] - +50;
        rect.bottom = (int) coords[1] + 50;

        final ArrayList<Camera.Area> areas = new ArrayList<>(1);
        areas.add(new Camera.Area(rect, 1000));

        return areas;
    }

    /**
     * 尝试对指定位置进行自动对焦
     * 前置摄像头一般不支持自动对焦，但可能会触发自动对光
     *
     * @param x
     * @param y
     * @param byTouch 是否由用户主动点击触发，是的话将播放对焦音
     */
    private void try2Focus(int x, int y, final boolean byTouch) {
        mIsFocused = true;
        if (cameraHelper.isCamera()) {
            Log.d(TAG, String.format("try to focus: x = %d, y = %d", x, y));

            final Camera camera = cameraHelper.getCamera();
            try {
                Camera.Parameters parameters = camera.getParameters();
                String focusMode = parameters.getFocusMode();
                Log.d(TAG, "FocusMode -> " + focusMode);

                mFocusPoint.set(x, y);
                changeFocusState(FocusState.FOCUSING);

                final ArrayList<Camera.Area> focusAreas = getCameraAreasFromPreview(x, y);
                parameters.setFocusAreas(focusAreas);
                if (parameters.getMaxNumMeteringAreas() != 0) { // also set metering areas
                    parameters.setMeteringAreas(focusAreas);
                }
                camera.setParameters(parameters);
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Log.println(success ? Log.INFO : Log.WARN, TAG, "auto focus complete: success -> " + success);
                        changeFocusState(success ? FocusState.FOCUS_COMPLETE : FocusState.FOCUS_FAILED);
                    }
                });
            } catch (Exception e) {// 手机剧烈晃动时可能导致
                Log.e(TAG, "auto focus error", e);
                mIsFocused = false;
                changeFocusState(FocusState.FOCUS_READY);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseFocusCompletedTimer();
    }

    private void changeFocusState(FocusState state) {
        releaseFocusCompletedTimer();

        mFocusState = state;
        invalidate();

        if (state == FocusState.FOCUS_COMPLETE || state == FocusState.FOCUS_FAILED) {
            mFocusCompletedTimer = new Timer();
            mFocusCompletedTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // 对焦成功一定时间后，隐藏对焦框
                    mFocusState = FocusState.FOCUS_READY;
                    postInvalidate();
                    Log.v(TAG, "Focus ready!");
                }
            }, 800);
        }
    }

    private void releaseFocusCompletedTimer() {
        if (mFocusCompletedTimer != null) {
            mFocusCompletedTimer.cancel();
            mFocusCompletedTimer.purge();
            mFocusCompletedTimer = null;
        }
    }

    private float getRatio(int sideA, int sideB) {
        return sideA > sideB ? (float) sideA / sideB : (float) sideB / sideA;
    }

    public void setOnRotationListener(OnRotationListener onRotationListener) {
        mOnRotationListener = onRotationListener;
    }

    /**
     * 将改option采用inBitmap处理
     *
     * @param options
     */
    private void addInBitmapOptions(BitmapFactory.Options options) {
        options.inMutable = true;
        Bitmap bitmap = CameraCacheHelper.get().getBitmapFromReuseableSet(options);
        if (bitmap != null) {
            Log.d(TAG, String.format("getBitmapFromReuseableSet != null"));
            options.inBitmap = bitmap;
        } else {
            Log.d(TAG, String.format("getBitmapFromReuseableSet == null"));
        }
    }

    enum FocusState {
        FOCUS_READY, FOCUSING, FOCUS_COMPLETE, FOCUS_FAILED
    }


    public interface TakePictureCallback {
        void onSuccess(Bitmap bitmap);

        void onError(Exception e);
    }

    public interface OnRotationListener {
        void onRotate(int newRotation, int oldRotation);
    }
}

