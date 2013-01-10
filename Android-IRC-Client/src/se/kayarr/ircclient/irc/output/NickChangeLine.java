package se.kayarr.ircclient.irc.output;

import org.pircbotx.hooks.events.NickChangeEvent;

import se.kayarr.ircclient.services.ServerConnectionService;

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
		return super.outputString() + oldNick + " changed nick to " + newNick; //TODO Make it use a global format
	}
}
