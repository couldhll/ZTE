package com.couldhll.zte;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;

public class SelectActivity extends Activity {
	private static final String TAG = "SelectActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select);

		FrameLayout sceneLayout = (FrameLayout) findViewById(R.id.previewView);

		HllCoverFlowSampleAdapter adapter = new HllCoverFlowSampleAdapter();
		adapter.activity = this;

		HllCoverFlow coverFlow = new HllCoverFlow(SelectActivity.this);
		coverFlow.setAdapter(adapter);
		coverFlow.setUnselectedAlpha(0.3f);
		coverFlow.setUnselectedSaturation(0.0f);
		coverFlow.setUnselectedScale(0.9f);
		coverFlow.setSpacing(-150);
		coverFlow.setMaxRotation(45);
		coverFlow.setScaleDownGravity(0.2f);
		coverFlow.setReflectionEnabled(true);
		coverFlow.setReflectionRatio(0.3f);
		coverFlow.setReflectionGap(0);
		coverFlow.setActionDistance(FancyCoverFlow.ACTION_DISTANCE_AUTO);

		sceneLayout.addView(coverFlow);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select, menu);
		return true;
	}

	public void gotoPreviousActivity(View view) {
		// goto main activity
		Intent intent = new Intent(SelectActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	public void selectSceneWithView(View view) {
		String selectIndexString = (String) view.getTag();
		int selectIndex = Integer.parseInt(selectIndexString);

		selectSceneWithInt(selectIndex);
	}

	public void selectSceneWithInt(int index) {
		// goto camera activity
		Intent intent = new Intent(SelectActivity.this, CameraActivity.class);
		intent.putExtra("mSceneIndex", index);
		startActivity(intent);
		finish();
	}
}
