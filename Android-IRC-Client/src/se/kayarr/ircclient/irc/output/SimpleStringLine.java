package se.kayarr.ircclient.irc.output;

import java.util.Date;

import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.shared.Util;
import android.text.TextUtils;

public class SimpleStringLine extends OutputLine {
	private String text;
	
	public SimpleStringLine(ServerConnectionService context, String text) {
		super(context);
		this.text = text;
	}
	
	public SimpleStringLine(ServerConnectionService context, Date time, String text) {
		super(context, time);
		this.text = text;
	}
	
	@Override
	protected CharSequence outputString() {
		return TextUtils.concat(super.outputString(),
				Util.parseForSpans(text, colors())
		);
	}
}
