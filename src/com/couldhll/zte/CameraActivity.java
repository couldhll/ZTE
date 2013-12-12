package com.couldhll.zte;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class CameraActivity extends Activity {
	private static final String TAG = "CameraActivity";

	public static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;
	public static final int MEDIA_TYPE = Config.MEDIA_TYPE_IMAGE;
	public static final int ANIMATION_START = 0;
	public static final int ANIMATION_END = 425;

	private Point mActivitySize;
	private int mActivityRotation;

	private Camera mCamera;
	private int mCameraId;
	private int mCameraDisplayOrientation;

	private CameraPreview mCameraPreview;
	private ImageView mAnimationView;

	private final PictureCallback mPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// camera bitmap
			Bitmap cameraBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

			// animation bitmap
			Bitmap animationBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.day_00100)).getBitmap();

			// scale animation bitmap
			int scale = mActivitySize.x / animationBitmap.getWidth();
			animationBitmap = Bitmap.createScaledBitmap(animationBitmap, scale, scale, true);

			// compose bitmap
			cameraBitmap = cameraBitmap.copy(Bitmap.Config.ARGB_8888, true);// for Bitmap.createBitmap
			Bitmap composeBitmap = Bitmap.createBitmap(cameraBitmap);
			Canvas canvas = new Canvas(composeBitmap);
			canvas.drawBitmap(animationBitmap, 0, 0, null);
			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();

			// save
			File bitmapFile = Config.getOutputMediaFile(MEDIA_TYPE);
			Config.saveBitmap(composeBitmap, bitmapFile);

			// continue preview
			mCamera.startPreview();
		}
	};

	// /**
	// * A pretty basic example of an AsyncTask that takes the photo and then sleeps for a defined period of time before finishing. Upon finishing, it will restart the preview -
	// * Camera.startPreview().
	// */
	// private class TakePictureTask extends AsyncTask<Void, Void, Void> {
	//
	// @Override
	// protected void onPostExecute(Void result) {
	// // This returns the preview back to the live camera feed
	// // mCamera.startPreview();
	// }
	//
	// @Override
	// protected Void doInBackground(Void... params) {
	// mCamera.takePicture(null, null, mPictureCallback);
	//
	// // // Sleep for however long, you could store this in a variable and
	// // // have it updated by a menu item which the user selects.
	// // try {
	// // Thread.sleep(3000); // 3 second preview
	// // } catch (InterruptedException e) {
	// // // TODO Auto-generated catch block
	// // e.printStackTrace();
	// // }
	//
	// return null;
	// }
	// }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		// init activity info
		mActivitySize = Config.getActivitySize(this);
		mActivityRotation = Config.getActivityRotation(this);

		// init camera info
		mCameraId = Config.getCameramId(CameraActivity.CAMERA_FACING);
		Log.i(TAG, "Camera ID: " + String.valueOf(mCameraId));
		mCamera = Config.getCamera(mCameraId);
		mCameraDisplayOrientation = Config.getCameraDisplayOrientation(mCameraId, mActivityRotation);
		// mCamera.setDisplayOrientation(mCameraDisplayOrientation);

		// set camera parameters
		Camera.Parameters cameraParameters = mCamera.getParameters();
		cameraParameters.setPreviewSize(mActivitySize.x, mActivitySize.y);
		Log.i(TAG, "Camera Preview Size: " + String.valueOf(mActivitySize.x) + "x" + String.valueOf(mActivitySize.y));
		cameraParameters.setPictureSize(mActivitySize.x, mActivitySize.y);
		Log.i(TAG, "Camera Picture Size: " + String.valueOf(mActivitySize.x) + "x" + String.valueOf(mActivitySize.y));
		cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		cameraParameters.setPictureFormat(ImageFormat.JPEG);
		try {
			mCamera.setParameters(cameraParameters);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// init preview view
		mCameraPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.previewView);
		preview.addView(mCameraPreview);

		// Add a listener to the Capture button
		Button captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCamera.takePicture(null, null, mPictureCallback);
			}
		});

		// init animation
		mAnimationView = (ImageView) findViewById(R.id.animationImageView);
		AnimationDrawable animationDrawable = new AnimationDrawable();
		for (int i = ANIMATION_START; i <= ANIMATION_END; i++) {
			Log.d(TAG, String.format("day_%05d", i));
			int id = getResources().getIdentifier(String.format("day_%05d", i), "drawable", getApplicationContext().getPackageName());
			animationDrawable.addFrame(getResources().getDrawable(id), 1000 / 24);
		}
		mAnimationView.setImageDrawable(animationDrawable);
		animationDrawable.start();
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
		releaseCamera(); // release the camera immediately on pause event
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}
}
