package se.kayarr.ircclient.irc.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.pircbotx.hooks.events.ServerResponseEvent;
import org.pircbotx.hooks.events.WhoisEvent;

import se.kayarr.ircclient.irc.Bot;
import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.Window;
import android.text.TextUtils;

public class WhoisCommand extends Command {
	public WhoisCommand() {
		setHelpText("Prints Whois information about a user from the server. STILL A WIP"); //Externalize
		addAlias("whois");
	}
	
	public String usage() {
		return super.usage() + " [user]";
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		if(params.length() > 0) {
			conn.getBot().sendRawLine("WHOIS " + TextUtils.join(",", params.split(" ", 2)));
		}
		else printUsage(conn);
	}
	
	public void whoisServerReply(WhoisEvent<Bot> event) {
		Window window = event.getBot().getConnectionContext().getCurrentWindow();
		
		window.output("* " + event.getNick() + " is "
				+ event.getLogin() + " @ " + event.getHostname() + " ( " + event.getRealname() + " )");
		
		List<String> channels = event.getChannels();
		if(channels != null) {
			window.output("* " + event.getNick() + " is in " + TextUtils.join(", ", channels)
					+ " ( " + event.getChannels().size() + " channel"
					+ ((event.getChannels().size() > 1) ? "s" : "") + " )");
		}
		else {
			window.output("* " + event.getNick() + " is not currently in any channel");
		}
		
		window.output("* " + event.getNick() + " is currently on " + event.getServer()
				+ " ( " + event.getServerInfo() + " )");
		
		if(event.getRegisteredAs() != null)
			window.output("* " + event.getNick() + " is logged in as " + event.getRegisteredAs());
		
		if(event.getSignOnTime() > 0) {
			long signOnMs = event.getSignOnTime()*1000L;
			long agoS = (System.currentTimeMillis() - signOnMs) / 1000;
			
			window.output("* " + event.getNick() + " signed on " +
					SimpleDateFormat.getDateTimeInstance().format(new Date(signOnMs))
					+ " ( " + outputTime(agoS) + " ago ) ");
		}
		
		if(event.getIdleSeconds() > 0)
			window.output("* " + event.getNick() + " has been idle for " + outputTime(event.getIdleSeconds()) );
	}
	
	public void whoisServerReply(ServerResponseEvent<Bot> event) {
		if(event.getCode() == 401) {
			Window window = event.getBot().getConnectionContext().getCurrentWindow();
			String nick = event.getResponse().split(" ", 3)[1];
			window.output("* There is no user with the nick " + nick);
		}
	}
	
	public String outputTime(long totalseconds) {
		long seconds = totalseconds % 60;
		long minutes = (totalseconds % 3600) / 60;
		long hours = (totalseconds % 86400) / 3600;
		long days = (totalseconds % 31536000) / 86400;
		long years = totalseconds / 31536000;

		String output = "";
		if (years != 0)
			output += (output.length() > 0 ? ", " : "") + years + " year"
					+ (years > 1 ? "s" : "");
		if (days != 0)
			output += (output.length() > 0 ? ", " : "") + days + " day"
					+ (days > 1 ? "s" : "");
		if (hours != 0)
			output += (output.length() > 0 ? ", " : "") + hours + " hour"
					+ (hours > 1 ? "s" : "");
		if (minutes != 0)
			output += (output.length() > 0 ? ", " : "") + minutes + " minute"
					+ (minutes > 1 ? "s" : "");
		if (seconds != 0)
			output += (output.length() > 0 ? ", " : "") + seconds + " second"
					+ (seconds > 1 ? "s" : "");

		return output;
	}
}
