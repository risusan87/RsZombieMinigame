package jp.risu87.hzp.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import jp.risu87.hzp.HypixelZombiesProject;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;

public class ActionBarConstructor {
	
	private int counterTaskID = -1;
	private boolean disable = true;
	private final ChatJsonBuilder json;
	private final List<Player> viewers;
	private final Runnable runnable;
	
	private ActionBarConstructor(ChatJsonBuilder message) {
		
		this.viewers = new ArrayList<Player>();
		this.json = message;
		
		IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a(message.toString());
		IChatBaseComponent emptyChat = IChatBaseComponent.ChatSerializer.a((String)("{\"text\": \"\"}"));
		
		PacketPlayOutChat text = new PacketPlayOutChat(cbc, ChatMessageType.a((byte) 2));
		PacketPlayOutChat empty = new PacketPlayOutChat(emptyChat, ChatMessageType.a((byte) 2));
		
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		BukkitScheduler schedular = plugin.getServer().getScheduler();
		final ActionBarConstructor cc = this;
		this.runnable = new Runnable() {
			
			@Override
			public void run() {
				
				if (cc.disable && cc.counterTaskID != -1) {
					cc.viewers.forEach( player -> {
						((CraftPlayer)player).getHandle().playerConnection.sendPacket(empty);
					});
					schedular.cancelTask(cc.counterTaskID);
					return;
				}

				cc.viewers.forEach( player -> {
					((CraftPlayer)player).getHandle().playerConnection.sendPacket(text);
				});
				
			}
			
		};
		
	}
	
	public void setTextVisible(boolean visible) {
		
		if (!visible) {
			this.disable = true;
			return;
		}
		
		if (!this.disable)
			return;
		this.disable = false;
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		BukkitScheduler schedular = plugin.getServer().getScheduler();
		this.counterTaskID = schedular.scheduleSyncRepeatingTask(plugin, this.runnable, 0, 1);
		
	}
	
	public void addViewers(Player... viewers) {
		for (Player p : viewers) {
			if (!this.viewers.contains(p))
				this.viewers.add(p);
		}
	}
	
	public void removeViewers(Player... viewers) {
		for (Player p : viewers) {
			if (this.viewers.contains(p))
				this.viewers.remove(p);
		}
	}
	
	public static ActionBarConstructor constractActionBarText(ChatJsonBuilder json) {
		return new ActionBarConstructor(json);
	}
	
	public static void sendTitle(Player player, ChatJsonBuilder msgTitle, ChatJsonBuilder msgSubTitle, float durationSec) {
		
		IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a((String)("{\"text\": \"" + msgTitle + "\"}"));
		IChatBaseComponent chatSubTitle = IChatBaseComponent.ChatSerializer.a((String)("{\"text\": \"" + msgSubTitle + "\"}"));
		PacketPlayOutTitle p = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
		PacketPlayOutTitle p2 = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubTitle);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(p);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(p2);
		ActionBarConstructor.sendTime(player, (int)Math.floor(durationSec * 20f));
		
	}

	private static void sendTime(Player player, int ticks) {
		PacketPlayOutTitle p = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, 20, ticks, 20);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(p);
	}

	public static void sendActionBar(Player player, ChatJsonBuilder message, float durationSec) {
		
		IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a((String)("{\"text\": \"" + message + "\"}"));
		IChatBaseComponent emptyChat = IChatBaseComponent.ChatSerializer.a((String)("{\"text\": \"\"}"));
		
		PacketPlayOutChat text = new PacketPlayOutChat(cbc, ChatMessageType.a((byte) 2));
		PacketPlayOutChat empty = new PacketPlayOutChat(emptyChat, ChatMessageType.a((byte) 2));
		
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		BukkitScheduler schedular = plugin.getServer().getScheduler();
		final ActionBarConstructor cc = new ActionBarConstructor(message);
		final int goalTick = (int) Math.floor(durationSec * 20f);
		Runnable r = new Runnable() {
			
			int counter = 0;
			
			@Override
			public void run() {
				
				if (counter >= goalTick && cc.counterTaskID != -1) {
					((CraftPlayer)player).getHandle().playerConnection.sendPacket(empty);
					schedular.cancelTask(cc.counterTaskID);
					return;
				}
				((CraftPlayer)player).getHandle().playerConnection.sendPacket(text);
				counter++;
				
			}
			
		};
		cc.counterTaskID = schedular.scheduleSyncRepeatingTask(plugin, r, 0, 1);
		
	}
	
	public static ActionBarConstructor sendActionBar(Player player, ChatJsonBuilder message) {
		
		IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a(message.toString());
		IChatBaseComponent emptyChat = IChatBaseComponent.ChatSerializer.a((String)("{\"text\": \"\"}"));
		
		PacketPlayOutChat text = new PacketPlayOutChat(cbc, ChatMessageType.a((byte) 2));
		PacketPlayOutChat empty = new PacketPlayOutChat(emptyChat, ChatMessageType.a((byte) 2));
		
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		BukkitScheduler schedular = plugin.getServer().getScheduler();
		final ActionBarConstructor cc = new ActionBarConstructor(message);
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				
				if (cc.disable && cc.counterTaskID != -1) {
					((CraftPlayer)player).getHandle().playerConnection.sendPacket(empty);
					schedular.cancelTask(cc.counterTaskID);
					return;
				}
				((CraftPlayer)player).getHandle().playerConnection.sendPacket(text);
				
			}
			
		};
		cc.counterTaskID = schedular.scheduleSyncRepeatingTask(plugin, r, 0, 1);
		return cc;
	}
}
