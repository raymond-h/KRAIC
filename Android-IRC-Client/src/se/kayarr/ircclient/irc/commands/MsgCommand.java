package se.kayarr.ircclient.irc.commands;

import se.kayarr.ircclient.irc.ServerConnection;

public class MsgCommand extends Command {
	public MsgCommand() {
		setHelpText("Sends a message to any target you enter (can be a channel or a nick).");
		addAlias("msg");
	}
	
	@Override
	public String usage() {
		return super.usage() + " [target] [message]";
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		String[] splitParams = params.split(" ", 2);
		
		if(splitParams.length == 2) {
			conn.getBot().sendMessage(splitParams[0], splitParams[1]);
		}
		else {
			printUsage(conn);
		}
	}
}
