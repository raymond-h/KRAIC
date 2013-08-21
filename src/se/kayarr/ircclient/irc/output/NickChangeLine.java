package se.kayarr.ircclient.irc.output;

import org.pircbotx.hooks.events.NickChangeEvent;

import se.kayarr.ircclient.services.ServerConnectionService;
import se.kayarr.ircclient.shared.Util;
import android.text.TextUtils;

public class NickChangeLine extends OutputLine {
	private String oldNick;
	private String newNick;
	
	public NickChangeLine(ServerConnectionService context, NickChangeEvent<?> event) {
		super(context, event.getTimestamp());
		
		oldNick = event.getOldNick();
		newNick = event.getNewNick();
	}

	@Override
	protected CharSequence outputString() {
		return TextUtils.concat(super.outputString(),
				Util.parseForSpans(
						Util.toBold(oldNick) + " changed nick to " + Util.toBold(newNick),
						
						colors()
				)
				//TODO Make it use a global format
		);
	}
}
