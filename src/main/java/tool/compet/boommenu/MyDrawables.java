/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.StateSet;

class MyDrawables {
	/**
	 * Create RippleDrawable or StateListDrawable circle background which can react press action.
	 */
	public static Drawable circleBackground(
		boolean useRippleEffect,
		Resources resources,
		int width, int height,
		int normalColor, int pressedColor, int unableColor) {

		if (useRippleEffect && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ColorStateList colorStateList = ColorStateList.valueOf(pressedColor);
			GradientDrawable content = circleGradientDrawable(normalColor);
			return new RippleDrawable(colorStateList, content, null);
		}
		int radius = Math.min(width >> 1, height >> 1);

		return circleStateListDrawable(resources, radius, normalColor, pressedColor, unableColor);
	}

	/**
	 * Create RippleDrawable or StateListDrawable rectangle background which can react press action.
	 */
	public static Drawable rectBackground(
		boolean useRippleEffect,
		//		Resources resources, int width, int height,
		int normalColor, int pressedColor, int unableColor,
		float cornerRadius) {

		if (useRippleEffect && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ColorStateList colorStateList = ColorStateList.valueOf(pressedColor);
			GradientDrawable content = rectGradientDrawable(normalColor, cornerRadius);

			return new RippleDrawable(colorStateList, content, null);
		}

		return rectStateListDrawable(
			//			resources, width, height,
			normalColor, pressedColor, unableColor, cornerRadius);
	}

	private static GradientDrawable circleGradientDrawable(int color) {
		GradientDrawable drawable = new GradientDrawable();
		drawable.setShape(GradientDrawable.OVAL);
		drawable.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
		drawable.setColor(color);

		return drawable;
	}

	private static GradientDrawable rectGradientDrawable(int color, float cornerRadius) {
		GradientDrawable drawable = new GradientDrawable();
		drawable.setShape(GradientDrawable.RECTANGLE);
		drawable.setColor(color);
		drawable.setCornerRadius(cornerRadius);

		return drawable;
	}

	// Other way make ripple effect for api 21-
	private static Drawable circleStateListDrawable(
		Resources resources,
		int radius,
		int normalColor, int pressedColor, int unableColor) {

		StateListDrawable drawable = new StateListDrawable();

		drawable.addState(new int[]{android.R.attr.state_pressed}, circleBitmapDrawable(resources, radius, pressedColor));
		drawable.addState(new int[]{-android.R.attr.state_enabled}, circleBitmapDrawable(resources, radius, unableColor));
		drawable.addState(StateSet.WILD_CARD, circleBitmapDrawable(resources, radius, normalColor));

		return drawable;
	}

	// Other way make ripple effect for api 21-
	static Drawable rectStateListDrawable(
		//		Resources resources,
		//		int width, int height,
		int normalColor, int pressedColor, int unableColor,
		float cornerRadius) {

		StateListDrawable drawable = new StateListDrawable();

		//		drawable.addState(new int[] {android.R.attr.state_pressed}, rectBitmapDrawable(resources, width, height, pressedColor, cornerRadius));
		//		drawable.addState(new int[] {-android.R.attr.state_enabled}, rectBitmapDrawable(resources, width, height, unableColor, cornerRadius));
		//		drawable.addState(StateSet.WILD_CARD, rectBitmapDrawable(resources, width, height, normalColor, cornerRadius));

		drawable.addState(new int[] {android.R.attr.state_pressed}, rectGradientDrawable(pressedColor, cornerRadius));
		drawable.addState(new int[] {-android.R.attr.state_enabled}, rectGradientDrawable(unableColor, cornerRadius));
		drawable.addState(StateSet.WILD_CARD, rectGradientDrawable(normalColor, cornerRadius));

		return drawable;
	}

	private static BitmapDrawable rectBitmapDrawable(
		Resources resources,
		int width, int height,
		int color,
		float cornerRadius) {

		if (width <= 0 || height <= 0) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);

		Canvas canvas = new Canvas(bitmap);
		canvas.drawRoundRect(new RectF(0, 0, width, height), cornerRadius, cornerRadius, paint);

		return new BitmapDrawable(resources, bitmap);
	}

	private static BitmapDrawable circleBitmapDrawable(Resources resources, int radius, int color) {
		if (radius <= 0) {
			return null;
		}
		int diameter = radius << 1;
		Bitmap bitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);

		Canvas canvas = new Canvas(bitmap);
		canvas.drawCircle(radius, radius, radius, paint);

		return new BitmapDrawable(resources, bitmap);
	}
}
