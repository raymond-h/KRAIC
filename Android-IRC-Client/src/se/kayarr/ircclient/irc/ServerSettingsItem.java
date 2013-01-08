package se.kayarr.ircclient.irc;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public class ServerSettingsItem implements Serializable {
	private static final long serialVersionUID = -9187385204145219989L;
	
	@Getter private long id = -1;
	@Getter @Setter private String name;
	@Getter @Setter private String address;
	@Getter @Setter private int port;
	
	@Getter private UserInfo userInfo = new UserInfo();
	
	public ServerSettingsItem(long id, String name, String address, int port) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.port = port;
	}
	
	public ServerSettingsItem() {
	}

	public String getDisplayName() {
		return (name != null) ? name : address + ":" + port;
	}
	
	@Data @Accessors(chain=true)
	public static class Builder {
		private int id = -1;
		private String name = "Unnamed Server";
		private String address = "";
		private int port = 6667;
		private String nick;
		private String userName;
		private String realName;
		private String quitMessage;
		
		public Builder setUserInfo(UserInfo info) {
			this.nick = info.getNick();
			this.userName = info.getUserName();
			this.realName = info.getRealName();
			this.quitMessage = info.getQuitMessage();
			return this;
		}
		
		public ServerSettingsItem build() {
			ServerSettingsItem item = new ServerSettingsItem(id, name, address, port);
			item.userInfo = new UserInfo(nick, userName, realName, quitMessage);
			return item;
		}
	}
}
