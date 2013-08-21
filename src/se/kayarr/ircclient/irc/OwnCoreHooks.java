package se.kayarr.ircclient.irc;

import org.pircbotx.hooks.CoreHooks;
import org.pircbotx.hooks.events.VersionEvent;

@SuppressWarnings("rawtypes")
public class OwnCoreHooks extends CoreHooks {
	@Override
	public void onVersion(VersionEvent event) {
        event.respond("VERSION " + ((Bot)event.getBot()).getActualVersion());
	}
}
