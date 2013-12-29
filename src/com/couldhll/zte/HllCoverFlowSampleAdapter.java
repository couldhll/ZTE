/*
 * Copyright 2013 David Schreiber 2013 John Paul Nalog Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.couldhll.zte;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;

public class HllCoverFlowSampleAdapter extends FancyCoverFlowAdapter {

	public SelectActivity activity;

	// =============================================================================
	// Private members
	// =============================================================================

	private final int[] images = { R.drawable.select_sence1, R.drawable.select_sence2, R.drawable.select_sence3 };

	// =============================================================================
	// Supertype overrides
	// =============================================================================

	@Override
	public int getCount() {
		return images.length;
	}

	@Override
	public Integer getItem(int i) {
		return images[i];
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getCoverFlowItem(int i, View reuseableView, ViewGroup viewGroup) {
		ImageView imageView = null;

		if (reuseableView != null) {
			imageView = (ImageView) reuseableView;
		} else {
			imageView = new ImageView(viewGroup.getContext());
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setLayoutParams(new FancyCoverFlow.LayoutParams((int) (932 * 1.3f), (int) (574 * 1.3f)));
			imageView.setTag(i);
			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					// Intent i = new Intent(Intent.actionview, Uri.parse("http://mettletech.co/"));
					// view.getContext().startActivity(i);

					int tag = (Integer) view.getTag();
					int index = tag + 1;
					activity.selectSceneWithInt(index);
				}
			});
		}

		imageView.setImageResource(this.getItem(i));
		return imageView;
	}
}
