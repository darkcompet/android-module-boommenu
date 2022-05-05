/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.core.view.animation.PathInterpolatorCompat;

import tool.compet.core.DkConfig;

/**
 * Base ItemBuilder.
 *
 * @param <T> Subclass type.
 */
@SuppressWarnings("unchecked")
public abstract class DkItemBuilder<T> {
	protected abstract DkItemView getView(Context context);

	interface Callback {
		void onTranslate(DkItem item, float dx, float dy);

		void onClick(DkItem item, float x, float y);
	}

	// Info for item
	protected int index;
	protected int width;
	protected int height;
	protected int margin = Integer.MIN_VALUE;
	protected long animStartDelay;
	protected Boolean enableRotation = null;
	protected Boolean enable3DAnimation = null;
	protected Boolean enableScale = null;
	// up -> over top -> down
	protected static Interpolator movingInterpolator = PathInterpolatorCompat.create(0.48f, 1.47f, 0.91f, 1.06f);
	protected DkMovingShape movingShape = DkMovingShape.LINE;
	protected Boolean dismissMenuOnClickItem = null;
	protected Boolean dismissMenuImmediateOnClickItem = null;
	// extra basic info for view
	protected boolean isCircleShape;
	protected float cornerRadius = Integer.MIN_VALUE;
	protected boolean useRippleEffect = true;
	protected int normalColor = Color.TRANSPARENT;
	protected int pressedColor = Color.TRANSPARENT;
	protected int unableColor = Color.TRANSPARENT;
	// extra info for view animation
	protected float startRotationDegrees = 0f;
	protected float endRotationDegrees = 360f;
	protected float startScaleFactor = -1f;
	protected float endScaleFactor = 1f;

	// Building process
	private float widthPercent;
	private float heightPercent;
	private boolean ratioBaseOnWidth = true;
	private float widthRatio;
	private float heightRatio;
	private float scaleX;
	private float scaleY;

	// Package private infor from cluster manager
	int anchorWidth;
	int anchorHeight;
	int boardWidth;
	int boardHeight;
	DkOnItemClickListener onClickListener;

	private static final MyColorGenerator colorGenerator = new MyColorGenerator();

	/**
	 * Build item from info of this item-builder.
	 */
	protected DkItem build(Context context, Callback callback) {
		DkItem item = new DkItem();
		DkItemView view = getView(context);

		// Validate and initialize
		if (view == null) {
			throw new RuntimeException("Must provide item view at `getView()`");
		}
		if (isCircleShape) { // auto-fix ratio if its shape is circle
			widthRatio = heightRatio = 1f;
		}

		// Assign info to item first, maybe need re-assign later
		item.index = index;
		item.view = view;
		item.onClickLisener = onClickListener;
		item.margin = margin;
		item.animStartDelay = animStartDelay;
		item.enableRotation = enableRotation;
		item.enable3DAnimation = enable3DAnimation;
		item.enableScale = enableScale;
		item.movingInterpolator = movingInterpolator;
		item.movingShape = movingShape;
		item.startRotationDegrees = startRotationDegrees;
		item.endRotationDegrees = endRotationDegrees;
		item.startScaleFactor = startScaleFactor;
		item.endScaleFactor = endScaleFactor;
		item.dismissMenuOnClickItem = dismissMenuOnClickItem;
		item.dismissMenuImmediateOnClickItem = dismissMenuImmediateOnClickItem;

		// Setup internal detector listener
		view.gestureDetector.setListener(new MyGestureDetector.Listener() {
			@Override
			public boolean onTranslate(float dx, float dy) {
				callback.onTranslate(item, dx, dy);
				return false;
			}

			@Override
			public boolean onClick(float rawX, float rawY) {
				callback.onClick(item, rawX, rawY);
				return false;
			}
		});

		// Update dimension
		if (width <= 0 || height <= 0) { // Measure view dimension if unspecific
			view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			width = view.getMeasuredWidth();
			height = view.getMeasuredHeight();
		}
		// Calculate dimension by percent
		if (widthPercent > 0f) {
			width = (int) (widthPercent * boardWidth);
		}
		if (heightPercent > 0f) {
			height = (int) (heightPercent * boardHeight);
		}
		// Re-calculate dimension by ratio
		if (widthRatio > 0f && heightRatio > 0f) {
			if (ratioBaseOnWidth) {
				height = (int) (heightRatio * width / widthRatio);
			}
			else {
				width = (int) (widthRatio * height / heightRatio);
			}
		}
		// Re-calculate dimension if scaling
		if (scaleX > 0f) {
			width *= scaleX;
		}
		if (scaleY > 0f) {
			height *= scaleY;
		}
		// Auto fix start scaleFactor if over anchor size (default make item equals to 1/8 size of anchor)
		final float startWidth = anchorWidth >> 3;
		final float startHeight = anchorHeight >> 3;
		// choose suitable start scale factor
		if (startScaleFactor == -1f || startScaleFactor * width > startWidth || startScaleFactor * height > startHeight) {
			item.startScaleFactor = Math.min(startWidth / width, startHeight / height);
		}

		// Ok, assign final dimension to item
		item.width = width;
		item.height = height;

		// Finally, update view dimension
		item.updateViewDimension(width, height);

		return item;
	}

	/**
	 * This method should be called inside subclass.
	 */
	protected <V extends DkItemView> V initView(Context context, int layoutRes) {
		DkItemView view = (DkItemView) View.inflate(context, layoutRes, null);

		// This is time to need build some info for item view
		if (cornerRadius == Integer.MIN_VALUE) {
			cornerRadius = DkConfig.dp2px(16);
		}
		if (normalColor == Color.TRANSPARENT) {
			normalColor = colorGenerator.nextNormalColor();
		}
		if (pressedColor == Color.TRANSPARENT) {
			pressedColor = colorGenerator.getPressedColor(normalColor);
		}
		if (unableColor == Color.TRANSPARENT) {
			unableColor = colorGenerator.getUnableColor(normalColor);
		}

		view.isCircleShape = isCircleShape;
		view.cornerRadius = cornerRadius;
		view.useRippleEffect = useRippleEffect;
		view.normalColor = normalColor;
		view.pressedColor = pressedColor;
		view.unableColor = unableColor;

		return (V) view;
	}

	// region Setup

	public T setOnClickListener(DkOnItemClickListener onClickListener) {
		this.onClickListener = onClickListener;
		return (T) this;
	}

	public T setDimension(int width, int height) {
		this.width = width;
		this.height = height;
		return (T) this;
	}

	// Percent size on board
	public T setPercent(float widthPercent, float heightPercent) {
		this.widthPercent = widthPercent;
		this.heightPercent = heightPercent;
		return (T) this;
	}

	// Width : Height rate, for eg,. 3:4
	public T setDimensionRatio(boolean baseOnWidth, float widthRatio, float heightRatio) {
		this.ratioBaseOnWidth = baseOnWidth;
		this.widthRatio = widthRatio;
		this.heightRatio = heightRatio;
		return (T) this;
	}

	public T setScale(float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		return (T) this;
	}

	public T setMovingInterpolator(Interpolator movingInterpolator) {
		this.movingInterpolator = movingInterpolator;
		return (T) this;
	}

	public T setMovingShape(DkMovingShape movingShape) {
		this.movingShape = movingShape;
		return (T) this;
	}

	public T setMargin(int dp) {
		this.margin = DkConfig.dp2px(dp);
		return (T) this;
	}

	public T setEnableRotationAnimation(boolean enable) {
		this.enableRotation = enable;
		return (T) this;
	}

	public T setEnable3DAnimation(boolean enable) {
		this.enable3DAnimation = enable;
		return (T) this;
	}

	public T setEnableScaleAnimation(boolean enable) {
		this.enableScale = enable;
		return (T) this;
	}

	public T setAnimStartDelay(long animStartDelay) {
		this.animStartDelay = animStartDelay;
		return (T) this;
	}

	public T setAnimationScaleFactor(float startScaleFactor, float endScaleFactor) {
		this.startScaleFactor = startScaleFactor;
		this.endScaleFactor = endScaleFactor;
		return (T) this;
	}

	public T setAnimationRotationDegrees(float startRotationDegrees, float endRotationDegrees) {
		this.startRotationDegrees = startRotationDegrees;
		this.endRotationDegrees = endRotationDegrees;
		return (T) this;
	}

	public T setCornerRadius(float cornerRadius) {
		this.cornerRadius = cornerRadius;
		return (T) this;
	}

	public T setCircleShape(boolean circleShape) {
		isCircleShape = circleShape;
		return (T) this;
	}

	public T setUseRippleEffect(boolean useRippleEffect) {
		this.useRippleEffect = useRippleEffect;
		return (T) this;
	}

	public T setNormalColor(int normalColor) {
		this.normalColor = normalColor;
		return (T) this;
	}

	public T setPressedColor(int pressedColor) {
		this.pressedColor = pressedColor;
		return (T) this;
	}

	public T setUnableColor(int unableColor) {
		this.unableColor = unableColor;
		return (T) this;
	}

	public T setDismissMenuOnClickItem(boolean dismissMenu) {
		this.dismissMenuOnClickItem = dismissMenu;
		return (T) this;
	}

	public T setDismissMenuImmediate(boolean dismissImmediate) {
		this.dismissMenuImmediateOnClickItem = dismissImmediate;
		return (T) this;
	}

	// endregion Setup
}
