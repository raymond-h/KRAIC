package se.kayarr.ircclient.irc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor(suppressConstructorProperties=true)
public class UserInfo {
	public UserInfo() {}
	
	private String nick;
	private String userName;
	private String realName;
	
	private String quitMessage;
}