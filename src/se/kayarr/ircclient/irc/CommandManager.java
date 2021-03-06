package se.kayarr.ircclient.irc;

import java.util.LinkedList;
import java.util.List;

import se.kayarr.ircclient.irc.commands.*;

public class CommandManager {
	private static CommandManager instance = new CommandManager();
	
	public static CommandManager getInstance() { return instance; }
	
	
	private List<Command> commands = new LinkedList<Command>();
	
	public CommandManager() {
		addCommand(new CommandsCommand());
		addCommand(new HelpCommand());
		addCommand(new LineDumpCommand());		
		addCommand(new MsgCommand());
		addCommand(new MeCommand());
		addCommand(new JoinCommand());
		addCommand(new PartCommand());
		addCommand(new QuitCommand());
		addCommand(new NickChangeCommand());
		addCommand(new ClearCommand());
		addCommand(new WhoisCommand());
		addCommand(new NickListCommand());
	}
	
	public void addCommand(Command command) {
		commands.add(command);
	}
	
	public void removeCommand(Command command) {
		commands.remove(command);
	}
	
	public List<Command> getCommands() {
		return commands;
	}
	
	public boolean executeCommand(ServerConnection conn, String alias, String params) {
		boolean hasExecuted = false;
		
		for(Command command : commands) {
			if(command.hasAlias(alias)) {
				command.execute(conn, alias, params);
				hasExecuted = true;
			}
		}
		
		return hasExecuted;
	}
}
