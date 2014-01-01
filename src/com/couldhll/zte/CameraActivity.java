package com.couldhll.zte;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

public class CameraActivity extends Activity {
	private static final String TAG = "CameraActivity";

	public static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;
	public static final int MEDIA_TYPE = Config.MEDIA_TYPE_IMAGE;
	public static final int ANIMATION_START = 0;
	public static final int ANIMATION1_END = 451;
	public static final int ANIMATION2_END = 443;
	public static final int ANIMATION3_END = 455;

	// public static final int ANIMATION_SHOW = 300;
	// public static final int[] ANIMATION1_CAPTURE = { 194, 220, 411 };
	// public static final int[] ANIMATION2_CAPTURE = { 165, 200, 274 };
	// public static final int[] ANIMATION3_CAPTURE = { 272, 283, 290 };
	// private int[] mAnimationCaptureFrames;

	private int mSceneIndex;
	private int mAnimationEndFrame;

	private Point mActivitySize;
	private int mActivityRotation;

	private Camera mCamera;
	private int mCameraId;
	private int mCameraDisplayOrientation;

	private CameraPreview mCameraPreview;
	private ImageView mAnimationView;
	private AnimationDrawable mAnimationDrawable;

	private ArrayList<File> mSaveFiles;

	private final PictureCallback mPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// camera bitmap
			Bitmap cameraBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

			// animation bitmap
			mAnimationView.setDrawingCacheEnabled(true);
			// mAnimationView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			// mAnimationView.layout(0, 0, mAnimationView.getMeasuredWidth(), mAnimationView.getMeasuredHeight());
			mAnimationView.buildDrawingCache(true);
			Bitmap animationBitmap = Bitmap.createBitmap(mAnimationView.getDrawingCache());
			mAnimationView.setDrawingCacheEnabled(false);

			// scale animation bitmap
			// Bitmap animationBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.day_00425)).getBitmap();
			// int scale = mActivitySize.x / animationBitmap.getWidth();
			// animationBitmap = Bitmap.createScaledBitmap(animationBitmap, scale, scale, true);

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

			// add save file to list
			mSaveFiles.add(bitmapFile);

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

		// hide button
		hideButton();

		// get save files
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			mSceneIndex = bundle.getInt("mSceneIndex");
		} else {
			mSceneIndex = 1;
		}

		// init info
		switch (mSceneIndex) {
		case 1:
			mAnimationEndFrame = ANIMATION1_END;
			// mAnimationCaptureFrames = ANIMATION1_CAPTURE;
			break;
		case 2:
			mAnimationEndFrame = ANIMATION2_END;
			// mAnimationCaptureFrames = ANIMATION2_CAPTURE;
			break;
		case 3:
			mAnimationEndFrame = ANIMATION3_END;
			// mAnimationCaptureFrames = ANIMATION3_CAPTURE;
			break;
		default:
			mAnimationEndFrame = ANIMATION1_END;
			// mAnimationCaptureFrames = ANIMATION1_CAPTURE;
			break;
		}

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
		ImageButton captureButton = (ImageButton) findViewById(R.id.captureImageButton);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCamera.takePicture(null, null, mPictureCallback);
			}
		});

		// init animation
		mAnimationView = (ImageView) findViewById(R.id.animationImageView);
		mAnimationDrawable = new AnimationDrawable();
		for (int i = ANIMATION_START; i <= mAnimationEndFrame; i++) {
			String identifierNameString = String.format("sence%d_%05d", mSceneIndex, i);
			Log.d(TAG, identifierNameString);
			int id = getResources().getIdentifier(identifierNameString, "drawable", getApplicationContext().getPackageName());
			mAnimationDrawable.addFrame(getResources().getDrawable(id), 1000 / 24);
		}
		mAnimationView.setImageDrawable(mAnimationDrawable);
		mAnimationDrawable.start();

		// hide loading
		// ProgressBar loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
		// loadingProgressBar.setVisibility(View.GONE);

		// show capture&next button
		final Handler handler = new Handler();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						showButton();
					}
				});
			}
		}, 1000 * 10);

		// // add listener to animation stop
		// CustomAnimDrawable customAnimDrawable = new CustomAnimDrawable(mAnimationDrawable);
		// customAnimDrawable.setAnimationListener(new CustomAnimDrawable.AnimationDrawableListener() {
		// @Override
		// public void onAnimationStart(AnimationDrawable animation) {
		// // TODO Auto-generated method stub
		// }
		//
		// @Override
		// public void onAnimationEnd(AnimationDrawable animation) {
		// // show capture&next button
		// // showButton();
		// }
		//
		// @Override
		// public void onAnimationFrame(AnimationDrawable animation, int frameIndex) {
		// // // show capture&next button
		// // if (frameIndex == ANIMATION_SHOW) {
		// // showButton();
		// // }
		// //
		// // // camera take picture
		// // for (int frame : mAnimationCaptureFrames) {
		// // if (frameIndex == frame) {
		// // mCamera.takePicture(null, null, mPictureCallback);
		// // }
		// // }
		// }
		// });
		// customAnimDrawable.start();

		// init save file list
		mSaveFiles = new ArrayList<File>();
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
		releaseAnimation(); // release the animation immediately on pause event
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	private void releaseAnimation() {
		mAnimationDrawable.stop();
		for (int i = 0; i < mAnimationDrawable.getNumberOfFrames(); ++i) {
			Drawable frame = mAnimationDrawable.getFrame(i);
			if (frame instanceof BitmapDrawable) {
				((BitmapDrawable) frame).getBitmap().recycle();
			}
			frame.setCallback(null);
		}
		mAnimationDrawable.setCallback(null);
	}

	public void gotoNextActivity(View view) {
		// goto share activity
		Intent intent = new Intent(CameraActivity.this, ShareActivity.class);
		intent.putExtra("mSaveFiles", mSaveFiles);
		startActivity(intent);
		finish();
	}

	private void showButton() {
		ImageButton captureButton = (ImageButton) findViewById(R.id.captureImageButton);
		captureButton.setVisibility(View.VISIBLE);

		ImageButton nextButton = (ImageButton) findViewById(R.id.nextImageButton);
		nextButton.setVisibility(View.VISIBLE);
	}

	private void hideButton() {
		ImageButton captureButton = (ImageButton) findViewById(R.id.captureImageButton);
		captureButton.setVisibility(View.GONE);

		ImageButton nextButton = (ImageButton) findViewById(R.id.nextImageButton);
		nextButton.setVisibility(View.GONE);
	}
}
