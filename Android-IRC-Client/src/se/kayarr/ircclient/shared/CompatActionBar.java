package se.kayarr.ircclient.shared;

import se.kayarr.ircclient.R;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class CompatActionBar extends CompatActionBarBase {
	private ViewGroup actionBarViewGroup;
	
	private ImageButton homeIcon;
	private TextView title;
	private TextView subtitle;
	
	private ImageButton menuButton;
	
	public CompatActionBar(Activity context) {
		super(context);
		
		actionBarViewGroup = (ViewGroup) context.getLayoutInflater().inflate(R.layout.prehc_actionbar, null);
		
		homeIcon = (ImageButton) actionBarViewGroup.findViewById(R.id.home_button);
		
		title = (TextView) actionBarViewGroup.findViewById(R.id.activity_title);
		subtitle = (TextView) actionBarViewGroup.findViewById(R.id.activity_subtitle);
		
		menuButton = (ImageButton) actionBarViewGroup.findViewById(R.id.menu_button);
		menuButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getContext().openOptionsMenu();
			}
		});
		
		setSubtitle(null);
	}
	
	public ViewGroup getActionBarView() {
		return actionBarViewGroup;
	}
	
	//TITLE CODE
	public void setTitle(int resId) {
		setTitle(getContext().getText(resId));
	}
	
	public void setTitle(CharSequence title) {
		this.title.setText(title);
	}
	
	public CharSequence getTitle() {
		return title.getText().toString();
	}
	
	
	//SUBTITLE CODE
	public void setSubtitle(int resId) {
		setSubtitle(getContext().getText(resId));
	}
	
	public void setSubtitle(CharSequence title) {
		if(title == null) subtitle.setVisibility(View.GONE);
		else {
			subtitle.setVisibility(View.VISIBLE);
			subtitle.setText(title);
		}
	}
	
	public CharSequence getSubtitle() {
		return subtitle.getText().toString();
	}
	
	//ICON CODE
	public void setIcon(int resId) {
		setIcon(getContext().getResources().getDrawable(resId));
	}
	
	public void setIcon(Drawable icon) {
		homeIcon.setImageDrawable(icon);
	}
	
	//VISIBILITY CODE
	public boolean isShowing() {
		return actionBarViewGroup.isShown();
	}
	
	public void show() {
		if(!actionBarViewGroup.isShown()) actionBarViewGroup.setVisibility(View.VISIBLE);
	}
	
	public void hide() {
		if(actionBarViewGroup.isShown()) actionBarViewGroup.setVisibility(View.GONE);
	}
}
