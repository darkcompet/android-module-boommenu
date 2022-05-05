/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.min;
import static java.lang.Math.sin;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Random;

import tool.compet.view.DkViews;
import tool.compet.core.DkMaths;

class MyPositionCalculator implements DkPositionCalculator {
	private final boolean randomStartPosition;
	private final boolean autoScaleIfOversize;
	private final Random random = new Random();

	MyPositionCalculator(boolean randomStartPosition, boolean autoScaleIfOversize) {
		this.randomStartPosition = randomStartPosition;
		this.autoScaleIfOversize = autoScaleIfOversize;
	}

	/**
	 * @return Cluster bounds inside the board.
	 */
	@Override
	public RectF calcStartEndPositions(List<DkItem> items, DkClusterGravity gravity, DkClusterShape shape, Rect offset, ViewGroup board, View anchor) {
		// Calculate cluster size and also update items position inside cluster
		float[] clusterSize = calcClusterSizeAndUpdateItemsEndPositionInCluster(items, shape);

		// Calculate anchor-bounds inside board
		final Rect anchorBounds = DkViews.calcDescendantBoundsAtAncestorCoords(board, anchor);

		// Scale down each item to make cluster fit inside the board
		if (autoScaleIfOversize) {
			float scaleFactor = calcScaleFactor(clusterSize, board);

			// Cluster size > board: Update size and margin of each item in cluster
			if (scaleFactor > 1f) {
				// Update items size and margin
				for (DkItem item : items) {
					item.width = (int) (item.width / scaleFactor);
					item.height = (int) (item.height / scaleFactor);
					item.margin = (int) (item.margin / scaleFactor);
				}

				// Re-calculate cluser size
				clusterSize = calcClusterSizeAndUpdateItemsEndPositionInCluster(items, shape);
			}
		}

		// Update start, end positions of each item in board (flatten items in board)
		float[] clusterOffset = flattenClusterIntoBoard(
			items, clusterSize, gravity, offset,
			board, anchorBounds
		);

		// Cluster bounds inside board
		return new RectF(clusterOffset[0], clusterOffset[1], clusterOffset[0] + clusterSize[0], clusterOffset[1] + clusterSize[1]);
	}

	private float calcScaleFactor(float[] clusterSize, ViewGroup board) {
		float scaleFactor = 1.0f;
		final float clusterWidth = clusterSize[0];
		final float clusterHeight = clusterSize[1];
		final float boardWidth = board.getWidth();
		final float boardHeight = board.getHeight();

		// We wanna get scale-factor greater than 1.0f
		if (clusterWidth > boardWidth) {
			scaleFactor = clusterWidth / boardWidth;
		}
		if (clusterHeight > boardHeight) {
			scaleFactor = Math.max(scaleFactor, clusterHeight / boardHeight);
		}

		return scaleFactor;
	}

	/**
	 * @return Cluster dimension [width, height]
	 */
	private float[] calcClusterSizeAndUpdateItemsEndPositionInCluster(List<DkItem> items, DkClusterShape shape) {
		final int itemCount = items.size();

		switch (shape) {
			case HORIZONTAL_LINE: {
				int curLeft = 0;
				int maxHeight = 0;

				for (DkItem item : items) {
					curLeft += item.margin;

					item.endPos.x = curLeft;
					item.endPos.y = item.margin;

					curLeft += item.width + item.margin;
					maxHeight = Math.max(maxHeight, item.height + (item.margin << 1));
				}

				return new float[] {curLeft, maxHeight};
			}
			case VERTICAL_LINE: {
				int maxWidth = 0;
				int curTop = 0;

				for (DkItem item : items) {
					curTop += item.margin;

					item.endPos.x = item.margin;
					item.endPos.y = curTop;

					curTop += item.height + item.margin;
					maxWidth = Math.max(maxWidth, item.width + (item.margin << 1));
				}

				return new float[] {maxWidth, curTop};
			}
			case CIRCLE:
			case CIRCLE_AND_CENTER: {
				if (itemCount <= 1) {
					DkItem center = items.get(0);
					float maxItemRadius = (float) (hypot(center.width, center.height) / 2);
					float maxMargin = center.margin;

					// Radius of outer-circle which bounds item
					float R2 = maxItemRadius + maxMargin;
					final float clusterSize = 2.0f * R2;

					// Calculate end-position in bounds
					center.endPos.x = R2 - center.margin;
					center.endPos.y = R2 - center.margin;

					return new float[] {clusterSize, clusterSize};
				}

				// For beauty, we consider bounds of items as a Square
				final boolean hasCenter = (shape == DkClusterShape.CIRCLE_AND_CENTER);
				// Radius of inner-circle which through all items center
				float R1 = 0;
				// Radius of outer-circle which bounds all items, of course R2 > R1
				float R2;

				// We consider each item as a circle
				float[] item_radius = new float[itemCount];
				float maxItemRadius = 0;
				float maxMargin = 0;
				int biggestIndex = 0;

				// Estimate max-margin, max-item-radius...
				for (int index = 0; index < itemCount; ++index) {
					DkItem item = items.get(index);
					item_radius[index] = estimateItemRadius(item);

					if (item_radius[index] + item.margin >= maxMargin + maxItemRadius) {
						if (hasCenter && index > 0) {
							biggestIndex = index;
						}
					}

					maxMargin = Math.max(maxMargin, item.margin);
					maxItemRadius = Math.max(maxItemRadius, item_radius[index]);
				}

				// In bounds: calculate inner circle radius R1
				float teta = 360f / (hasCenter ? itemCount - 1 : itemCount);
				float tmp = (float) Math.sqrt(2 * (1 - DkMaths.cos(teta)));

				for (int index = hasCenter ? 1 : 0; index < itemCount; ++index) {
					int next = index + 1;

					if (next == itemCount) {
						next = hasCenter ? 1 : 0;
					}
					DkItem it1 = items.get(index);
					DkItem it2 = items.get(next);
					R1 = Math.max(R1, (item_radius[index] + item_radius[next] + it1.margin + it2.margin) / tmp);
				}
				// try to make R1 bigger if has center (note that, centerIndex = 0)
				if (hasCenter) {
					DkItem center = items.get(0);
					DkItem biggest = items.get(biggestIndex);

					R1 += Math.max(0, item_radius[biggestIndex] + biggest.margin + item_radius[0] + center.margin - R1);
				}

				// In bounds: calculate outer circle radius R2
				R2 = R1 + maxItemRadius + maxMargin;
				final float clusterSize = 2.0f * R2;

				// Calculate end-position in bounds
				final float angle = hasCenter ? 360f / (itemCount - 1) : 360f / itemCount;

				if (hasCenter) {
					DkItem center = items.get(0);
					center.endPos.x = R2 - (center.width >> 1);
					center.endPos.y = R2 - (center.height >> 1);
				}

				for (int index = hasCenter ? 1 : 0; index < itemCount; ++index) {
					DkItem item = items.get(index);
					float degrees = index * angle;
					item.endPos.x = (float) (R2 + R1 * DkMaths.sin(degrees) - (item.width >> 1));
					item.endPos.y = (float) (R2 - R1 * DkMaths.cos(degrees) - (item.height >> 1));
				}

				return new float[] {clusterSize, clusterSize};
			}
			case GRID_2_COLS:
			case GRID_3_COLS:
			case GRID_4_COLS:
			case GRID_5_COLS:
			case GRID_6_COLS:
			case GRID_7_COLS:
			case GRID_8_COLS:
			case GRID_9_COLS:
			case GRID_10_COLS: {
				int col = shape.getVal();
				int row = itemCount / col;

				if (col * row < itemCount) {
					++row;
				}

				int maxWidth = 0;
				int curTop = 0;

				for (int r = 0; r < row; ++r) {
					int curLeft = 0;
					int maxHeight = 0;

					for (int c = 0; c < col; ++c) {
						int i = r * col + c;

						if (i >= itemCount) {
							continue;
						}

						DkItem item = items.get(i);
						curLeft += item.margin;

						item.endPos.x = curLeft;
						item.endPos.y = curTop + item.margin;

						curLeft += item.width + item.margin;
						maxHeight = Math.max(maxHeight, item.height + (item.margin << 1));
					}

					maxWidth = Math.max(maxWidth, curLeft);
					curTop += maxHeight;
				}

				return new float[] {maxWidth, curTop};
			}
			case QUARTER_LEFT:
			case QUARTER_RIGHT:
			case QUARTER_TOP:
			case QUARTER_BOTTOM:
			case QUARTER_LEFT_TOP:
			case QUARTER_TOP_RIGHT:
			case QUARTER_RIGHT_BOTTOM:
			case QUARTER_BOTTOM_LEFT: {
				final DkItem mock = items.get(0);

				if (itemCount == 1) {
					mock.endPos.x = mock.endPos.y = mock.margin;
					return new float[] {mock.width + (mock.margin << 1), mock.height + (mock.margin << 1)};
				}

				// To make life easer, we suppose all item has same radius and margin
				final double teta = Math.PI / (1 + (itemCount << 1));
				final double half_teta = teta / 2;
				final double base_teta = (Math.PI / 2 - itemCount * teta) / 2;
				final float item_radius = estimateItemRadius(mock);

				// Center to center
				float R_cc = (float) (4 * (mock.margin + itemCount * (item_radius + mock.margin)) / Math.PI);
				float clusterWidth = R_cc + item_radius + mock.margin;
				float clusterHeight = R_cc + item_radius + mock.margin;

				double angle = base_teta;
				double bx = 0;
				double by = 0;
				double fx = R_cc;
				double fy = R_cc;
				boolean xsin = true;
				boolean ycos = true;

				// Code-volume optimization for end-position calculation at next step
				if (shape == DkClusterShape.QUARTER_LEFT) {
					clusterHeight = (float) (clusterWidth * Math.sqrt(2));
					angle += Math.PI / 4;
					bx = clusterWidth;
					by = clusterHeight / 2;
					fx = -R_cc;
				}
				else if (shape == DkClusterShape.QUARTER_RIGHT) {
					clusterHeight = (float) (clusterWidth * Math.sqrt(2));
					angle += Math.PI / 4;
					by = clusterHeight / 2;
					fy = -R_cc;
				}
				else if (shape == DkClusterShape.QUARTER_TOP) {
					clusterWidth = (float) (clusterHeight * Math.sqrt(2));
					angle += Math.PI / 4;
					bx = clusterWidth / 2;
					by = clusterHeight;
					fx = -R_cc;
					fy = -R_cc;
					xsin = ycos = false;
				}
				else if (shape == DkClusterShape.QUARTER_BOTTOM) {
					clusterWidth = (float) (clusterHeight * Math.sqrt(2));
					angle += Math.PI / 4;
					bx = clusterWidth / 2;
					xsin = ycos = false;
				}
				else if (shape == DkClusterShape.QUARTER_LEFT_TOP) {
					bx = clusterWidth;
					by = clusterHeight;
					fx = -R_cc;
					fy = -R_cc;
					xsin = ycos = false;
				}
				else if (shape == DkClusterShape.QUARTER_TOP_RIGHT) {
					by = clusterHeight;
					fy = -R_cc;
				}
				else if (shape == DkClusterShape.QUARTER_RIGHT_BOTTOM) {
					xsin = ycos = false;
				}
				else if (shape == DkClusterShape.QUARTER_BOTTOM_LEFT) {
					bx = clusterWidth;
					fx = -R_cc;
				}
				else {
					throw new RuntimeException("Invalid shape");
				}

				// Calculate end-position for each item
				for (DkItem item : items) {
					double curAngle = angle + half_teta;

					item.endPos.x = (float) (bx + fx * (xsin ? sin(curAngle) : cos(curAngle)) - (item.width >> 1));
					item.endPos.y = (float) (by + fy * (ycos ? cos(curAngle) : sin(curAngle)) - (item.height >> 1));

					angle += teta;
				}

				return new float[] {clusterWidth, clusterHeight};
			}
			default: {
				throw new RuntimeException("Invalid shape: " + shape);
			}
		}
	}

	private float estimateItemRadius(DkItem item) {
		int half_w = item.width >> 1;
		int half_h = item.height >> 1;

		return (float) ((min(half_w, half_h) + hypot(half_w, half_h)) / 2);
	}

	/**
	 * @return Array [left, top] of cluster in board.
	 */
	private float[] flattenClusterIntoBoard(
		// Cluster dimension, position, offset in board
		List<DkItem> items, float[] clusterSize,
		// Request from caller
		DkClusterGravity gravity, Rect offset,
		// Board and anchor
		ViewGroup board, Rect anchorBounds) {

		// First, calculate end-position in bounds
		float[] clusterOffset = calcClusterOffsetInBoard(offset, clusterSize, gravity, board, anchorBounds);
		final float clusterLeft = offset.left + clusterOffset[0];
		final float clusterTop = offset.top + clusterOffset[1];

		// Finally, calculate start position and end positions in board
		final int anchorHalfWidth = anchorBounds.width() >> 1;
		final int anchorHalfHeight = anchorBounds.height() >> 1;
		final int anchorCx = anchorBounds.centerX();
		final int anchorCy = anchorBounds.centerY();

		for (DkItem item : items) {
			// Put item-center at anchor-center when start
			item.startPos.x = anchorCx - (item.width >> 1);
			item.startPos.y = anchorCy - (item.height >> 1);

			// Translate item end-position by cluster offset
			item.endPos.x += clusterLeft;
			item.endPos.y += clusterTop;

			if (randomStartPosition) {
				// Allow only translation inside the anchor
				int translate_max_dx = (int) (anchorHalfWidth - item.startScaleFactor * item.width / 2f);
				int translate_max_dy = (int) (anchorHalfHeight - item.startScaleFactor * item.height / 2f);

				int random_x = this.random.nextInt(Math.max(1, translate_max_dx));
				int random_y = this.random.nextInt(Math.max(1, translate_max_dy));

				item.startPos.x += this.random.nextBoolean() ? random_x : -random_x;
				item.startPos.y += this.random.nextBoolean() ? random_y : -random_y;
			}
		}

		return new float[] {clusterLeft, clusterTop};
	}

	private float[] calcClusterOffsetInBoard(Rect offset, float[] clusterSize, DkClusterGravity gravity, ViewGroup board, Rect anchorBounds) {
		float clusterLeft = offset.left;
		float clusterTop = offset.top;

		if (gravity == DkClusterGravity.CENTER) {
			clusterLeft += (board.getWidth() - clusterSize[0]) / 2f;
			clusterTop += (board.getHeight() - clusterSize[1]) / 2f;
		}
		else if (gravity == DkClusterGravity.CENTER_TOP) {
			clusterLeft += (board.getWidth() - clusterSize[0]) / 2f;
		}
		else if (gravity == DkClusterGravity.CENTER_BOTTOM) {
			clusterLeft += (board.getWidth() - clusterSize[0]) / 2f;
			clusterTop += board.getHeight() - clusterSize[1];
		}
		else if (gravity == DkClusterGravity.CENTER_LEFT) {
			clusterTop += (board.getHeight() - clusterSize[1]) / 2f;
		}
		else if (gravity == DkClusterGravity.CENTER_RIGHT) {
			clusterLeft += board.getWidth() - clusterSize[0];
			clusterTop += (board.getHeight() - clusterSize[1]) / 2f;
		}
		else if (gravity == DkClusterGravity.LEFT_TOP) {
			// Don't need to calculate
		}
		else if (gravity == DkClusterGravity.TOP_RIGHT) {
			clusterLeft += board.getWidth() - clusterSize[0];
		}
		else if (gravity == DkClusterGravity.BOTTOM_LEFT) {
			clusterTop += board.getHeight() - clusterSize[1];
		}
		else if (gravity == DkClusterGravity.RIGHT_BOTTOM) {
			clusterLeft += board.getWidth() - clusterSize[0];
			clusterTop += board.getHeight() - clusterSize[1];
		}
		else if (gravity == DkClusterGravity.ANCHOR_LEFT) {
			clusterLeft += anchorBounds.left - clusterSize[0];
			clusterTop += anchorBounds.top + (anchorBounds.height() - clusterSize[1]) / 2f;
		}
		else if (gravity == DkClusterGravity.ANCHOR_RIGHT) {
			clusterLeft += anchorBounds.left + anchorBounds.width();
			clusterTop += anchorBounds.top + (anchorBounds.height() - clusterSize[1]) / 2f;
		}
		else if (gravity == DkClusterGravity.ANCHOR_TOP) {
			clusterLeft += anchorBounds.left + (anchorBounds.width() - clusterSize[0]) / 2f;
			clusterTop += anchorBounds.top - clusterSize[1];
		}
		else if (gravity == DkClusterGravity.ANCHOR_BOTTOM) {
			clusterLeft += anchorBounds.left + (anchorBounds.width() - clusterSize[0]) / 2f;
			clusterTop += anchorBounds.top + anchorBounds.height();
		}
		else if (gravity == DkClusterGravity.ANCHOR_LEFT_TOP) {
			clusterLeft += anchorBounds.left - clusterSize[0];
			clusterTop += anchorBounds.top - clusterSize[1];
		}
		else if (gravity == DkClusterGravity.ANCHOR_TOP_RIGHT) {
			clusterLeft += anchorBounds.left + anchorBounds.width();
			clusterTop += anchorBounds.top - clusterSize[1];
		}
		else if (gravity == DkClusterGravity.ANCHOR_RIGHT_BOTTOM) {
			clusterLeft += anchorBounds.left + anchorBounds.width();
			clusterTop += anchorBounds.top + clusterSize[1];
		}
		else if (gravity == DkClusterGravity.ANCHOR_BOTTOM_LEFT) {
			clusterLeft += anchorBounds.left - clusterSize[0];
			clusterTop += anchorBounds.top + anchorBounds.height();
		}
		else if (gravity == DkClusterGravity.ANCHOR_LEFT_TOP_CENTERED) {
			clusterLeft += anchorBounds.centerX() - clusterSize[0];
			clusterTop += anchorBounds.centerY() - clusterSize[1];
		}
		else if (gravity == DkClusterGravity.ANCHOR_TOP_RIGHT_CENTERED) {
			clusterLeft += anchorBounds.centerX();
			clusterTop += anchorBounds.top - clusterSize[1];
		}
		else if (gravity == DkClusterGravity.ANCHOR_RIGHT_BOTTOM_CENTERED) {
			clusterLeft += anchorBounds.centerX();
			clusterTop += anchorBounds.centerY();
		}
		else if (gravity == DkClusterGravity.ANCHOR_BOTTOM_LEFT_CENTERED) {
			clusterLeft += anchorBounds.centerX() - clusterSize[0];
			clusterTop += anchorBounds.centerY();
		}
		else {
			throw new RuntimeException("Invalid gravity: " + gravity);
		}

		return new float[] {clusterLeft, clusterTop};
	}
}
