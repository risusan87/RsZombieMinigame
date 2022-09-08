package jp.risu87.hzp.gamerule;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class CollisionRule {
	
	private static CollisionRule rule;
	private final Team teamInGamePlayers;
	
	public static final String TEAM_IN_GAME_PLAYERS = "team1";
	public static final String TEAM_IN_GAME_CORPSE = "corpse";
	
	private CollisionRule() {
        
		Scoreboard sb = GameRunningRule.getZombies().scoreboard;
        this.teamInGamePlayers = sb.registerNewTeam(TEAM_IN_GAME_PLAYERS);
        this.teamInGamePlayers.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
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
        
    	Scoreboard sb = GameRunningRule.getZombies().scoreboard;
    	Team t = sb.getTeam(team);
    	if (t == null)
    		return;
    	
        t.addEntry(p.getName());
        p.setScoreboard(sb);
        
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
