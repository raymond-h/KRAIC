package se.kayarr.ircclient.irc.commands;

import se.kayarr.ircclient.irc.ServerConnection;

public class QuitCommand extends Command {
	public QuitCommand() {
		setHelpText("Quits the current server, with an optional quit message.");
		addAlias("quit");
		addAlias("q");
	}

	@Override
	public String usage() {
		return super.usage() + " {message}";
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		if(params.length() > 0)
			conn.disconnect(params);
		else
			conn.disconnect();
	}

}
