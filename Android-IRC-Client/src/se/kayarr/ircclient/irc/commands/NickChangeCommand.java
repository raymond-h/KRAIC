package se.kayarr.ircclient.irc.commands;

import se.kayarr.ircclient.irc.ServerConnection;

public class NickChangeCommand extends Command {
	
	public NickChangeCommand() {
		setHelpText("Changes your current nick");
		addAlias("nick");
	}
	
	@Override
	public String usage() {
		return super.usage() + " [new nick]";
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		conn.changeNick(params);
	}

}
