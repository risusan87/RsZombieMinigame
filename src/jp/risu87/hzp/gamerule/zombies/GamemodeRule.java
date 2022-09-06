package jp.risu87.hzp.gamerule.zombies;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GamemodeRule {
	
	public void applyGamemode() {
		GameRunningRule.getZombies().inServerPlayers.forEach((uuid, profile) -> {
			
			Player p = Bukkit.getPlayer(uuid);
			
			switch (profile.playerState) {
				case SPECTATOR:
					p.setGameMode(GameMode.SPECTATOR);
					break;
				case EDITOR:
					p.setGameMode(GameMode.CREATIVE);
					break;
				default:
					p.setGameMode(GameMode.ADVENTURE);
			}
		});
	}

}
