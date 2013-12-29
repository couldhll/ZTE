package com.couldhll.zte;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxLink;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.google.zxing.WriterException;

public class ShareActivity extends Activity {
	private static final String TAG = "ShareActivity";

	// dropbox app key
	public final static String APP_KEY = "0indni3j9vi4c1p";
	public final static String APP_SECRET = "16xxwyfs4c123zf";
	public final static AccessType ACCESS_TYPE = AccessType.DROPBOX;

	// dropbox token save
	private static final String ACCOUNT_PREFS_NAME = "prefs";
	private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
	private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	// save path in dropbox
	private final String PHOTO_DIR = "/ZTE";

	private DropboxAPI<AndroidAuthSession> mDBApi;

	// login state
	private boolean mLoggedIn;

	private ArrayList<File> mSaveFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);

		// get save files
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			mSaveFiles = (ArrayList<File>) bundle.get("mSaveFiles");
		} else {
			mSaveFiles = new ArrayList<File>();
			for (int i = 0; i < 1; i++) {
				File file = new File("/storage/sdcard/Pictures/ZTE/IMG_20131222_174803.jpg");
				mSaveFiles.add(file);
			}
		}

		// init DBApi
		AndroidAuthSession session = buildSession();
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);

		// Display the proper UI state if logged in or not
		setLoggedIn(mDBApi.getSession().isLinked());

		if (!mLoggedIn) {
			mDBApi.getSession().startAuthentication(ShareActivity.this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.share, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mDBApi.getSession().authenticationSuccessful()) {
			try {
				// Required to complete auth, sets the access token on the session
				mDBApi.getSession().finishAuthentication();

				// store token
				AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);
				setLoggedIn(true);
			} catch (IllegalStateException e) {
				Log.i("DbAuthLog", "Error authenticating", e);
			}
		}
	}

	public void gotoNextActivity(View view) {
		// goto select activity
		Intent intent = new Intent(ShareActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	private void logOut() {
		// Remove credentials from the session
		mDBApi.getSession().unlink();

		// Clear our stored keys
		clearKeys();
		// Change UI state to display logged out version
		setLoggedIn(false);
	}

	/**
	 * Convenience function to change UI state based on being logged in
	 */
	private void setLoggedIn(boolean loggedIn) {
		mLoggedIn = loggedIn;
		if (loggedIn) {
			// set dropbox dir
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			final String dropboxDir = PHOTO_DIR + "/" + timeStamp;

			new Thread(new Runnable() {
				@Override
				public void run() {
					// share link for dir
					try {
						Entry entry = mDBApi.createFolder(dropboxDir);
						DropboxLink dropboxLink = mDBApi.share(dropboxDir);

						// barcode bitmap
						final Bitmap barcodeBitmap = EncodingHandler.createQRCode(dropboxLink.url, 640);

						// barcode image
						final ImageView barcodeImageView = (ImageView) findViewById(R.id.barcodeImageView);
						barcodeImageView.post(new Runnable() {
							@Override
							public void run() {
								barcodeImageView.setImageBitmap(barcodeBitmap);
							}
						});
					} catch (DropboxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (WriterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

			// upload files
			for (File file : mSaveFiles) {
				UploadPicture upload = new UploadPicture(this, mDBApi, dropboxDir, file);
				upload.execute();
			}
		} else {
			// mSubmit.setText("Link with Dropbox");
			// mDisplay.setVisibility(View.GONE);
			// mImage.setImageDrawable(null);
		}
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local store, rather than storing user name & password, and re-authenticating each time (which is not
	 * to be done, ever).
	 * 
	 * @return Array of [access_key, access_secret], or null if none stored
	 */
	private String[] getKeys() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local store, rather than storing user name & password, and re-authenticating each time (which is not
	 * to be done, ever).
	 */
	private void storeKeys(String key, String secret) {
		// Save the access key for later
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, key);
		edit.putString(ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	private void clearKeys() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}

		return session;
	}
}
