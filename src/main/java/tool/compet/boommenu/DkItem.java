/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.graphics.PointF;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

/**
 * Contains item info after build.
 */
public class DkItem {
	// Basic
	protected int index;
	protected View view;
	protected DkOnItemClickListener onClickLisener;
	protected int width;
	protected int height;
	protected int margin;

	// Extras
	boolean dismissMenuImmediateOnClickItem;
	boolean dismissMenuOnClickItem;

	// Animations
	final PointF startPos = new PointF();
	final PointF endPos = new PointF();
	long animStartDelay;
	boolean enableRotation;
	boolean enable3DAnimation;
	boolean enableScale;
	Interpolator movingInterpolator;
	DkMovingShape movingShape;
	float startRotationDegrees;
	float endRotationDegrees;
	float startScaleFactor;
	float endScaleFactor;
	// private animation info
	private float startFraction;
	private float endFraction;
	private boolean needRoundStart = true;
	private boolean needRoundEnd = true;
	private MyScaleEvaluator scaleEvaluator;
	private MyRotationEvaluator rotationEvaluator;
	private MyMotionCalculator motionCalculator;

	void setupAnimation(long startDelay, long duration, long totalDuration) {
		needRoundStart = true;
		needRoundEnd = true;

		long startTime = animStartDelay + startDelay;
		long endTime = startTime + duration;

		startFraction = startTime / (float) totalDuration;
		endFraction = endTime / (float) totalDuration;

		if (enableRotation && rotationEvaluator == null) {
			rotationEvaluator = new MyRotationEvaluator(startRotationDegrees, endRotationDegrees);
		}
		if (enableScale && scaleEvaluator == null) {
			scaleEvaluator = new MyScaleEvaluator(startScaleFactor, endScaleFactor);
		}
		motionCalculator = new MyMotionCalculator(movingInterpolator, movingShape, startPos, endPos);
	}

	void onAnimationUpdate(float f) {
		f = (f - startFraction) / (endFraction - startFraction);

		if (needRoundStart && f < 0f) {
			needRoundStart = false;
			f = 0f;
		}
		else if (needRoundEnd && f > 1f) {
			needRoundEnd = false;
			f = 1f;
		}

		if (f >= 0f && f <= 1f) {
			View view = this.view;
			motionCalculator.calcCurrentCoordinates(f);
			view.setX(motionCalculator.curX);
			view.setY(motionCalculator.curY);

			if (enableRotation) {
				float degrees = rotationEvaluator.getAnimatedValue(f);
				view.setRotation(degrees);
			}

			if (enable3DAnimation) {
				float degrees = rotationEvaluator.getAnimatedValue(f);
				view.setRotationX(degrees);
				view.setRotationY(degrees);
			}

			if (enableScale) {
				float sf = scaleEvaluator.getAnimatedValue(f);
				view.setScaleX(sf);
				view.setScaleY(sf);
			}
		}
	}

	/**
	 * Make view be normally displayed on its parent.
	 * Because we apply scale to view instead of making it to gone,
	 * so the view is always visible, just scale up to 1.
	 */
	void show() {
		View view = this.view;

		view.setScaleX(1f);
		view.setScaleY(1f);

		if (view.getX() != endPos.x) {
			view.setX(endPos.x);
		}
		if (view.getY() != endPos.y) {
			view.setY(endPos.y);
		}
	}

	/**
	 * Make view be hidden on its parent.
	 * We make the view size to 0 instead of make it gone.
	 */
	void hide() {
		view.setScaleX(0f);
		view.setScaleY(0f);
	}

	void updateViewDimension(int width, int height) {
		ViewGroup.LayoutParams params = view.getLayoutParams();

		if (params == null) {
			params = new ViewGroup.LayoutParams(width, height);
		}
		else {
			params.width = width;
			params.height = height;
		}

		view.setLayoutParams(params);
	}

	public View getView() {
		return view;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
