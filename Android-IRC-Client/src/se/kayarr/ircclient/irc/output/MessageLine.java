package se.kayarr.ircclient.irc.output;

import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.shared.Util;
import android.text.TextUtils;

public class MessageLine extends OutputLine {
	private String channel;
	private String nick;
	private String message;
	
	public MessageLine(ServerConnectionService context, MessageEvent<?> event) {
		this(context, event.getTimestamp(), event.getChannel().getName(),
				event.getUser().getNick(), event.getMessage());
	}
	
	public MessageLine(ServerConnectionService context, PrivateMessageEvent<?> event) {
		this(context, event.getTimestamp(), null, event.getUser().getNick(), event.getMessage());
	}

	public MessageLine(ServerConnectionService context, String channel, String nick, String message) {
		super(context);
		this.channel = channel;
		this.nick = nick;
		this.message = message;
	}
	
	public MessageLine(ServerConnectionService context, long timestamp, String channel, String nick, String message) {
		super(context, timestamp);
		this.channel = channel;
		this.nick = nick;
		this.message = message;
	}

	protected CharSequence outputString() { //TODO Make it use a global format
		return TextUtils.concat(super.outputString(),
				Util.parseForSpans( Colors.BOLD + nick + Colors.BOLD + " :  " + message, colors() )
		);
	}
}
