package com.couldhll.zte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.R.id;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.Surface;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CameraActivity extends Activity {
	private static final String TAG = "CameraActivity";
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	private static int mCameraId;
	private static float mRotation;
	
	private Camera mCamera;
    private CameraPreview mPreview;
    
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
		int cameraCount = 0;
	    Camera cam = null;
	 
	    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
	    cameraCount = Camera.getNumberOfCameras(); // get cameras number
	          
	    for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
	        Camera.getCameraInfo( camIdx, cameraInfo ); // get camera info
	        if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_FRONT ) { // front camera
	            try {            
	                cam = Camera.open( camIdx ); // attempt to get a Camera instance
	                mCameraId=camIdx;
	            }
	    	    catch (Exception e){
	    	        // Camera is not available (in use or does not exist)
	    	    }
	        }
	    }
	    
	    return cam; // returns null if camera is unavailable
	}
	
	public static int getDisplayRotation(Activity activity) {  
	    int rotation = activity.getWindowManager().getDefaultDisplay()  
	       .getRotation();  
	    switch (rotation) {  
	        case Surface.ROTATION_0: return 0;  
	        case Surface.ROTATION_90: return 90;  
	        case Surface.ROTATION_180: return 180;  
	        case Surface.ROTATION_270: return 270;  
	    }  
	    return 0;  
	}  
	
	public static void setCameraDisplayOrientation(Activity activity,  
	        int cameraId, Camera camera) {  
	    // See android.hardware.Camera.setCameraDisplayOrientation for  
	    // documentation.  
	    Camera.CameraInfo info = new Camera.CameraInfo();  
	    Camera.getCameraInfo(cameraId, info);  
	    
	    int degrees = getDisplayRotation(activity);  
	    int result;  
	    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {  
	        result = (info.orientation + degrees) % 360;  
	        result = (360 - result) % 360;  // compensate the mirror  
	    } else {  // back-facing  
	        result = (info.orientation - degrees + 360) % 360;  
	    }  
	    camera.setDisplayOrientation(result);  
	    mRotation=result;
	}  
	
	/**
	 * A pretty basic example of an AsyncTask that takes the photo and
	 * then sleeps for a defined period of time before finishing. Upon
	 * finishing, it will restart the preview - Camera.startPreview().
	 */
	private class TakePictureTask extends AsyncTask<Void, Void, Void> {

	    @Override
	    protected void onPostExecute(Void result) {
	        // This returns the preview back to the live camera feed
	        mCamera.startPreview();
	    }

	    @Override
	    protected Void doInBackground(Void... params) {
	        mCamera.takePicture(null, null, mPictureCallback);

	        // Sleep for however long, you could store this in a variable and
	        // have it updated by a menu item which the user selects.
	        try {
	            Thread.sleep(3000); // 3 second preview
	        } catch (InterruptedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }

	        return null;
	    }
	}
	
//	private class ScreenshotTask extends AsyncTask<Void, Void, Void> {
//
//	    @Override
//	    protected void onPostExecute(Void result) {
//	        // This returns the preview back to the live camera feed
//	        mCamera.startPreview();
//	    }
//
//	    @Override
//	    protected Void doInBackground(Void... params) {
//        	// screen shot
//	    	mPreview.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));  
//	    	mPreview.layout(0, 0, mPreview.getMeasuredWidth(), mPreview.getMeasuredHeight());  
//	    	mPreview.buildDrawingCache(true);  
//        	Bitmap bitmap = mPreview.getDrawingCache();
//        	mPreview.setDrawingCacheEnabled(false);
//        	
//        	// save
//            saveBitmap(bitmap);
//        	
//	        // Sleep for however long, you could store this in a variable and
//	        // have it updated by a menu item which the user selects.
//	        try {
//	            Thread.sleep(3000); // 3 second preview
//	        } catch (InterruptedException e) {
//	            // TODO Auto-generated catch block
//	            e.printStackTrace();
//	        }
//
//	        return null;
//	    }
//	}
	
    private PictureCallback mPictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	// camera shot
        	Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);  
            Matrix matrix = new Matrix();  
            matrix.preRotate(mRotation);  
            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);  
            
            // save
            saveBitmap(bitmap);
        }
    };
    
    private static void saveBitmap(Bitmap bitmap) {
		try {
        	File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);//将图片压缩到流中  
            fos.flush();//输出 
//            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        } catch (Exception e) {
        	Log.d(TAG, "Error creating media file, check storage permissions: " +
                    e.getMessage());
        }
	}
	
	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		
        // Check whether the media is mounted with read/write permission.
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
        	Log.d(TAG, "Error creating media file, check storage persmissions!");
            return null;
        }

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "ZTE");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
	
		//Get Screen Size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        
	    // Create an instance of Camera
        mCamera = getCameraInstance();
        setCameraDisplayOrientation(CameraActivity.this,mCameraId,mCamera);
//        mCamera.setDisplayOrientation(90);
        
        Camera.Parameters p = mCamera.getParameters();
        Log.i("CheckCapture", "Preview Size: " + String.valueOf(width) +"x" + String.valueOf(height));
//        p.setPreviewSize(width, height);
        //Set picture size to a multiple of previewSize to maintain aspect ratio AND minimum capture width
//        Log.i("CheckCapture", "Picture Size: " + String.valueOf(width*factor) +"x" + String.valueOf(height*factor));
//        p.setPictureSize(width, height);
        p.setPictureSize(height, width);
//        p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        //Set picture format (we can check device capabilities, but all devices at API level 8 should support JPEG)
//        p.setPictureFormat(PixelFormat.JPEG);
        //Set new camera parameters
        try {
        	mCamera.setParameters(p);
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // water
        TextView tv = new TextView(this);
        tv.setText("test");
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        preview.addView(tv, lp);
        
        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	// get an image from the camera
//                	TakePictureTask takePictureTask = new TakePictureTask();
//                    takePictureTask.execute();
                    
//                    mCamera.takePicture(null, null, mPictureCallBack);
                    
                    // get an image from the screen
//                    ScreenshotTask screenshotTask = new ScreenshotTask();
//                    screenshotTask.execute();
                	
                	// screen shot
        	    	mPreview.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));  
        	    	mPreview.layout(0, 0, mPreview.getMeasuredWidth(), mPreview.getMeasuredHeight());  
//        	    	mPreview.buildDrawingCache();  
//                	Bitmap bitmap = mPreview.getDrawingCache();
//                	mPreview.setDrawingCacheEnabled(false);
                	
                	Bitmap bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888);
                	mPreview.draw(new Canvas(bitmap));
                	
                	// save
                    saveBitmap(bitmap);
                }
            }
        );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	@Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}
