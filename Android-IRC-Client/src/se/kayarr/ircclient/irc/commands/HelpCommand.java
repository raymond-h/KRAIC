package se.kayarr.ircclient.irc.commands;

import se.kayarr.ircclient.irc.CommandManager;
import se.kayarr.ircclient.irc.ServerConnection;

public class HelpCommand extends Command {
	public HelpCommand() {
		setHelpText(
				"Displays the usage and help message of a specified command. " +
				"For usage, [pointy brackets] specifies required parameters, and " +
				"{curly brackets} specifies optional ones. " +
				"A line ( | ) inbetween any parts in brackets means that " +
				"you can use any one of them.");
		addAlias("help");
	}
	
	@Override
	public String usage() {
		return super.usage() + " [command]";
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		if(params.length() == 0) {
			printUsage(conn);
			return;
		}
		if(params.startsWith("/")) params = params.substring(1);
		
		CommandManager manager = CommandManager.getInstance();
		for(Command command : manager.getCommands()) {
			if(command.hasAlias(params)) {
				conn.getCurrentWindow().output(command.usage() + ": " + command.getHelpText());
				return;
			}
		}
		
		conn.getCurrentWindow().output("Unable to find command \"" + params + "\"");
	}
}
