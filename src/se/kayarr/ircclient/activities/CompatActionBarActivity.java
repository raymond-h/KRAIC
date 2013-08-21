package se.kayarr.ircclient.activities;

import se.kayarr.ircclient.shared.ActionBarWrapper;
import se.kayarr.ircclient.shared.CompatActionBar;
import se.kayarr.ircclient.shared.CompatActionBarBase;
import se.kayarr.ircclient.shared.DeviceInfo;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;

@SuppressLint("Registered")
public class CompatActionBarActivity extends FragmentActivity {
	private CompatActionBarBase compatActionBar;
	
	public CompatActionBarBase getCompatActionBar() {
		return compatActionBar;
	}
	
	ImageButton overflowButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(!DeviceInfo.isHoneycomb(true)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			compatActionBar = new CompatActionBar(this);
		}
		else
			compatActionBar = new ActionBarWrapper(this);
	}
	
	@Override
	protected void onDestroy() {
		compatActionBar = null;
		
		super.onDestroy();
	}

	@Override
	public void setContentView(int layoutResID) {
		setContentView(getLayoutInflater().inflate(layoutResID, null, true));
	}

	@Override
	public void setContentView(View view) {
		setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		if( compatActionBar.getActionBarView() != null ) layout.addView(compatActionBar.getActionBarView());
		layout.addView(view);
		super.setContentView(layout, params);
	}

	@Override
	protected void onTitleChanged(CharSequence title, int color) {
		compatActionBar.setTitle(title);
		super.onTitleChanged(title, color);
	}
}
