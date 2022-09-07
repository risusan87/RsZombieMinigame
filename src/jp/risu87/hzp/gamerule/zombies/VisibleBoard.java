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
	
	private final BoardSidebar waitBoard;
	private final Scoreboard invisibleBoard;
	
	private static VisibleBoard board;
	
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
		this.waitBoard = new BoardSidebar("" + ChatColor.YELLOW + ChatColor.BOLD + "ZOMBIES");
		
		this.waitBoard.setLine(8, "HI!");
		
		this.setVisibleBoard(BoardType.WAITING);
	}
	
	
	public void setVisibleBoard(BoardType type) {
		
		final Scoreboard setBoard = 
				type == BoardType.INVISIBLE ? this.invisibleBoard :
				type == BoardType.WAITING ? this.waitBoard.getScoreboard() :
				this.invisibleBoard;
		GameRunningRule.getZombies().inServerPlayers.forEach((uuid, profile) -> {
			System.out.println("visible to " + uuid);
			Bukkit.getPlayer(uuid).setScoreboard(setBoard);
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
	
	private static class BoardSidebar {
		
		private List<String> lines;
		private String title;
		
		public BoardSidebar(String title) {
			this.lines = new ArrayList<String>();	
			this.title = title;
		}
		
		public void setLine(int lineNum, String line) {
			for (int i = lines.size(); lines.size() < lineNum - 1; i++) {
				String s = "";
				for (int j = 0; j < i; j++) s+=" ";
				this.lines.add(s);
			}
			this.lines.add(line);
		}
		
		public Scoreboard getScoreboard() {
			
			Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
			Objective bObj = board.registerNewObjective("obj", "dummy");
			bObj.setDisplayName(title);
			bObj.setDisplaySlot(DisplaySlot.SIDEBAR);
			
			int lineSize = this.lines.size();
			for (int i = lineSize; i > 0 ; i--) {
				Score s = bObj.getScore(this.lines.get(i - 1));
				s.setScore(i);
			}
			
			if (lineSize == 0) {
				Score s = bObj.getScore("");
				s.setScore(1);
			}
			
			return board;
		}
		
	}
}
