/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

class MyGestureDetector {
	public interface Listener {
		boolean onTranslate(float dx, float dy);

		boolean onClick(float rawX, float rawY);
	}

	private float downX;
	private float downY;
	private float lastX;
	private float lastY;
	private Listener listener;

	private final int squaredTouchSlop;
	private boolean clickable;
	private boolean movable;

	public MyGestureDetector(Context context) {
		int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		squaredTouchSlop = touchSlop * touchSlop;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public boolean onTouchEvent(View v, MotionEvent event) {
		float rawX = event.getRawX();
		float rawY = event.getRawY();
		boolean eatEvent = false;

		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN: {
				clickable = true;
				movable = false;
				downX = lastX = rawX;
				downY = lastY = rawY;
				eatEvent = true;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				if (movable) {
					if (listener != null) {
						eatEvent = listener.onTranslate(rawX - lastX, rawY - lastY);
					}
					lastX = rawX;
					lastY = rawY;
				}
				else {
					float dx = rawX - downX;
					float dy = rawY - downY;

					if (dx * dx + dy * dy >= squaredTouchSlop) {
						movable = true;
						clickable = false;
						lastX = rawX;
						lastY = rawY;
					}
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				if (clickable && listener != null) {
					eatEvent = listener.onClick(rawX, rawY);
				}
				break;
			}
		}

		return eatEvent;
	}
}
