package se.kayarr.ircclient.irc.output;

import org.pircbotx.hooks.events.PartEvent;

import se.kayarr.ircclient.services.ServerConnectionService;

public class PartLine extends OutputLine {
	private String channel;
	private String nick;
	private String reason;
	private boolean ownPart;
	
	public PartLine(ServerConnectionService context, PartEvent<?> event) {
		super(context);
		
		this.channel = event.getChannel().getName();
		this.nick = event.getUser().getNick();
		this.reason = event.getReason();
		this.ownPart = (event.getBot().getUserBot() == event.getUser());
	}

	@Override
	protected CharSequence outputString() { //TODO Make it use a global format
		return super.outputString() + (ownPart ? "You" : nick) + " parted " + channel +
				(reason.length() > 0 ? " (Reason: " + reason + ")" : "");
	}
}
