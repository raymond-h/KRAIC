package se.kayarr.ircclient.irc.output;

import java.util.Date;

import se.kayarr.ircclient.shared.Util;

import android.content.Context;
import android.text.SpannableStringBuilder;

public class SimpleStringLine extends OutputLine {
	private String text;
	
	public SimpleStringLine(Context context, String text) {
		super(context);
		this.text = text;
	}
	
	public SimpleStringLine(Context context, Date time, String text) {
		super(context, time);
		this.text = text;
	}
	
	@Override
	protected CharSequence outputString() {
		return new SpannableStringBuilder(super.outputString())
				.append(Util.parseForSpans(getContext(), text));
	}
}
