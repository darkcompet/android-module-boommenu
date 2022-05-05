/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

/**
 * Indicate the way how to arrange gravity of rectangle which bounds items (cluster).
 * Eg, for gravity ANCHOR_LEFT_TOP, the bottom-right point of cluster-rectangle will be
 * moved to left-top point of the anchor.
 */
public enum DkClusterGravity {
	ANCHOR_LEFT, // right of cluster will be at left of anchor
	ANCHOR_RIGHT, // left of cluster will be at right of anchor
	ANCHOR_TOP, // bottom of cluster will be at top of anchor
	ANCHOR_BOTTOM, // top  of cluster will be at bottom of anchor
	ANCHOR_LEFT_TOP, // right-bottom of cluster will be at left-top of anchor
	ANCHOR_TOP_RIGHT, // left-bottom of cluster will be at top-right of anchor
	ANCHOR_RIGHT_BOTTOM, // left-top of cluster will be at right-bottom of anchor
	ANCHOR_BOTTOM_LEFT, // top-right of cluster will be at left-bottom of anchor
	ANCHOR_LEFT_TOP_CENTERED, // right-bottom of cluster will be at anchor
	ANCHOR_TOP_RIGHT_CENTERED, // left-bottom of clusterm will be at anchor
	ANCHOR_RIGHT_BOTTOM_CENTERED, // left-top of cluster will be at anchor
	ANCHOR_BOTTOM_LEFT_CENTERED, // top-right of cluster will be at anchor

	CENTER, // center of cluster will be at center of board
	CENTER_TOP, // bottom-center of cluster will be at center of board
	CENTER_BOTTOM, // top-center of cluster will be at center of board
	CENTER_LEFT, // right-center of cluster will be at center of board
	CENTER_RIGHT, // left-center of cluster will be at center of board

	LEFT_TOP, // left-top of cluster will be at left-top of board
	TOP_RIGHT, // top-right of cluster will be at top-right of board
	BOTTOM_LEFT, // left-bottom of cluster will be at left-bottom of board
	RIGHT_BOTTOM, // right-bottom of cluster will be at right-bottom of board
}
