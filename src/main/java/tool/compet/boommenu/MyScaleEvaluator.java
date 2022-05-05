/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.view.animation.Interpolator;

import tool.compet.animation.DkLookupTableInterpolator;

class MyScaleEvaluator {
	private final float startValue;
	private final float endValue;
	private static final Interpolator scaleInterpolator = new DkLookupTableInterpolator(new float[] {
		0.0000f, 0.0967f, 0.1870f, 0.2710f, 0.3490f, 0.4213f, 0.4880f, 0.5494f, 0.6056f, 0.6570f,
		0.7037f, 0.7460f, 0.7840f, 0.8180f, 0.8483f, 0.8750f, 0.8984f, 0.9186f, 0.9360f, 0.9507f,
		0.9630f, 0.9730f, 0.9810f, 0.9873f, 0.9920f, 0.9954f, 0.9976f, 0.9990f, 0.9997f, 1.0000f,
		1.0000f
	}); // ease cubic out

	MyScaleEvaluator(float startValue, float endValue) {
		this.startValue = startValue;
		this.endValue = endValue;
	}

	float getAnimatedValue(float fraction) {
		return startValue + scaleInterpolator.getInterpolation(fraction) * (endValue - startValue);
	}
}
