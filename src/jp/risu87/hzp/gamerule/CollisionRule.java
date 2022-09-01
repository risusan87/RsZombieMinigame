package jp.risu87.hzp.gamerule;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class CollisionRule {
	
	private static CollisionRule rule;
	private final Team teamPlayer;
	private final Scoreboard board;
	
	private CollisionRule() {
		ScoreboardManager sm = Bukkit.getScoreboardManager();
        this.board = sm.getNewScoreboard();
        this.teamPlayer = board.registerNewTeam("no_collision");
        this.teamPlayer.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
	}
	
	public static CollisionRule setupCollisionRule(boolean collidable) {
		return rule == null ? (rule = new CollisionRule()) : rule;
		
	}
	
	public static CollisionRule getCollisionRule() {
		return rule;
	}
	
	public static void disableCollisionRule() {
		
	}
	
    public void addPlayer(Player p) {
        
        this.teamPlayer.addEntry(p.getName());
        p.setScoreboard(board);
    }

    public void removePlayer(Player p) {
        if(p.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) return;
        if(p.getScoreboard().getTeam("no_collision") != null)
            p.getScoreboard().getTeam("no_collision").removeEntry(p.getName());
    }

    public boolean containsPlayer(Player p) {
        return p.getScoreboard().getTeam("no_collision") != null && p.getScoreboard().getTeam("no_collision").hasEntry(p.getName());
    }
}
