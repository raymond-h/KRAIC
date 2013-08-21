package se.kayarr.ircclient.irc.commands;

import se.kayarr.ircclient.exceptions.InvalidWindowTypeException;
import se.kayarr.ircclient.irc.ServerConnection;

public class MeCommand extends Command {
	public MeCommand() {
		setHelpText("Sends an action to the current channel.");
		addAlias("me");
	}
	
	@Override
	public String usage() {
		return super.usage() + " [action]";
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		try {
			conn.getCurrentWindow().sendAction(params);
		}
		catch (InvalidWindowTypeException e) {}
	}

}
