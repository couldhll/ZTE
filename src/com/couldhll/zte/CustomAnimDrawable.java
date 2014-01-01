package com.couldhll.zte;

import java.lang.reflect.Field;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.Log;

public class CustomAnimDrawable extends AnimationDrawable {
	private final String TAG = "xmp";
	private final AnimationDrawable mOriAnim;
	private AnimationDrawable mSelf;
	private Handler mHandler;
	private boolean mStarted;
	private AnimEndListenerRunnable mEndRunnable;
	private AnimationDrawableListener mListener;

	public CustomAnimDrawable(AnimationDrawable anim) {
		mOriAnim = anim;
		initialize();
	}

	private void initialize() {
		mSelf = this;
		mStarted = false;
		mHandler = new Handler();
		mEndRunnable = new AnimEndListenerRunnable();
		// for (int i = 0; i < mOriAnim.getNumberOfFrames(); i++) {
		// mSelf.addFrame(mOriAnim.getFrame(i), mOriAnim.getDuration(i));
		// }
	}

	@Override
	public void start() {
		mOriAnim.start();
		mStarted = true;
		mHandler.post(mEndRunnable);
		if (mListener != null) {
			mListener.onAnimationStart(mSelf);
		}
		Log.v(TAG, "------CustomAnimDrawable------>start");
	}

	/**
	 * 循环检测 动画的状态
	 */
	class AnimEndListenerRunnable implements Runnable {
		@Override
		public void run() {
			// 动画已开始
			if (!mStarted) {
				return;
			}
			// 未停止继续监听
			if (!isEnd()) {
				// 这里的延迟时间是跟你的每一帧动画时间有关，基本保持一致就可以，最后一帧也会完整播放
				// 上面动画时间为每一帧1000ms，所以这里设为了1000ms
				mHandler.postDelayed(mEndRunnable, 1000 / 24);
				return;
			}
			Log.v(TAG, "----------->over");
			// 动画已结束
			if (mListener != null) {
				mStarted = false;
				mListener.onAnimationEnd(mSelf);
			}
		}
	}

	/**
	 * 判断动画是否结束 采用反射机制
	 * 
	 * @return
	 */
	private boolean isEnd() {
		Class<AnimationDrawable> animClass = AnimationDrawable.class;
		try {
			// 利用Java反射方法判断是否结束
			// 获得私有方法 例如
			// Method method = animClass.getDeclaredMethod("nextFrame",boolean.class);

			// 访问其私有变量
			Field field = animClass.getDeclaredField("mCurFrame");
			field.setAccessible(true);

			int currFrameNum = field.getInt(mOriAnim);
			int totalFrameNum = mOriAnim.getNumberOfFrames();

			// 动画帧
			if (mListener != null) {
				mListener.onAnimationFrame(mSelf, currFrameNum);
			}

			if ((currFrameNum == totalFrameNum - 1) || (currFrameNum == -1)) {
				return true;
			}
		} catch (Exception e) {
			Log.v(TAG, "-------->Exception");
		}

		return false;
	}

	public void setAnimationListener(AnimationDrawableListener listener) {
		mListener = listener;
	}

	public interface AnimationDrawableListener {
		/**
		 * Notifies the start of the animation
		 * 
		 * @param animation
		 */
		public void onAnimationStart(AnimationDrawable animation);

		/**
		 * Notifies the end of the animation
		 * 
		 * @param animation
		 */
		public void onAnimationEnd(AnimationDrawable animation);

		public void onAnimationFrame(AnimationDrawable animation, int frameIndex);
	}
}