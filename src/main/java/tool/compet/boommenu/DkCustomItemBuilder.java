/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.content.Context;

import tool.compet.core.DkUtils;

/**
 * Item builder for custom view. Your view must be subclass of DkBaseItemView and don't forget
 * call setters to setup view properties like: color, radius, useRippleEffect...
 */
public class DkCustomItemBuilder extends DkItemBuilder<DkCustomItemBuilder> {
	protected int layoutRes;
	protected DkItemView view;

	public DkCustomItemBuilder() {
	}

	@Override
	protected DkItemView getView(Context context) {
		if (view == null) {
			view = super.initView(context, layoutRes);

			if (view == null) {
				DkUtils.complainAt(this, "Must specify view or layoutRes of DkBaseItemView");
			}
		}

		return view;
	}

	public DkCustomItemBuilder setView(DkItemView view) {
		this.view = view;
		return this;
	}

	public DkCustomItemBuilder setView(int layoutRes) {
		this.layoutRes = layoutRes;
		return this;
	}
}
