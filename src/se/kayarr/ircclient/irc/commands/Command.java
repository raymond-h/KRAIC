package se.kayarr.ircclient.irc.commands;

import java.util.Set;
import java.util.TreeSet;

import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.shared.Util;
import android.text.TextUtils;

public abstract class Command {
	private Set<String> aliases = new TreeSet<String>();
	
	private String helpText = "No help text is defined for this command";
	
	public void addAlias(String alias) {
		aliases.add(alias);
	}
	
	public void removeAlias(String alias) {
		aliases.remove(alias);
	}
	
	public boolean hasAlias(String alias) {
		return aliases.contains(alias);
	}
	
	public boolean hasAliasMatchingPattern(String pattern) {
		for(String alias : aliases) {
			if(Util.matches(pattern, alias)) return true;
		}
		
		return false;
	}
	
	public Set<String> getAliases() {
		return aliases;
	}
	
	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}
	
	public String getHelpText() {
		return helpText;
	}
	
	public abstract void execute(ServerConnection conn, String alias, String params);
	
	public String aliasesAsString() {
		if(aliases.size() == 1) return "/" + (String)aliases.toArray()[0];
		return "/[" + TextUtils.join(" | ", aliases) + "]";
	}
	
	public String toString() {
		return getClass().getSimpleName() + " {" + usage() + "}";
	}
	
	/**
	 * Utility method for printing the contents of {@link #usage()} to the current {@link Window}
	 * for {@code conn}.
	 * 
	 * @param conn The {@link ServerConnection} to get the current window from
	 */
	public void printUsage(ServerConnection conn) {
		conn.getCurrentWindow().output("Usage: " + usage()); //TODO Externalize
	}
	
	/**
	 * A method that should be overridden to provide a string telling the user
	 * how to use the command. The implementation should call the super implementation
	 * and add to the returned string (if the command takes any parameters).
	 * 
	 * @return The string describing how this command is used
	 */
	public String usage() {
		return aliasesAsString();
	}
}
