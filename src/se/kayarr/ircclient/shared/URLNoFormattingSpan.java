package se.kayarr.ircclient.shared;

import android.text.TextPaint;
import android.text.style.URLSpan;

public class URLNoFormattingSpan extends URLSpan {
	//private int color = 0;
	
	public URLNoFormattingSpan(String url) {
		super(url);
		
		/*Random r = new Random();
		color = Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256));*/
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		/*ds.setTextScaleX(2.9f); //Nutty stuff
		ds.setTextSkewX(1.5f);
		ds.setTextSize(17.0f);
		ds.setColor(color);*/
	}
}
