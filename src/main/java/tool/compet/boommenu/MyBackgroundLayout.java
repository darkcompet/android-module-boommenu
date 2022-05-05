/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.content.Context;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import tool.compet.animation.DkLookupTableInterpolator;

class MyBackgroundLayout extends FrameLayout implements View.OnClickListener, View.OnTouchListener, View.OnKeyListener {
	interface Listener {
		void onSizeChanged(int w, int h, int oldw, int oldh);

		boolean onClick(View v);

		boolean onTranslate(float dx, float dy);

		boolean onKey(View v, int keyCode, KeyEvent event);
	}

	private static final int DEFAULT_DIM_COLOR = Color.parseColor("#55000000");

	int dimColor = DEFAULT_DIM_COLOR;
	Listener listener;
	long startDelay;

	private float f1;
	private float f2;
	private boolean needRoundStart = true;
	private boolean needRoundEnd = true;
	private final MyGestureDetector detector;
	private static final MyArgbEvaluator colorEvaluator = new MyArgbEvaluator(DEFAULT_DIM_COLOR);
	private static final Interpolator interpolator = new DkLookupTableInterpolator(new float[] {
		0.0000f, 0.2560f, 0.3590f, 0.4359f, 0.4989f, 0.5528f, 0.6000f, 0.6420f, 0.6799f, 0.7141f,
		0.7454f, 0.7739f, 0.8000f, 0.8239f, 0.8459f, 0.8660f, 0.8844f, 0.9012f, 0.9165f, 0.9304f,
		0.9428f, 0.9539f, 0.9638f, 0.9724f, 0.9798f, 0.9860f, 0.9911f, 0.9950f, 0.9978f, 0.9994f,
		1.0000f
	}); // ease circ out

	MyBackgroundLayout(Context context) {
		super(context);

		setFocusable(true);
		setClickable(true);
		setOnTouchListener(this);
		setOnKeyListener(this);

		setBackgroundColor(Color.TRANSPARENT);

		detector = new MyGestureDetector(context);
		detector.setListener(new MyGestureDetector.Listener() {
			@Override
			public boolean onTranslate(float dx, float dy) {
				return listener != null && listener.onTranslate(dx, dy);
			}

			@Override
			public boolean onClick(float rawX, float rawY) {
				return listener != null && listener.onClick(MyBackgroundLayout.this);
			}
		});
	}

	void updateDimension(int width, int height) {
		ViewGroup.LayoutParams params = getLayoutParams();

		if (params == null) {
			params = new ViewGroup.LayoutParams(width, height);
		}
		else {
			params.width = width;
			params.height = height;
		}

		setLayoutParams(params);
	}

	void setupAnimation(long animStartDelay, long duration, long totalDuration) {
		needRoundStart = true;
		needRoundEnd = true;

		long startTime = animStartDelay + startDelay;
		long endTime = startTime + duration;

		f1 = startTime / (float) totalDuration;
		f2 = endTime / (float) totalDuration;
	}

	void onAnimationUpdate(float f) {
		f = interpolator.getInterpolation(f);
		f = (f - f1) / (f2 - f1);

		if (needRoundStart && f < 0f) {
			needRoundStart = false;
			f = 0f;
		}
		else if (needRoundEnd && f > 1f) {
			needRoundEnd = false;
			f = 1f;
		}

		if (f >= 0f && f <= 1f) {
			setBackgroundColor(colorEvaluator.evaluate(f, Color.TRANSPARENT, dimColor));
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		listener = null;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (listener != null) {
			listener.onSizeChanged(w, h, oldw, oldh);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return detector.onTouchEvent(v, event) || super.onTouchEvent(event);
	}

	@Override
	public void onClick(View v) {
		if (listener != null) {
			listener.onClick(this);
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return listener != null && listener.onKey(v, keyCode, event);
	}
}
