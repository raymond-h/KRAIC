package se.kayarr.ircclient.irc.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.pircbotx.hooks.events.JoinEvent;

import se.kayarr.ircclient.irc.Bot;
import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.Window;

public class JoinCommand extends Command {
	private Map<ServerConnection, HashSet<String>> requestedChannelJoins =
			new HashMap<ServerConnection, HashSet<String>>();
	
	public JoinCommand() {
		setHelpText("Joins a channel, optionally with a key.");
		addAlias("join");
		addAlias("j");
	}
	
	@Override
	public String usage() {
		return super.usage() + " [channel] {key}";
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		String[] splitParams = params.split(" ", 2);
		if(splitParams.length > 0 && splitParams[0].length() > 0) {
			
			HashSet<String> chans;
			if(!requestedChannelJoins.containsKey(conn)) {
				chans = new HashSet<String>();
				requestedChannelJoins.put(conn, chans);
			}
			else chans = requestedChannelJoins.get(conn);
			
			chans.add(splitParams[0]);
			
			if(splitParams.length == 2)
				conn.getBot().joinChannel(splitParams[0], splitParams[1]);
			else
				conn.getBot().joinChannel(splitParams[0]);
		}
		else {
			printUsage(conn);
		}
	}
	
	public void joinedChannel(JoinEvent<Bot> event) {
		Bot bot = event.getBot();
		
		if( event.getUser() == bot.getUserBot() && requestedChannelJoins.containsKey(bot.getConnectionContext()) ) {
			HashSet<String> chans = requestedChannelJoins.get(bot.getConnectionContext());
			chans.remove(event.getChannel().getName());
			if(chans.isEmpty()) requestedChannelJoins.remove(bot.getConnectionContext());
			
			Window window = bot.getConnectionContext().getWindowIgnoreCase(event.getChannel().getName());
			bot.getConnectionContext().setCurrentWindow(window);
		}
	}
}
