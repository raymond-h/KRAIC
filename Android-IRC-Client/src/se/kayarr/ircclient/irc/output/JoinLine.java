package se.kayarr.ircclient.irc.output;

import org.pircbotx.hooks.events.JoinEvent;

import android.content.Context;

public class JoinLine extends OutputLine {
	private String channel;
	private String nick;
	private boolean ownJoin;
	
	public JoinLine(Context context, JoinEvent<?> event) {
		super(context);
		
		this.channel = event.getChannel().getName();
		this.nick = event.getUser().getNick();
		this.ownJoin = (event.getUser() == event.getBot().getUserBot());
	}

	@Override
	protected CharSequence outputString() { //TODO Make it use a global format
		if(ownJoin)
			return super.outputString() + "Joined channel " + channel;
		
		else
			return super.outputString() + nick + " has joined channel " + channel;
	}
}
