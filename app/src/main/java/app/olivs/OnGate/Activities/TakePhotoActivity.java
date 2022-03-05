package app.olivs.OnGate.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import app.olivs.OnGate.App.Config;
import app.olivs.OnGate.App.Services;
import app.olivs.OnGate.Classes.ActivityState;
import app.olivs.OnGate.Classes.CheckInRecord;
import app.olivs.OnGate.Databases.DatabaseAccess;
import app.olivs.OnGate.R;
import app.olivs.OnGate.Utils.NetworkUtil;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static app.olivs.OnGate.App.Services.ServiceRequest;
import static app.olivs.OnGate.App.Services.AUTHENTICATION_TOKEN_KEY;

public class TakePhotoActivity extends AppCompatActivity implements View.OnClickListener, TextureView.SurfaceTextureListener, Response.Listener<JSONObject>, Response.ErrorListener {

    private ConstraintLayout background;
    private TextView welcome/*, eventIDLabel*/;
    private Button notYou;
    private int userID;
    private String userFullName;
    private Button[] controlButtons;

    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private ActivityState state;
    private String actionDateTime;

    private String cameraId;
    private CameraCaptureSession captureSession;
    private CameraDevice cameraDevice;
    private Size previewSize;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private ImageReader imageReader;
    private CaptureRequest.Builder previewRequestBuilder;
    private CaptureRequest previewRequest;
    private Semaphore cameraOpenCloseLock = new Semaphore(1);
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private Bitmap tempImage;

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // This method is called when the camera is opened.  We start camera preview here.
            cameraOpenCloseLock.release();
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraOpenCloseLock.release();
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraOpenCloseLock.release();
            camera.close();
            cameraDevice = null;
            finish();
        }

    };


    DatabaseAccess databaseAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        background = findViewById(R.id.background);
        welcome = findViewById(R.id.welcome);
        //eventIDLabel = findViewById(R.id.eventID);
        notYou = findViewById(R.id.notYou);
        textureView = findViewById(R.id.textureView);
        controlButtons = new Button[4];
        for (int i = 0; i < ActivityState.values().length; i++) {
            int id = getResources().getIdentifier(ActivityState.values()[i].name(), "id", getPackageName());
            controlButtons[i] = findViewById(id);
            controlButtons[i].setOnClickListener(this);
        }
        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        Intent intent = getIntent();
        userID = intent.getIntExtra("UserID",0);
        if(userID>0){
            //eventIDLabel.setVisibility(View.GONE);
            userFullName = intent.getStringExtra("UserFullName");
            welcome.setText(String.format("Welcome, %s\nClick the applicable button to proceed.", userFullName));
            notYou.setVisibility(View.VISIBLE);
            notYou.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeCamera();
                    finish();
                }
            });
        }
        else{
            userFullName = "";
            notYou.setVisibility(View.GONE);
            String messageText = userID == 0 ? getResources().getString(R.string.could_not_find_pin) :
                    getResources().getString(R.string.could_not_connect_to_check_in);
            messageText += "\n";
            String eventID = databaseAccess.queryDatabase("EventID");
            messageText += eventID;
            welcome.setText(messageText);
            //eventIDLabel.setText(eventID);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(this);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }


    @Override
    public void onClick(View view) {
        //eventIDLabel.setVisibility(View.GONE);
        notYou.setVisibility(View.GONE);
        for (Button button: controlButtons)
            button.setEnabled(false);
        welcome.setText("Preparing photo...");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        actionDateTime = dateFormat.format(Calendar.getInstance().getTime());
        final String activityType = view.getResources().getResourceEntryName(view.getId());
        state = ActivityState.valueOf(activityType);
        takePicture();
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        openCamera(i, i1);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        configureTransform(i, i1);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }

    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    protected void takePicture() {

        if (cameraDevice == null) {
            showThankYou(false);
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            ImageReader reader = ImageReader.newInstance(previewSize.getHeight(), previewSize.getWidth(), ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            //captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(-270));
            // CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics("" + cameraDevice.getId());
            rotation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                    bitmapImage = getResizedBitmap(bitmapImage, previewSize.getHeight(), previewSize.getWidth());
                    checkIn(bitmapImage);
                    closeCamera();
                    image.close();
                }

            };

            reader.setOnImageAvailableListener(readerListener, backgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroundHandler);
                    } catch (CameraAccessException e) {
                        showThankYou(false);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            showThankYou(false);
        }
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics cameraCharacteristics = null;
        try {
            cameraCharacteristics = manager.getCameraCharacteristics("" + cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
    }

    private void checkIn(Bitmap bitmapImage) {
        if (userID>0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    welcome.setText("Uploading data...");
                }
            });
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 35, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String imageInBase64 = "base64," + Base64.encodeToString(imageBytes, Base64.DEFAULT);
            JSONObject body = new JSONObject();
            String token = databaseAccess.getAuthenticationToken();
            try {
                body.put(AUTHENTICATION_TOKEN_KEY, token);
                body.put("ActionDateTime", actionDateTime);
                body.put("UserID", userID);
                body.put("PhotoBase64", imageInBase64);
                body.put("CheckType", state);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final ServiceRequest request = new ServiceRequest(this, Request.Method.POST, Services.getServiceURL(3), body, this, this);
            if (NetworkUtil.getConnectivityStatus(this) == NetworkUtil.TYPE_NOT_CONNECTED)
                saveImage(bitmapImage);
            else
                tempImage = bitmapImage;
                Volley.newRequestQueue(this).add(request);
        }
        else{
            saveImage(bitmapImage);
        }
    }

    private void saveImage(Bitmap bitmapImage){
        int eventID = Integer.parseInt(databaseAccess.queryDatabase("EventID"));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String imageInBase64 = "base64," + Base64.encodeToString(imageBytes, Base64.DEFAULT);


        CheckInRecord record = new CheckInRecord(userID, eventID, actionDateTime, imageInBase64, false, state);
        databaseAccess.saveRecord(record);
        showThankYou(false);


        //File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Config.IMAGE_DIRECTORY_NAME);
        /*File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), Config.IMAGE_DIRECTORY_NAME);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                databaseAccess.saveRecord(record);
                showThankYou(false);
                return;
            }
        }
        String timestamp = actionDateTime.replace(' ', '_')
                .replace('/','_')
                .replace(':','_');
        File photoFile = new File(directory, timestamp + ".jpg");
        String path = photoFile.getAbsolutePath();
        record.setPhotoURL(path);
        if (!photoFile.exists()) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(photoFile);
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                databaseAccess.saveRecord(record);
            } catch (IOException e) {
                e.printStackTrace();
            }
        showThankYou(false);
        }*/
    }

    private void requestCameraPermission() {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder
                    = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (cameraDevice == null) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            captureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Finally, we start displaying the camera preview.
                                previewRequest = previewRequestBuilder.build();
                                captureSession.setRepeatingRequest(previewRequest,
                                        null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {


                    StreamConfigurationMap map = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map == null) {
                        continue;
                    }
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int screenHeight = displayMetrics.heightPixels;
                    int screenWidth = displayMetrics.widthPixels;
                    imageReader = ImageReader.newInstance(screenWidth, screenHeight,
                            ImageFormat.JPEG,2);
                    imageReader.setOnImageAvailableListener(
                            null, backgroundHandler);
                    previewSize = getPreviewSize(characteristics);
                    ConstraintSet set = new ConstraintSet();
                    set.clone(background);
                    set.setDimensionRatio(R.id.textureView, "H,"+getAspectRatio(previewSize));
                    set.applyTo(background);
                    this.cameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private Size getPreviewSize(CameraCharacteristics characteristics){
        Size[] allCameraSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                .getOutputSizes(ImageFormat.JPEG);
        String resolution = databaseAccess.queryDatabase("PhotoResolution");
        String[] items = resolution.split("x");
        if (items.length == 1)
        return new Size(1024,768);
        else {
            try{
                int a = Integer.parseInt(items[0]);
                int b = Integer.parseInt(items[1]);
                return new Size(Math.max(a,b),Math.min(a,b));
            }
            catch(NumberFormatException e) {
                return new Size(1024,768);
            }
        }
    }

    private String getAspectRatio(Size size){
        double width = (double) size.getWidth();
        double height = (double) size.getHeight();
        double ratio = (double) size.getWidth()/(double) size.getHeight();
        double bestDelta = Double.MAX_VALUE;
        int i = 1;
        int j = 1;
        int bestI = 0;
        int bestJ = 0;

        for (int iterations = 0; iterations < 100; iterations++) {
            double delta = (double) i / (double) j - ratio;
            if (delta < 0) i++;
            else j++;

            double newDelta = Math.abs((double) i / (double) j - ratio);
            if (newDelta < bestDelta) {
                bestDelta = newDelta;
                bestI = i;
                bestJ = j;
            }
        }
        System.out.println(bestI + ":" + bestJ);
        return bestI + ":" + bestJ;
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == textureView || null == previewSize) {
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics cameraCharacteristics = null;
        try {
            cameraCharacteristics = manager.getCameraCharacteristics("" + cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        assert cameraCharacteristics != null;
        Matrix matrix = new Matrix();
        System.out.println(viewHeight + " x " + viewWidth);
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        int transformWidth =  previewSize.getWidth();
        int transformHeight = previewSize.getHeight();
        RectF bufferRect = new RectF(0, 0, Math.min(transformWidth, transformHeight), Math.max(transformWidth, transformHeight));
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        int angle = getJpegOrientation(cameraCharacteristics, rotation);
        bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
        matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.CENTER);
        float scale = Math.min(
                (float) viewHeight / previewSize.getHeight(),
                (float) viewWidth / previewSize.getWidth());
        if (isEmulator()) {
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(angle, centerX, centerY);
        } else if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {

            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (rotation == Surface.ROTATION_180) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);

    }



    @Override
    public void onErrorResponse(VolleyError error) {
            saveImage(tempImage);
            tempImage = null;
    }

    @Override
    public void onResponse(JSONObject response) {
        databaseAccess.updateLastSync();
        showThankYou(true);
    }


    /*static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }*/



    /*private File getOutputMediaFile(int type) {
        // External sdcard location
        File mediaStorageDir = new File(getFilesDir(), Config.IMAGE_DIRECTORY_NAME);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("TAG", "Oops! Failed create " + Config.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            // mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

            String photoName = timeStamp;

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + photoName + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }*/

    private void showThankYou(boolean success){
        Intent intent = new Intent(this, ThankYouActivity.class);
        intent.putExtra("SUCCESS", success);
        if (success)
        intent.putExtra("UserFullName",userFullName);
        else {
            String eventID = databaseAccess.queryDatabase("EventID");
            intent.putExtra("UserFullName",eventID);
        }
        intent.putExtra("Timestamp", actionDateTime);
        startActivity(intent);
        finish();
    }

    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }


}