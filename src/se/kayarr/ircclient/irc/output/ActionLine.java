package se.kayarr.ircclient.irc.output;

import org.pircbotx.Colors;
import org.pircbotx.hooks.events.ActionEvent;

import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.shared.Util;
import android.text.TextUtils;

public class ActionLine extends OutputLine {
	private String channel;
	private String nick;
	private String action;

	public ActionLine(ServerConnectionService context, ActionEvent<?> event) {
		this(context, event.getTimestamp(), (event.getChannel() != null) ? event.getChannel().getName() : null,
				event.getUser().getNick(), event.getAction());
	}

	public ActionLine(ServerConnectionService context, String channel, String nick, String action) {
		super(context);
		this.channel = channel;
		this.nick = nick;
		this.action = action;
	}
	
	public ActionLine(ServerConnectionService context, long timestamp, String channel, String nick, String action) {
		super(context, timestamp);
		this.channel = channel;
		this.nick = nick;
		this.action = action;
	}
	
	@Override
	protected CharSequence outputString() { //TODO Make it use a global format
		return TextUtils.concat(super.outputString(),
				Util.parseForSpans(Colors.BOLD + nick + Colors.BOLD + " " + action, colors())
		);
	}
}
