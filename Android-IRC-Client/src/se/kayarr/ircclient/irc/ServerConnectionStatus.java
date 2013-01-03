package se.kayarr.ircclient.irc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor(suppressConstructorProperties=true)
public class ServerConnectionStatus {
	private String title;
	private String info;
}
