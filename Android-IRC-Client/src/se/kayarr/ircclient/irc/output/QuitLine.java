package se.kayarr.ircclient.irc.output;

import org.pircbotx.hooks.events.QuitEvent;

import android.text.TextUtils;

import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.shared.Util;

public class QuitLine extends OutputLine {
	private String nick;
	private String reason;

	public QuitLine(ServerConnectionService context, QuitEvent<?> event) {
		super(context);
		
		this.nick = event.getUser().getNick();
		this.reason = event.getReason();
	}

	@Override
	protected CharSequence outputString() { //TODO Make it use a global format
		return TextUtils.concat(super.outputString(), nick, " has quit",
				(reason != null && reason.length() > 0 ? Util.parseForSpans(" ( " + reason + " )", colors()) : ""));
	}
}
