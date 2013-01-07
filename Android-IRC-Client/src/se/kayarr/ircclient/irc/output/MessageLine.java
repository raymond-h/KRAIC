package se.kayarr.ircclient.irc.output;

import org.pircbotx.Colors;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import se.kayarr.ircclient.shared.Util;
import android.content.Context;
import android.text.SpannableStringBuilder;

public class MessageLine extends OutputLine {
	private String channel;
	private String nick;
	private String message;
	
	public MessageLine(Context context, MessageEvent<?> event) {
		this(context, event.getTimestamp(), event.getChannel().getName(),
				event.getUser().getNick(), event.getMessage());
	}
	
	public MessageLine(Context context, PrivateMessageEvent<?> event) {
		this(context, event.getTimestamp(), null, event.getUser().getNick(), event.getMessage());
	}

	public MessageLine(Context context, String channel, String nick, String message) {
		super(context);
		this.channel = channel;
		this.nick = nick;
		this.message = message;
	}
	
	public MessageLine(Context context, long timestamp, String channel, String nick, String message) {
		super(context, timestamp);
		this.channel = channel;
		this.nick = nick;
		this.message = message;
	}

	protected CharSequence outputString() {
		return new SpannableStringBuilder(super.outputString())
			.append(Util.parseForSpans(getContext(), Colors.BOLD + nick + Colors.BOLD + " :  " + message)); //TODO Make it use a global format
	}
}
