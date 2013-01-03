package se.kayarr.ircclient.shared;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

public abstract class CompatActionBarBase {
	private Activity context;
	
	public CompatActionBarBase(Activity context) {
		this.context = context;
	}
	
	public Activity getContext() {
		return context;
	}
	
	public abstract ViewGroup getActionBarView();
	
	//TITLE CODE
	public abstract void setTitle(int resId);
	
	public abstract void setTitle(CharSequence title);
	
	public abstract CharSequence getTitle();
	
	
	//SUBTITLE CODE
	public abstract void setSubtitle(int resId);
	
	public abstract void setSubtitle(CharSequence title);
	
	public abstract CharSequence getSubtitle();
	
	//ICON CODE
	public abstract void setIcon(int resId);
	
	public abstract void setIcon(Drawable icon);
	
	//VISIBILITY CODE
	public abstract boolean isShowing();
	
	public abstract void show();
	
	public abstract void hide();
	
	/**
	 * Convenience method with a default implementation calling 
	 * {@code show()} or {@code hide()} depending on the passed value.
	 * 
	 * @author Raymond
	 * @param show Whether to show the Action Bar or not
	 */
	public void setShowing(boolean show) {
		if(show) show();
		else hide();
	}
}
