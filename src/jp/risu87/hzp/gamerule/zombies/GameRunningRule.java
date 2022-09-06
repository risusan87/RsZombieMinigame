package jp.risu87.hzp.gamerule.zombies;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class GameRunningRule {
	
	private static GameRunningRule rule = null;
	
	protected final Map<UUID, GameProfile> inServerPlayers;
	protected GameState gameState = GameState.EDIT;
	
	private GameRunningRule() {
		
		this.inServerPlayers = new HashMap<UUID, GameProfile>();
		
	}
	
	public static GameRunningRule getZombies() {
		return rule == null ? (rule = new GameRunningRule()) : rule;
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
		
		public GameProfile() {
			
		}
		
	}
}
