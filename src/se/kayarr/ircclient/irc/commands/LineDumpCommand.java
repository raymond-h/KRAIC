package se.kayarr.ircclient.irc.commands;

import se.kayarr.ircclient.irc.ServerConnection;
import se.kayarr.ircclient.irc.output.OutputLine;
import se.kayarr.ircclient.shared.StaticInfo;
import android.util.Log;

public class LineDumpCommand extends Command {
	public LineDumpCommand() {
		setHelpText("Dumps all the lines in the current window to logs.");
		addAlias("linedump");
	}

	@Override
	public void execute(ServerConnection conn, String alias, String params) {
		Log.d(StaticInfo.APP_TAG, "* Checking current window...");
		for(OutputLine line : conn.getCurrentWindow().getLines()) {
			Log.d(StaticInfo.APP_TAG, "*** line " + line + ": " + line.getOutput());
		}
	}
}
