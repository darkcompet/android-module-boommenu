/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

import tool.compet.animation.DkInterpolatorFunctions;

/**
 * This class shows a menu with a lot of items (cluster), start from an anchor and end in
 * somewhere in screen with various ending-shapes.
 * You can setup cluster via 2 fields: gravity and shape. Also you can set cluster margin
 * inside board via #setClusterMargin(). For detail, see below example:
 * <pre><code>
 *    DkBoomMenu.newIns(context, anchor)
 *        .addItemBuilder(new DkTextItemBuilder()
 *            .setOnClickListener(v -> onFeedbackClick())
 *            .setIcon(R.drawable.ic_feedback)
 *            .setText(R.string.feedback)
 *            .setStyle(DkTextItemBuilder.STYLE_TEXT_RIGHT_OUT_ICON)
 *        )
 *        .addItemBuilder(new DkTextItemBuilder()
 *            .setOnClickListener(v -> onRateAppClick())
 *            .setIcon(R.drawable.ic_star)
 *            .setText(R.string.rate_app)
 *            .setStyle(DkTextItemBuilder.STYLE_TEXT_RIGHT_OUT_ICON)
 *        )
 *        .setClusterShape(DkShape.VERTICAL_LINE)
 *        .setClusterGravity(DkGravity.ANCHOR_BOTTOM_LEFT)
 *        .setClusterMargin(anchor.getWidth(), 0)
 *        .boom();
 * </code></pre>
 */
public class DkBoomMenu {
	private static final int ANIM_STATE_NOT_YET = -1;
	private static final int ANIM_STATE_WILL_ANIMATE_SOON = 1;
	private static final int ANIM_STATE_ANIMATING = 2;
	private static final int ANIM_STATE_ANIMATED = 3;

	private final Context context;
	private WeakReference<View> anchor;

	private int animState = ANIM_STATE_NOT_YET;

	private boolean enableCache = true; // for faster animation at next time
	private boolean backgroundWholeScreen = true;
	private boolean dismissImmediateOnClickOutside = true;
	private boolean dismissOnClickOutsideItem = true;
	private boolean dismissOnBackPressed = true;
	private boolean bringAnchorToFront;
	private long animStartDelay;
	private long boomDuration = 225; // ms
	private long unboomDuration = 150; // ms
	private long emissionDelayBetweenItems = 50; // ms
	private ValueAnimator animator;

	private final MyClusterManager clusterManager = new MyClusterManager();
	private boolean shouldRebuildItems = true;
	private MyBackgroundLayout background;
	private int backgroundWidth;
	private int backgroundHeight;
	private final MyBackgroundLayout.Listener backgroundListener = new MyBackgroundLayout.Listener() {
		@Override
		public void onSizeChanged(int w, int h, int oldw, int oldh) {
			backgroundWidth = w;
			backgroundHeight = h;

			if (w != oldw || h != oldh) {
				shouldRebuildItems = true;
			}
		}

		@Override
		public boolean onTranslate(float dx, float dy) {
			clusterManager.translateItems(dx, dy, backgroundWidth, backgroundHeight);
			return true;
		}

		@Override
		public boolean onClick(View v) {
			if (dismissOnClickOutsideItem) {
				unboom(dismissImmediateOnClickOutside);
				return true;
			}
			return false;
		}

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
				if (dismissOnBackPressed && (animState == ANIM_STATE_WILL_ANIMATE_SOON || animState == ANIM_STATE_ANIMATED)) {
					unboom(dismissImmediateOnClickOutside);
				}
				return true;
			}
			return false;
		}
	};

	public DkBoomMenu(Context context, View anchor) {
		this.context = context;
		this.anchor = new WeakReference<>(anchor);
	}

	public static DkBoomMenu newIns(Context context, View anchor) {
		return new DkBoomMenu(context, anchor);
	}

	/**
	 * Expanding menu with animation.
	 */
	public void boom() {
		boom(false);
	}

	/**
	 * Expanding menu without animation.
	 */
	public void show() {
		boom(true);
	}

	// Expanding (animate if not immediate) all items into background.
	public void boom(boolean immediate) {
		if (animState == ANIM_STATE_NOT_YET) {
			setupBackground();
			setupMenuItems();

			if (immediate) {
				animState = ANIM_STATE_ANIMATED;
				for (DkItem item : clusterManager.items) {
					item.show();
				}
				showAndForcusBackground();
			}
			else {
				animState = ANIM_STATE_WILL_ANIMATE_SOON;
				startAnimation(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animation) {
						animState = ANIM_STATE_ANIMATING;
						showAndForcusBackground();
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						cancelAnimationAndCleanupLayout();
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						animState = ANIM_STATE_ANIMATED;
					}
				});
			}
		}
	}

	/**
	 * Dismiss menu with animation.
	 */
	public void unboom() {
		unboom(false);
	}

	/**
	 * Close menu without animation.
	 */
	public void close() {
		unboom(true);
	}

	/**
	 * Dismiss menu with animation.
	 */
	public void unboom(boolean immediate) {
		if (animState == ANIM_STATE_WILL_ANIMATE_SOON) {
			cancelAnimationAndCleanupLayout();
			return;
		}
		if (animState == ANIM_STATE_ANIMATED) {
			if (immediate) {
				cancelAnimationAndCleanupLayout();
				return;
			}
			animState = ANIM_STATE_WILL_ANIMATE_SOON;
			reverseAnimation(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					animState = ANIM_STATE_ANIMATING;
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					cancelAnimationAndCleanupLayout();
				}
			});
		}
	}

	// region Private

	private void showAndForcusBackground() {
		final View background = this.background;
		background.setVisibility(View.VISIBLE);
		background.setFocusableInTouchMode(true);
		background.requestFocus();
	}

	private void cancelAnimationAndCleanupLayout() {
		// Cancel animation
		final ValueAnimator animator = this.animator;
		if (animator != null) {
			animator.cancel();
			animator.removeAllListeners();
			animator.removeAllUpdateListeners();
		}
		this.animState = ANIM_STATE_NOT_YET;

		// Cleanup view
		removeMenuItems();
		removeBackground();
	}

	/**
	 * Create background if not yet existed -> just add as child to ancestor without showing.
	 */
	private void setupBackground() {
		final View anchor = this.anchor.get();
		MyBackgroundLayout background = this.background;

		if (anchor == null) {
			throw new RuntimeException("Anchor is null. Please call setAnchor() or use 2nd constructor.");
		}
		if (background == null) {
			background = this.background = new MyBackgroundLayout(context);
		}

		background.setVisibility(View.GONE);
		background.listener = backgroundListener;
		ViewGroup parent = findSuitableParent(anchor);

		if (parent != null) {
			if (background.getParent() instanceof ViewGroup) {
				((ViewGroup) background.getParent()).removeView(background);
			}
			background.updateDimension(parent.getWidth(), parent.getHeight());
			parent.addView(background);
		}

		if (this.bringAnchorToFront) {
			anchor.bringToFront();
		}
	}

	/**
	 * Build items -> calculate start, end positions -> scale each item to 0 -> add to background.
	 */
	private void setupMenuItems() {
		final View anchor = this.anchor.get();
		final ViewGroup parent = findSuitableParent(anchor);

		if (anchor == null || parent == null) {
			throw new RuntimeException("Must provide anchor and parent.");
		}
		if (isShouldBuildItems()) {
			clusterManager.buildItems(context, anchor, parent, new DkItemBuilder.Callback() {
				@Override
				public void onTranslate(DkItem item, float dx, float dy) {
					clusterManager.translateItems(dx, dy, backgroundWidth, backgroundHeight);
				}

				@Override
				public void onClick(DkItem item, float x, float y) {
					if (item.dismissMenuOnClickItem) {
						unboom(item.dismissMenuImmediateOnClickItem);
					}
					if (item.onClickLisener != null) {
						item.onClickLisener.onClick(item.view, item.index);
					}
				}
			});
		}

		// Prepare start and end position for each item inside cluster
		clusterManager.calcStartEndPositions(anchor, parent);

		for (DkItem item : clusterManager.items) {
			item.hide();
			background.addView(item.view);
		}
	}

	private boolean isShouldBuildItems() {
		if (shouldRebuildItems) {
			shouldRebuildItems = false;
			return true;
		}
		return clusterManager.items.size() == 0;
	}

	private void startAnimation(AnimatorListenerAdapter listener) {
		final List<DkItem> items = clusterManager.items;
		final int itemCount = items.size();
		long totalDuration = boomDuration + emissionDelayBetweenItems * (itemCount - 1);

		for (DkItem item : items) {
			totalDuration += item.animStartDelay;
		}

		final MyBackgroundLayout background = this.background;
		background.setupAnimation(animStartDelay, totalDuration, totalDuration);
		clusterManager.setupAnimation(animStartDelay, emissionDelayBetweenItems, boomDuration, totalDuration);

		final ValueAnimator animator = this.animator = ValueAnimator.ofInt(0, 1);
		animator.setInterpolator(DkInterpolatorFunctions::linear);
		animator.setStartDelay(animStartDelay);
		animator.setDuration(totalDuration);
		animator.addListener(listener);
		animator.addUpdateListener(va -> {
			float f = va.getAnimatedFraction();

			background.onAnimationUpdate(f);

			for (DkItem item : items) {
				item.onAnimationUpdate(f);
			}
		});
		animator.start();
	}

	private void reverseAnimation(AnimatorListenerAdapter listener) {
		final List<DkItem> items = clusterManager.items;
		final int itemCount = items.size();
		long totalDuration = unboomDuration + emissionDelayBetweenItems * (itemCount - 1);

		for (DkItem item : items) {
			totalDuration += item.animStartDelay;
		}

		final MyBackgroundLayout background = this.background;
		background.setupAnimation(animStartDelay, totalDuration, totalDuration);
		clusterManager.setupAnimation(animStartDelay, emissionDelayBetweenItems, unboomDuration, totalDuration);

		final ValueAnimator animator = this.animator;
		animator.removeAllListeners();
		animator.removeAllUpdateListeners();
		animator.setDuration(totalDuration);
		animator.addListener(listener);
		animator.addUpdateListener(va -> {
			float f = va.getAnimatedFraction();

			background.onAnimationUpdate(f);

			for (DkItem item : items) {
				item.onAnimationUpdate(f);
			}
		});
		animator.reverse();
	}

	private void removeMenuItems() {
		final MyBackgroundLayout background = this.background;
		if (background != null) {
			background.removeAllViews();
		}
		if (! enableCache) {
			clusterManager.items.clear();
		}
	}

	private void removeBackground() {
		final MyBackgroundLayout background = this.background;

		if (background != null) {
			ViewParent parent = background.getParent();

			if (parent instanceof ViewGroup) {
				((ViewGroup) parent).removeView(background);
			}
		}

		if (! enableCache) {
			this.background = null;
		}
	}

	@Nullable
	private ViewGroup findSuitableParent(View view) {
		if (view == null) {
			return null;
		}
		if (backgroundWholeScreen) {
			return MyUtils.findSuperFrameLayout(view);
		}

		ViewParent parent = view.getParent();

		return parent instanceof ViewGroup ? (ViewGroup) parent : null;
	}

	// endregion Private

	// region Get/Set

	public DkBoomMenu addItemBuilder(DkItemBuilder itemBuilder) {
		clusterManager.itemBuilders.add(itemBuilder);
		return this;
	}

	public DkBoomMenu setBackgroundDimColor(int dimColor) {
		background.dimColor = dimColor;
		return this;
	}

	public DkBoomMenu setBackgroundWholeScreen(boolean wholeScreen) {
		this.backgroundWholeScreen = wholeScreen;
		return this;
	}

	public DkBoomMenu setDismissOnClickOutsideItem(boolean dismiss, boolean dismissImmediate) {
		this.dismissOnClickOutsideItem = dismiss;
		this.dismissImmediateOnClickOutside = dismissImmediate;
		return this;
	}

	public DkBoomMenu setDismissOnBackPressed(boolean dismiss, boolean dismissImmediate) {
		this.dismissOnBackPressed = dismiss;
		this.dismissImmediateOnClickOutside = dismissImmediate;
		return this;
	}

	public DkBoomMenu setDismissOnBackPressed(boolean dismiss) {
		this.dismissOnBackPressed = dismiss;
		return this;
	}

	public DkBoomMenu setDismissImmediateOnClickOutside(boolean dismissImmediateOnClickOutside) {
		this.dismissImmediateOnClickOutside = dismissImmediateOnClickOutside;
		return this;
	}

	public DkBoomMenu setAnchor(View anchor) {
		this.anchor = new WeakReference<>(anchor);
		return this;
	}

	public DkBoomMenu setBoomDuration(long boomDuration) {
		this.boomDuration = boomDuration;
		return this;
	}

	public DkBoomMenu setUnboomDuration(long unboomDuration) {
		this.unboomDuration = Math.max(0, unboomDuration);
		return this;
	}

	public DkBoomMenu setBoomStartDelay(long animStartDelay) {
		this.animStartDelay = Math.max(0L, animStartDelay);
		return this;
	}

	public DkBoomMenu setClusterOffset(int left, int top, int right, int bottom) {
		return setClusterOffset(new Rect(left, top, right, bottom));
	}

	/**
	 * Set offset of cluster (items) inside board after boomed.
	 */
	public DkBoomMenu setClusterOffset(Rect offset) {
		clusterManager.offset = offset;
		return this;
	}

	/**
	 * Set gravity (position) of cluster (items) after boomed.
	 */
	public DkBoomMenu setClusterGravity(DkClusterGravity gravity) {
		clusterManager.gravity = gravity;
		return this;
	}

	/**
	 * Set shape (arrange) of cluster (items) after boomed.
	 */
	public DkBoomMenu setClusterShape(DkClusterShape shape) {
		clusterManager.shape = shape;
		return this;
	}

	public DkBoomMenu setEmissionDelayBetweenItems(long emissionDelay) {
		this.emissionDelayBetweenItems = emissionDelay;
		return this;
	}

	public DkBoomMenu setItemsEmissionOrder(DkEmissionOrder order) {
		clusterManager.emissionOrder = order;
		return this;
	}

	public DkBoomMenu setBringAnchorToFront(boolean bringAnchorToFront) {
		this.bringAnchorToFront = bringAnchorToFront;
		return this;
	}

	public DkBoomMenu setItemsPositionCalculator(DkPositionCalculator calculator) {
		clusterManager.calculator = calculator;
		return this;
	}

	public DkBoomMenu setRandomItemsStartPosition(boolean randomStartPosition) {
		clusterManager.randomStartPosition = randomStartPosition;
		return this;
	}

	public DkBoomMenu setAutoScaleClusterIfOversize(boolean autoScale) {
		clusterManager.autoScaleIfOversize = autoScale;
		return this;
	}

	public DkBoomMenu setAllowClusterOutsideBoard(boolean allow) {
		clusterManager.allowOutsideBoard = allow;
		return this;
	}

	public DkBoomMenu setAutoUnisize(boolean autoUnisize) {
		clusterManager.autoUnisize = autoUnisize;
		return this;
	}

	public DkBoomMenu setCacheOptimization(boolean enable) {
		this.enableCache = enable;
		return this;
	}

	public DkBoomMenu setDismissOnClickOutsideItem(boolean dismissOnClickOutsideItem) {
		this.dismissOnClickOutsideItem = dismissOnClickOutsideItem;
		return this;
	}

	// endregion Get/Set

	// region Setting for cluster items

	public DkBoomMenu setItemClickListener(DkOnItemClickListener onItemClickListener) {
		clusterManager.onItemClickListener = onItemClickListener;
		return this;
	}

	public DkBoomMenu setItemDismissOnBackPressed(boolean dismiss) {
		clusterManager.itemDismissOnBackPressed = dismiss;
		return this;
	}

	public DkBoomMenu setItemDismissImmediate(boolean immediate) {
		clusterManager.itemDismissImmediate = immediate;
		return this;
	}

	public DkBoomMenu setItemRotation(boolean enable) {
		clusterManager.itemEnableRotation = enable;
		return this;
	}

	public DkBoomMenu setItemMargin(int dp) {
		clusterManager.itemMargin = dp;
		return this;
	}

	// endregion Setting for cluster items
}
