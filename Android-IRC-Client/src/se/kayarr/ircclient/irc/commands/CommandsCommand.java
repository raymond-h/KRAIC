package se.kayarr.ircclient.irc.commands;

import java.util.LinkedList;
import java.util.List;

import se.kayarr.ircclient.irc.CommandManager;
import se.kayarr.ircclient.irc.ServerConnection;
import android.text.TextUtils;

public class CommandsCommand extends Command {
	public CommandsCommand() {
		setHelpText(
				"Lists all commands. If you enter a pattern, " +
				"it will only list matching commands. " +
				"You can use one or more asterisk(s) (*) to match anything."
				);
		addAlias("commands");
		addAlias("cmds");
	}
	
	@Override
	public String usage() {
		return super.usage() + " {match pattern}";
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		CommandManager manager = CommandManager.getInstance();
		
		List<String> commands = new LinkedList<String>();
		for(Command command : manager.getCommands()) {
			if(params.length() == 0 || command.hasAliasMatchingPattern(params))
				commands.add(command.aliasesAsString());
		}
		
		conn.getCurrentWindow().output("Commands available" +
				(params.length() == 0 ? "" : " matching pattern \"" + params + "\"") + ": " +
				TextUtils.join(", ", commands));
	}
}
