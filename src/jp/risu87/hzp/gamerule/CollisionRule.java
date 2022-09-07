package jp.risu87.hzp.gamerule;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class CollisionRule {
	
	private static CollisionRule rule;
	private final Team teamInGamePlayers;
	private final Team teamInGameNonPlayers;
	private final Scoreboard board;
	
	public static final String TEAM_IN_GAME_PLAYERS = "team1";
	public static final String TEAM_IN_GAME_NON_PLAYERS = "team2";
	
	private CollisionRule() {
		
		ScoreboardManager sm = Bukkit.getScoreboardManager();
        this.board = sm.getNewScoreboard();
        
        
        this.teamInGamePlayers = board.registerNewTeam(TEAM_IN_GAME_PLAYERS);
        this.teamInGamePlayers.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
        this.teamInGamePlayers.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        
        this.teamInGameNonPlayers = board.registerNewTeam(TEAM_IN_GAME_NON_PLAYERS);
        this.teamInGamePlayers.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);
        this.teamInGamePlayers.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        
	}
	
	public static CollisionRule setupCollisionRule(boolean collidable) {
		return rule == null ? (rule = new CollisionRule()) : rule;
		
	}
	
	public static CollisionRule getCollisionRule() {
		return rule;
	}
	
	public static void disableCollisionRule() {
		
	}
	
    public void addPlayer(Player p, String team) {
        
    	Team t = this.board.getTeam(team);
    	if (t == null)
    		return;
    	
        t.addEntry(p.getName());
        p.setScoreboard(board);
    }

    public void removePlayer(Player p, String team) {
        if(p.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard())) 
        	return;
        if(p.getScoreboard().getTeam(team) != null)
            p.getScoreboard().getTeam(team).removeEntry(p.getName());
    }

    public boolean containsPlayer(Player p, String team) {
        return p.getScoreboard().getTeam(team) != null && p.getScoreboard().getTeam(team).hasEntry(p.getName());
    }
}
