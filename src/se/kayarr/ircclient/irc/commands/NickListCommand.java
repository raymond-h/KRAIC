package se.kayarr.ircclient.irc.commands;

import java.util.Set;

import org.pircbotx.User;

import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.Window;

public class NickListCommand extends Command
{
	
	public NickListCommand()
	{
		setHelpText("Displays all the nicknames of all users in the current channel.");
		addAlias("nicklist");
		
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params)
	{
		Window window = conn.getCurrentWindow();
		Set<User> users = window.getChannel().getUsers();
		StringBuilder output = new StringBuilder();
		for(User u : users)
		{
			if(output.length() != 0)
				output.append(", ");
			output.append(u.getNick());
		}
		window.output(output.toString());
	}
	

}
