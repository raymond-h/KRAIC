package se.kayarr.ircclient.irc.output;

import org.pircbotx.Colors;
import org.pircbotx.hooks.events.ActionEvent;

import se.kayarr.ircclient.shared.Util;
import android.content.Context;
import android.text.SpannableStringBuilder;

public class ActionLine extends OutputLine {
	private String channel;
	private String nick;
	private String action;

	public ActionLine(Context context, ActionEvent<?> event) {
		this(context, event.getTimestamp(), (event.getChannel() != null) ? event.getChannel().getName() : null,
				event.getUser().getNick(), event.getAction());
	}

	public ActionLine(Context context, String channel, String nick, String action) {
		super(context);
		this.channel = channel;
		this.nick = nick;
		this.action = action;
	}
	
	public ActionLine(Context context, long timestamp, String channel, String nick, String action) {
		super(context, timestamp);
		this.channel = channel;
		this.nick = nick;
		this.action = action;
	}
	
	@Override
	protected CharSequence outputString() {
		return new SpannableStringBuilder(super.outputString())
			.append(Util.parseForSpans(getContext(), "* " + Colors.BOLD + nick + Colors.BOLD + " " + action)); //TODO Make it use a global format
	}
}
