/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.graphics.Color;

import java.util.Random;

import tool.compet.core.DkIntArrayList;

class MyColorGenerator {
	private final int[] availableColors = new int[72];
	private final Random random = new Random();

	MyColorGenerator() {
		float[] hsl = new float[] {0f, 1f, 0.5f};

		for (int index = availableColors.length - 1; index >= 0; --index) {
			hsl[0] = (index << 2) + index;
			hsl[1] = (index & 1) == 0 ? 0.2f : 0.8f;

			availableColors[index] = Color.HSVToColor(hsl);
		}
	}

	private final DkIntArrayList usableColors = new DkIntArrayList(availableColors.length);

	private DkIntArrayList acquireUsableColors() {
		DkIntArrayList usableColors = this.usableColors;
		if (usableColors.size() == 0) {
			usableColors.addAll(availableColors);
		}
		return usableColors;
	}

	public int nextNormalColor() {
		DkIntArrayList usableColors = acquireUsableColors();
		final int colorCount = usableColors.size();

		int nextIndex = this.random.nextInt(colorCount);
		int color = usableColors.get(nextIndex);
		int lastIndex = colorCount - 1;

		// Fast remove next color
		usableColors.set(nextIndex, usableColors.get(lastIndex));
		usableColors.remove(lastIndex);

		return color;
	}

	public int getPressedColor(int normalColor) {
		if (normalColor == Color.TRANSPARENT) {
			normalColor = nextNormalColor();
		}

		float[] hsv = new float[3];
		Color.colorToHSV(normalColor, hsv);

		hsv[2] = 0.2f;

		return Color.HSVToColor(hsv);
	}

	public int getUnableColor(int normalColor) {
		if (normalColor == Color.TRANSPARENT) {
			normalColor = nextNormalColor();
		}

		float[] hsv = new float[3];
		Color.colorToHSV(normalColor, hsv);

		hsv[2] = 0.8f;

		return Color.HSVToColor(hsv);
	}
}
