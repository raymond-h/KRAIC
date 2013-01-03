package se.kayarr.ircclient.shared;

import android.text.InputFilter;
import android.text.Spanned;

public class NumberRangeInputFilter implements InputFilter {
	private int min;
	private int max;
	
	public NumberRangeInputFilter(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public CharSequence filter(CharSequence source, int start, int end,
			Spanned dest, int dstart, int dend) {
		StringBuffer buffer = new StringBuffer(dest);
		buffer.replace(dstart, dend, source.toString().substring(start, end));
		
		int num;
		try {
			num = Integer.parseInt(buffer.toString());
		}
		catch(NumberFormatException e) {
			return "";
		}
		
		if(num < min || num > max) {
			return "";
		}
		
		return null;
	}
}
