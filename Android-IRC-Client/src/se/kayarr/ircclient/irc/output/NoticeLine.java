package se.kayarr.ircclient.irc.output;

import org.pircbotx.hooks.events.NoticeEvent;

import se.kayarr.ircclient.shared.Util;

import android.content.Context;
import android.text.SpannableStringBuilder;

public class NoticeLine extends OutputLine {
	private boolean wasReceived = false;
	private String sender;
	private String channel;
	private String message;
	
	public NoticeLine(Context context, NoticeEvent<?> event) {
		super( context, event.getTimestamp() );
		
		sender = event.getUser().getNick();
		channel = (event.getChannel() != null) ? event.getChannel().getName() : null;
		message = event.getMessage();
		wasReceived = true;
	}

	public NoticeLine(Context context, String sender, String channel, String message) {
		super(context);
		this.sender = sender;
		this.channel = channel;
		this.message = message;
	}

	@Override
	protected CharSequence outputString() {
		return new SpannableStringBuilder(super.outputString())
				.append((wasReceived ? "-> " : "") + "-" + sender + "- ")
				.append(Util.parseForSpans(getContext(), message)); //TODO Make it use a global format
	}
}
