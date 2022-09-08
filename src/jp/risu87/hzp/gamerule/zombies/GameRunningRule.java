package jp.risu87.hzp.gamerule.zombies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import jp.risu87.hzp.HypixelZombiesProject;

public class GameRunningRule {
	
	private static GameRunningRule rule = null;
	
	protected final Map<UUID, GameProfile> inServerPlayers;
	protected GameState gameState = GameState.EDIT;
	
	private GameRunningRule() {
		
		this.inServerPlayers = new HashMap<UUID, GameProfile>();
		HypixelZombiesProject.getPlugin().getServer().getOnlinePlayers().forEach(p -> {
			System.out.println(p + " put");
			this.inServerPlayers.put(p.getUniqueId(), new GameProfile());
		});
		
	}
	
	public static GameRunningRule getZombies() {
		return rule == null ? (rule = new GameRunningRule()) : rule;
	}
	
	public List<UUID> getIngamePlayers() {
		
		final List<UUID> uuids = new ArrayList<UUID>();
		this.inServerPlayers.forEach((uuid, profile) -> {
			uuids.add(uuid);
		});
		return uuids;
		
	}
	
	public void addPlayerIfNew(Player player) {
		if (!this.inServerPlayers.containsKey(player.getUniqueId()))
			this.inServerPlayers.put(player.getUniqueId(), new GameProfile());
	}
	
	public void removePlayer(Player player) {
		if (this.inServerPlayers.containsKey(player.getUniqueId()))
			this.inServerPlayers.remove(player.getUniqueId());
	}
	
	protected static enum PlayerState {
		IN_GAME_ALIVE,
		IN_GAME_DEAD,
		IN_GAME_QUIT,
		SPECTATOR,
		EDITOR,
		INLOBBY;
	}
	
	protected static enum GameState {
		PLAY,
		EDIT
	}
	
	protected static class GameProfile {
		
		public PlayerState playerState = PlayerState.INLOBBY;
		public int purseGold;
		public int kills;
		
		public GameProfile() {
			
		}
		
	}
}
