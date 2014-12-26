package com.example.videotest;

import java.io.File;
import java.io.IOException;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Audio.Media;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private CameraInfo[] mCameraInfo;
    private Button startButton;
    private boolean mIsRecording = false;
    public File outf;
    private MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCamera();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initpreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
        });
        startButton = (Button) findViewById(R.id.start);
        startButton.setOnClickListener(this);
    }

    protected void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    protected void initpreview() {
        mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);//后置摄像头  CAMERA_FACING_FRONT
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setCameraDisplayOrientation(this, CameraInfo.CAMERA_FACING_BACK, mCamera);
        mCamera.startPreview();
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.start:
                if (mIsRecording == false) {
                    startmediaRecorder();
                } else {
                    stopmediaRecorder();
                }
                if (mIsRecording) {
                    startButton.setText("stop");
                } else {
                    startButton.setText("start");
                }
                break;
            default:
                break;
        }

    }

    private void stopmediaRecorder() {
        if (mediaRecorder != null) {
            if (mIsRecording) {
                mediaRecorder.stop();
                //mCamera.lock();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                mIsRecording = false;
                try {
                    mCamera.reconnect();
                } catch (IOException e) {
                    Toast.makeText(this, "reconect fail", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void startmediaRecorder() {
        mCamera.unlock();
        mIsRecording = true;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//		mediaRecorder.setOutputFile(getName());
//		mediaRecorder.setVideoFrameRate(5);
//		mediaRecorder.setVideoSize(640, 480);
        CamcorderProfile mCamcorderProfile = CamcorderProfile.get(CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_LOW);
        mediaRecorder.setProfile(mCamcorderProfile);
        try {
            outf = File.createTempFile("tts", ".3gp", Environment.getExternalStorageDirectory());
            mediaRecorder.setOutputFile(outf.getCanonicalPath());
        } catch (IOException e) {
            return;
        }
        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        try {
            mediaRecorder.prepare();
//			mediaRecorder.set
        } catch (Exception e) {
            mIsRecording = false;
            Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            mCamera.lock();
        }
        mediaRecorder.start();
    }

    private String getName() {
//		String fileName = Environment.getExternalStorageDirectory()+""+System.currentTimeMillis()+".3gp";
        String fileName = "/mnt/emmc/" + System.currentTimeMillis() + ".3gp";
        Log.e("shenwenjian", "fileName" + fileName);
        return fileName;
    }

}
