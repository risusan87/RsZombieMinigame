package jp.risu87.hzp.gamerule.zombies;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import jp.risu87.hzp.HypixelZombiesProject;

public class VisibleBoard {
	
	private final Scoreboard waitBoard;
	private final Scoreboard invisibleBoard;
	
	private static VisibleBoard board;

	private List<Score> linesWaiting;
	private List<Score> linesInGame;
	private List<Score> linesEnded;
	
	private String mapString = "Quintistic";
	private int playerReady = 1;
	private int maxPlayers = 4;
	
	protected static enum BoardType {
		INVISIBLE,
		WAITING,
		INGAME,
		ENDED
	}
	
	private VisibleBoard() {
		
		this.invisibleBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.waitBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		
		Objective obj = this.waitBoard.registerNewObjective("VisibleBoard", "dummy");
		obj.setDisplayName("" + ChatColor.YELLOW + ChatColor.BOLD + "ZOMBIES");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.linesWaiting = new ArrayList<Score>();
		
		
		this.linesWaiting.add(0, obj.getScore(ChatColor.YELLOW + "quintistic.net"));
		this.linesWaiting.get(0).setScore(1);
		this.linesWaiting.add(1, obj.getScore(" "));
		this.linesWaiting.get(1).setScore(2);
		this.linesWaiting.add(2, obj.getScore(ChatColor.WHITE + "Waiting..."));
		this.linesWaiting.get(2).setScore(3);
		this.linesWaiting.add(3, obj.getScore("  "));
		this.linesWaiting.get(3).setScore(4);
		this.linesWaiting.add(4, obj.getScore(ChatColor.WHITE + "Players: " + ChatColor.GREEN + playerReady + "/" + maxPlayers));
		this.linesWaiting.get(4).setScore(5);
		this.linesWaiting.add(5, obj.getScore(ChatColor.WHITE + "Map: " + ChatColor.GREEN + mapString));
		this.linesWaiting.get(5).setScore(6);
		this.linesWaiting.add(6, obj.getScore("   "));
		this.linesWaiting.get(6).setScore(7);
		this.linesWaiting.add(7, obj.getScore("    "));
		this.linesWaiting.get(7).setScore(8);

	}
	
	
	public void setVisibleBoard(BoardType type) {
		
		Scoreboard setBoard = null;
		switch (type) {
			case INVISIBLE:
				setBoard = this.invisibleBoard;
			case WAITING:
				setBoard = this.waitBoard;
			default:
				setBoard = this.invisibleBoard;
		}
		
		final Scoreboard b = setBoard;
		GameRunningRule.getZombies().inServerPlayers.forEach((uuid, profile) -> {
			Bukkit.getPlayer(uuid).setScoreboard(b);
		});
		
	}
	
	public static VisibleBoard setupBoard() {
		if (board != null)
			return board;
		return (board = new VisibleBoard());
	}
	
	public static VisibleBoard getBoard() {
		return board;
	}
	
	public static void disableVisibleBoard() {
		
		GameRunningRule.getZombies().inServerPlayers.forEach((uuid, profile) -> {
			Bukkit.getPlayer(uuid).setScoreboard(board.invisibleBoard);
		});
		board = null;
		
	}
}
