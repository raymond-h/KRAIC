package se.kayarr.ircclient.irc.commands;

import se.kayarr.ircclient.irc.ServerConnection;

public class ClearCommand extends Command {
	public ClearCommand() {
		setHelpText("Clears all lines from the current window.");
		addAlias("clear");
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		conn.getCurrentWindow().clearOutput();
	}
}
