/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tool.compet.core.DkUtils;

/**
 * This class, manages for cluster (items).
 */
class MyClusterManager {
	// Emission order for each item
	DkEmissionOrder emissionOrder = DkEmissionOrder.RANDOM;

	// Cluster gravity (for calculating cluster position in board)
	DkClusterGravity gravity = DkClusterGravity.ANCHOR_BOTTOM_LEFT_CENTERED;

	// Cluster shape
	DkClusterShape shape = DkClusterShape.VERTICAL_LINE;

	// Items and builders
	List<DkItemBuilder<?>> itemBuilders = new ArrayList<>();
	List<DkItem> items = new ArrayList<>();

	// Offset (margin) of cluster
	Rect offset = new Rect();

	// In case bounds of cluster oversize of board, scale-down will be applied
	boolean autoScaleIfOversize = true;

	// Auto make all items same size with biggest item
	boolean autoUnisize = true;

	// Allow whether or not this cluster can go outside of board
	boolean allowOutsideBoard;

	// Indicates whether each item should be started with random position inside the anchor
	boolean randomStartPosition = true;

	// To calculate start, end position for items
	DkPositionCalculator calculator;

	// Bounds of cluster in board
	private RectF bounds;

	// region GLOBAL setting (apply to all items if not set)

	DkOnItemClickListener onItemClickListener; // on click to each item
	boolean itemEnableRotation; // rotate while animating
	boolean itemEnable3DAnimation; // 3D animation while animating
	boolean itemEnableScale = true; // scale while animating
	boolean itemDismissMenuOnClickItem = true; // dismiss boom menu when click item itself
	boolean itemDismissMenuImmediateOnClickItem = true; // true: dismiss menu immediate, false: animate unboom
	int itemMargin = 4; // default 4dp (call item.setMargin() to convert to pixel)
	boolean itemDismissOnBackPressed = true;
	boolean itemDismissImmediate;

	// endregion GLOBAL setting (apply to all items if not set)

	void buildItems(Context context, View anchor, ViewGroup board, DkItemBuilder.Callback callback) {
		int anchorWidth = anchor.getWidth();
		int anchorHeight = anchor.getHeight();
		int boardWidth = board.getWidth();
		int boardHeight = board.getHeight();
		int N = itemBuilders.size();

		if (N == 0) {
			DkUtils.complainAt(this, "No item to build");
		}

		items.clear();

		int maxWidth = -1;
		int maxHeight = -1;

		// Build each item from global setting
		for (int index = 0; index < N; ++index) {
			DkItemBuilder itemBuilder = itemBuilders.get(index);
			itemBuilder.index = index;
			itemBuilder.anchorWidth = anchorWidth;
			itemBuilder.anchorHeight = anchorHeight;
			itemBuilder.boardWidth = boardWidth;
			itemBuilder.boardHeight = boardHeight;

			// If some fields were not set by user, then assign GLOBAL setting (maybe default) to them
			if (itemBuilder.onClickListener == null) {
				itemBuilder.setOnClickListener(onItemClickListener);
			}
			if (itemBuilder.enableRotation == null) {
				itemBuilder.setEnableRotationAnimation(itemEnableRotation);
			}
			if (itemBuilder.enable3DAnimation == null) {
				itemBuilder.setEnable3DAnimation(itemEnable3DAnimation);
			}
			if (itemBuilder.enableScale == null) {
				itemBuilder.setEnableScaleAnimation(itemEnableScale);
			}
			if (itemBuilder.dismissMenuOnClickItem == null) {
				itemBuilder.setDismissMenuOnClickItem(itemDismissMenuOnClickItem);
			}
			if (itemBuilder.dismissMenuImmediateOnClickItem == null) {
				itemBuilder.setDismissMenuImmediate(itemDismissMenuImmediateOnClickItem);
			}
			if (itemBuilder.margin == Integer.MIN_VALUE) {
				itemBuilder.setMargin(itemMargin);
			}

			DkItem item = itemBuilder.build(context, callback);
			items.add(item);

			if (autoUnisize) {
				if (maxWidth < item.width) {
					maxWidth = item.width;
				}
				if (maxHeight < item.height) {
					maxHeight = item.height;
				}
			}
		}

		if (autoUnisize) {
			for (DkItem item : items) {
				item.updateViewDimension(maxWidth, maxHeight);
			}
		}
	}

	void setupAnimation(long animStartDelay, long emissionDelayBetweenItems, long animDuration, long totalDuration) {
		final int N = items.size();

		switch (emissionOrder) {
			case NATURAL: {
				for (int i = 0; i < N; ++i) {
					items.get(i).setupAnimation(
						animStartDelay + emissionDelayBetweenItems * i,
						animDuration,
						totalDuration
					);
				}
				break;
			}
			case RESERVE: {
				for (int i = 0, lastIndex = N - 1; i < N; ++i) {
					items.get(i).setupAnimation(
						animStartDelay + emissionDelayBetweenItems * (lastIndex - i),
						animDuration,
						totalDuration
					);
				}
				break;
			}
			case RANDOM: {
				int[] indices = new int[N];

				for (int i = 0; i < N; ++i) {
					indices[i] = i;
				}

				final Random random = new Random();
				for (int index = 0, cnt = N; index < N; ++index) {
					int nextIndex = random.nextInt(cnt);
					int itemIndex = indices[nextIndex];

					indices[nextIndex] = indices[--cnt];

					items.get(itemIndex).setupAnimation(
						animStartDelay + emissionDelayBetweenItems * index,
						animDuration,
						totalDuration
					);
				}
				break;
			}
			default: {
				DkUtils.complainAt(this, "Invalid emission type");
			}
		}
	}

	void calcStartEndPositions(View anchor, ViewGroup board) {
		if (calculator == null) {
			calculator = new MyPositionCalculator(randomStartPosition, autoScaleIfOversize);
		}
		bounds = calculator.calcStartEndPositions(items, gravity, shape, offset, board, anchor);
	}

	void translateItems(float dx, float dy, int bw, int bh) {
		boolean okx = false;
		boolean oky = false;

		if (allowOutsideBoard) {
			okx = oky = true;
		}
		else {
			if (bounds.left + dx >= 0 && bounds.right + dx <= bw) {
				okx = true;
				bounds.left += dx;
				bounds.right += dx;
			}
			if (bounds.top + dy >= 0 && bounds.bottom + dy <= bh) {
				oky = true;
				bounds.top += dy;
				bounds.bottom += dy;
			}
		}

		for (DkItem item : items) {
			if (okx) {
				item.view.setX(item.endPos.x += dx);
			}
			if (oky) {
				item.view.setY(item.endPos.y += dy);
			}
		}
	}
}
