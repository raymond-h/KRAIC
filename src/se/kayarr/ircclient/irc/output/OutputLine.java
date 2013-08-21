package se.kayarr.ircclient.irc.output;

import java.util.Date;

import lombok.Getter;
import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.shared.Settings;
import se.kayarr.ircclient.shared.Util;
import android.util.SparseIntArray;

public class OutputLine {
	@Getter private ServerConnectionService context;
	private Date time;
	
	private boolean showTimestamp = true;
	
	private CharSequence cachedText;
	
	public OutputLine(ServerConnectionService context) {
		this(context, new Date());
	}
	
	public OutputLine(ServerConnectionService context, long ms) {
		this(context, new Date(ms));
	}
	
	public OutputLine(ServerConnectionService context, Date time) {
		this.context = context;
		this.time = time;
	}

	protected CharSequence outputString() {
//		return timestamp();
		return "";
	}
	
	public String timestamp() {
		return timestamp(time);
	}
	
	public String timestamp(Date t) {
		return ( Settings.getInstance(context).isUsingTimestamps() ?
			Util.formatTimestamp(context, t) + " " :
			"" );
	}
	
	public Date getTimestamp() {
		return time;
	}
	
	public boolean getShowTimestamp() {
		return showTimestamp;
	}
	
	public void setShowTimestamp(boolean showTimestamp) {
		this.showTimestamp = showTimestamp;
	}
	
	public CharSequence getOutput() {
		if(cachedText == null || Settings.getInstance(context).outputFormatsChanged()) cachedText = outputString();
		return cachedText;
	}
	
	protected SparseIntArray colors() {
		return context.getColors();
	}
}
