package se.kayarr.ircclient.irc.output;

import org.pircbotx.Colors;
import org.pircbotx.hooks.events.NoticeEvent;

import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.shared.Util;
import android.text.TextUtils;

public class NoticeLine extends OutputLine {
	private boolean wasReceived = false;
	private String sender;
	private String channel;
	private String message;
	
	public NoticeLine(ServerConnectionService context, NoticeEvent<?> event) {
		super( context, event.getTimestamp() );
		
		sender = event.getUser().getNick();
		channel = (event.getChannel() != null) ? event.getChannel().getName() : null;
		message = event.getMessage();
		wasReceived = true;
	}

	public NoticeLine(ServerConnectionService context, String sender, String channel, String message) {
		super(context);
		this.sender = sender;
		this.channel = channel;
		this.message = message;
	}

	@Override
	protected CharSequence outputString() { //TODO Make it use a global format
		return TextUtils.concat(super.outputString(),
				(wasReceived ? "-> " : ""),
				
				Util.parseForSpans("-" + Colors.BOLD + sender + Colors.BOLD + "- " + message, colors())
		);
	}
}
