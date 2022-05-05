/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.graphics.PointF;
import android.view.animation.Interpolator;

class MyMotionCalculator {
	public float curX;
	public float curY;

	private final PointF startPos;
	private final PointF endPos;
	private final DkMovingShape movingShape;
	private final Interpolator movingInterpolator;

	MyMotionCalculator(Interpolator movingInterpolator, DkMovingShape movingShape, PointF startPos, PointF endPos) {
		this.movingInterpolator = movingInterpolator;
		this.movingShape = movingShape;
		this.startPos = startPos;
		this.endPos = endPos;
	}

	void calcCurrentCoordinates(float fraction) {
		final float x1 = startPos.x;
		final float y1 = startPos.y;
		final float x2 = endPos.x;
		final float y2 = endPos.y;
		final float dx = x2 - x1;
		final float dy = y2 - y1;

		// Calculate current X
		curX = x1 + dx * movingInterpolator.getInterpolation(fraction);

		// Calculate current Y
		switch (movingShape) {
			case LINE: {
				curY = y1 + dy * (curX - x1) / dx;
				break;
			}
			case PARABOL_UP: {
				float x3 = (x1 + x2) / 2f;
				float y3 = Math.min(y1, y2) * 3f / 4;

				float a = ((y2 - y3) / (x2 - x3) - (y1 - y2) / (x1 - x2)) / (x3 - x1);
				float b = (y1 - y2) / (x1 - x2) - a * (x1 + x2);
				float c = y1 - x1 * (a * x1 + b);

				curY = (a * curX + b) * curX + c;
				break;
			}
			case PARABOL_DOWN: {
				float x3 = (x1 + x2) / 2f;
				float y3 = Math.max(y1, y2) * 5f / 4;

				float a = ((y2 - y3) / (x2 - x3) - (y1 - y2) / (x1 - x2)) / (x3 - x1);
				float b = (y1 - y2) / (x1 - x2) - a * (x1 + x2);
				float c = y1 - x1 * (a * x1 + b);

				curY = (a * curX + b) * curX + c;
				break;
			}
			default: {
				throw new RuntimeException("Invalid moving shape");
			}
		}
	}
}
