package se.kayarr.ircclient.shared;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.ViewGroup;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ActionBarWrapper extends CompatActionBarBase {
	private ActionBar actionBar;
	
	public ActionBar getWrappedActionBar() {
		return actionBar;
	}
	
	public ActionBarWrapper(Activity context) {
		super(context);
		actionBar = context.getActionBar();
	}

	@Override
	public ViewGroup getActionBarView() {
		return null;
	}

	@Override
	public void setTitle(int resId) {
		actionBar.setTitle(resId);
	}

	@Override
	public void setTitle(CharSequence title) {
		actionBar.setTitle(title);
	}

	@Override
	public CharSequence getTitle() {
		return actionBar.getTitle();
	}

	@Override
	public void setSubtitle(int resId) {
		actionBar.setSubtitle(resId);
	}

	@Override
	public void setSubtitle(CharSequence title) {
		actionBar.setSubtitle(title);
	}

	@Override
	public CharSequence getSubtitle() {
		return actionBar.getSubtitle();
	}

	@Override
	public void setIcon(int resId) {
		actionBar.setIcon(resId);
	}

	@Override
	public void setIcon(Drawable icon) {
		actionBar.setIcon(icon);
	}

	@Override
	public boolean isShowing() {
		return actionBar.isShowing();
	}

	@Override
	public void show() {
		actionBar.show();
	}

	@Override
	public void hide() {
		actionBar.hide();
	}

}
