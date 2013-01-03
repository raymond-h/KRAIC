package se.kayarr.ircclient.irc.commands;

import org.pircbotx.Channel;

import se.kayarr.ircclient.irc.Bot;
import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.Window;

public class PartCommand extends Command {
	public PartCommand() {
		setHelpText("Leaves a channel you currently are in, optionally stating a reason.");
		addAlias("part");
		addAlias("p");
	}
	
	@Override
	public String usage() {
		return super.usage() + " [channel] {reason}";
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		String[] splitParams = params.split(" ", 3);
		
		if(splitParams.length > 0 && splitParams[0].length() > 0) {
			Channel channel = conn.getBot().getChannelCaseInsensitive(splitParams[0]);
			
			if(channel != null) {
				if(splitParams.length >= 2)
					conn.getBot().partChannel(channel, splitParams[1]);
				else
					conn.getBot().partChannel(channel);
				
				Bot bot = conn.getBot();
				Window window = bot.getConnectionContext().getWindowIgnoreCase(channel.getName());
				bot.getConnectionContext().removeWindow(window);
				
				return;
			}
			else {
				conn.getCurrentWindow().output("You're not currently in that channel."); //TODO Externalize
				return;
			}
		}
		
		printUsage(conn);
	}
}
