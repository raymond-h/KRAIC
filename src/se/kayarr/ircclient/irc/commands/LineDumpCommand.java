package se.kayarr.ircclient.irc.commands;

import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.output.OutputLine;
import android.util.Log;

public class LineDumpCommand extends Command {
	
	public static final String TAG = LineDumpCommand.class.getName();
	
	public LineDumpCommand() {
		setHelpText("Dumps all the lines in the current window to logs.");
		addAlias("linedump");
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		Log.d(TAG, "* Checking current window...");
		for(OutputLine line : conn.getCurrentWindow().getLines()) {
			Log.d(TAG, "*** line " + line + ": " + line.getOutput());
		}
	}
}
