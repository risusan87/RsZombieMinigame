package jp.risu87.hzp.gamerule;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

import com.google.common.base.Function;

import jp.risu87.hzp.HypixelZombiesProject;
import jp.risu87.hzp.entity.Corpse;
import jp.risu87.hzp.gamerule.GameStateRule.GameState;
import jp.risu87.hzp.gamerule.VisibleBoard.BoardType;
import jp.risu87.hzp.util.ActionBarConstructor;
import jp.risu87.hzp.util.ChatJsonBuilder;
import net.minecraft.server.v1_12_R1.EntityPlayer;

public class GameStateRule {
	
	private static GameStateRule rule;
	protected GameState currentGameState = GameState.EDIT;
	private GameStateRule() {
		
	}
	
	public void switchGameState(GameState state) {
		
		if (currentGameState == state) 
			return;
		currentGameState = state;
		
		if (state == GameState.PLAY) {
			VisibleBoard.getBoard().setVisibleBoard(BoardType.INGAME);
			VisibleBoard.getBoard().ingameBoardTimerStart();
			GameRunningRule.getZombies().inServerPlayers.forEach((uuid, profile) -> {
				Player p = Bukkit.getPlayer(uuid);
				if (profile.playerState == PlayerState.IN_GAME_ALIVE) {
					
					ScoreboardRule.getScoreboardRule().addPlayer(p, ScoreboardRule.TEAM_IN_GAME_PLAYERS);
					
				}
			});
		}
		
		
	}
	
	public void setRound(int round) {
		
	}
	
	public static GameStateRule getGameStateRule() {
		return rule == null ? (rule = new GameStateRule()) : rule;
	}
	
	public static void disableGameStateRule() {
		rule = null;
	}
	
	protected static enum PlayerState {
		IN_GAME_ALIVE,
		IN_GAME_DOWN,
		IN_GAME_DEAD,
		IN_GAME_QUIT,
		SPECTATOR,
		EDITOR,
		INLOBBY;
	}
	
	public static enum GameState {
		PLAY,
		EDIT
	}
	
	protected static class GameProfile {
		
		//general
		public PlayerState playerState = PlayerState.IN_GAME_ALIVE;
		public int purseGold;
		public int kills;
		
		//reviving
		public Corpse playerCorpse;
		
		public GameProfile(Player owner) {
			this.playerCorpse = new Corpse(owner);
		}
		
	}
	
}
