/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import tool.compet.constraintlayout.DkConstraintLayout;
import tool.compet.view.DkViews;

/**
 * Base item view which be used in DkItemBuilder.getView().
 * For customize view, you must extend this class and provide settings via builder class.
 *
 * Ref: https://github.com/Nightonke/BoomMenu/wiki/Text-Inside-Circle-Button
 */
public class DkItemView extends DkConstraintLayout implements View.OnTouchListener {
	boolean isCircleShape;
	float cornerRadius;
	int normalColor;
	int pressedColor;
	int unableColor;
	boolean useRippleEffect;

	MyGestureDetector gestureDetector;

	private final Path path = new Path();

	public DkItemView(Context context) {
		this(context, null);
	}

	public DkItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DkItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		gestureDetector = new MyGestureDetector(context);
		setOnTouchListener(this);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Drawable background = MyDrawables.rectStateListDrawable(
			normalColor,
			pressedColor,
			unableColor,
			cornerRadius
		);

//		if (isCircleShape) {
//			background = MyDrawables.circleBackground(
//				useRippleEffect,
//				getResources(),
//				w,
//				h,
//				normalColor,
//				pressedColor,
//				unableColor);
//		}
//		else {
//			background = MyDrawables.rectStateListDrawable(
//				normalColor,
//				pressedColor,
//				unableColor,
//				cornerRadius
//			);
//		}

		// Setup background
//		setBackgroundColor(normalColor); // why this causes background cannot be clipped???
		setBackground(background);

		// Setup foreground
		setDefaultForeground(pressedColor);

		// Calculate for shaping view
		int cx = w >> 1;
		int cy = h >> 1;
		int radius = Math.min(cx, cy);

		path.reset();

		if (isCircleShape) {
			path.addCircle(cx, cy, radius, Path.Direction.CW);
		}
		else {
			path.addRoundRect(new RectF(0, 0, w, h), cornerRadius, cornerRadius, Path.Direction.CCW);
		}

		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public void draw(Canvas canvas) {
		// Hardware layer does not support clipping for old version -> should use compat for safe.
		DkViews.clipPath(this, canvas, path);
		canvas.clipPath(path);

		super.draw(canvas);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		gestureDetector.onTouchEvent(v, event);
		return super.onTouchEvent(event);
	}
}
