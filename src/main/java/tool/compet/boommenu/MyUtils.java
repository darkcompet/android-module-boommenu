/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

class MyUtils {
	/**
	 * Scan up to find a layout like FrameLayout which can make layer of views.
	 * This try to find Android root layout as possible.
	 *
	 * @return Nomarly, it results Root FrameLayout of current Activity.
	 */
	@Nullable
	static ViewGroup findSuperFrameLayout(View view) {
		ViewGroup layout = null;
		ViewGroup fallback = null;
		ViewParent viewParent = view.getParent();

		if (viewParent instanceof ViewGroup) {
			layout = (ViewGroup) viewParent;
		}

		do {
			if (layout instanceof FrameLayout) {
				if (layout.getId() == android.R.id.content) {
					return layout;
				}
				fallback = layout;
			}
			if (layout != null) {
				ViewParent parent = layout.getParent();
				layout = (parent instanceof ViewGroup) ? (ViewGroup) parent : null;
			}
		}
		while (layout != null);

		return fallback;
	}
}
