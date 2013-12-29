package com.couldhll.zte;

import android.content.Context;
import android.view.MotionEvent;

public class HllCoverFlow extends at.technikum.mti.fancycoverflow.FancyCoverFlow {

	private MotionEvent e;

	public HllCoverFlow(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		boolean bb = super.onInterceptTouchEvent(ev);
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			e = MotionEvent.obtain(ev);
			super.onTouchEvent(ev);
		} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			// 手指触摸的大小.........这儿我设的是20像素
			if (Math.abs(ev.getX() - e.getX()) > 5 || Math.abs(ev.getY() - e.getY()) > 5) {
				bb = true;
			}
		}
		return bb;
	}
}
