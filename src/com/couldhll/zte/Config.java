package com.couldhll.zte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

public class Config {
	private static final String TAG = "Config";

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	public Config() {

	}

	/**
	 * get camera by camera id
	 */
	public static Camera getCamera(int cameraId) {
		Camera camera = null;

		try {
			camera = Camera.open(cameraId); // get camera
		} catch (Exception e) {
			// camera is not available (in use or does not exist)
		}

		return camera; // return null if camera is unavailable
	}

	/**
	 * get camera id by facing
	 */
	public static int getCameramId(int facing) {
		int cameraID = -1;

		int cameraCount = 0;
		cameraCount = Camera.getNumberOfCameras(); // get cameras number
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
			Camera.getCameraInfo(cameraIndex, cameraInfo); // get camera info
			if (cameraInfo.facing == facing) { // get camera id by facing
				cameraID = cameraIndex;
			}
		}

		return cameraID; // return -1 if camera is unavailable
	}

	/**
	 * get camera display orientation
	 */
	public static int getCameraDisplayOrientation(int cameraId, int activityRotation) {
		int displayOrientation;

		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) { // front facing
			displayOrientation = (info.orientation + activityRotation) % 360;
			displayOrientation = (360 - displayOrientation) % 360; // compensate the mirror
		} else { // back facing
			displayOrientation = (info.orientation - activityRotation + 360) % 360;
		}

		return displayOrientation;
	}

	/**
	 * get activity rotation
	 */
	public static int getActivityRotation(Activity activity) {
		int rotation = 0;

		int activityRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		switch (activityRotation) {
		case Surface.ROTATION_0:
			rotation = 0;
		case Surface.ROTATION_90:
			rotation = 90;
		case Surface.ROTATION_180:
			rotation = 180;
		case Surface.ROTATION_270:
			rotation = 270;
		}

		return rotation;
	}

	/**
	 * get activity size
	 */
	public static Point getActivitySize(Activity activity) {
		Point size = new Point();

		activity.getWindowManager().getDefaultDisplay().getSize(size);

		return size;
	}

	/** save bitmap to file */
	public static void saveBitmap(Bitmap bitmap, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();// output
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		} catch (Exception e) {
			Log.d(TAG, "Error creating media file, check storage permissions: " + e.getMessage());
		}
	}

	/** Create a file for saving an image or video */
	public static File getOutputMediaFile(int type) {
		// Check whether the media is mounted with read/write permission.
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Log.d(TAG, "Error creating media file, check storage persmissions!");
			return null;
		}

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ZTE");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
}
