package jp.risu87.hzp.gamerule.zombies;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import jp.risu87.hzp.HypixelZombiesProject;
import jp.risu87.hzp.gamerule.CollisionRule;
import jp.risu87.hzp.gamerule.PermissionRule;
import jp.risu87.hzp.gamerule.gun.GunRule;
import jp.risu87.hzp.gamerule.zombies.VisibleBoard.BoardType;

public class GameRunningRule {
	
	private static GameRunningRule rule = null;
	
	protected final Map<UUID, GameProfile> inServerPlayers;
	protected GameState gameState = GameState.EDIT;
	
	private GameRunningRule() {
		
		CollisionRule.setupCollisionRule(false);
		PermissionRule.setupPermissionRule();
		GunRule.setupGunRule();
		VisibleBoard.setupBoard();
		
		this.inServerPlayers = new HashMap<UUID, GameProfile>();
		HypixelZombiesProject.getPlugin().getServer().getOnlinePlayers().forEach(p -> {
			this.inServerPlayers.put(p.getUniqueId(), new GameProfile());
		});
		VisibleBoard.getBoard().setVisibleBoard(BoardType.WAITING);
		
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
