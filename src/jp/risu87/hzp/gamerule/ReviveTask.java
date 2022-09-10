package jp.risu87.hzp.gamerule;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.google.common.base.Function;

import jp.risu87.hzp.HypixelZombiesProject;
import jp.risu87.hzp.entity.Corpse;
import jp.risu87.hzp.gamerule.GameStateRule.GameProfile;
import jp.risu87.hzp.gamerule.GameStateRule.PlayerState;
import jp.risu87.hzp.util.ActionBarConstructor;
import jp.risu87.hzp.util.ChatJsonBuilder;
import net.minecraft.server.v1_12_R1.EntityPlayer;

public class ReviveTask {

	private int reviveLookforTaskID = -1;
	private int reviveTickingTaskID = -1;
	private float revLifeRemaining = 30.0f;
	private float revRemaining = 1.5f;
	
	private ArmorStand hologram;

	/**
	 * Call this to make player knocked down.
	 * @param p
	 * @param playerCorpse
	 */
	public void knockdownPlayer(UUID p) {

		GameProfile profile = GameRunningRule.getZombies().inServerPlayers.get(p);
		Corpse playerCorpse = profile.playerCorpse;
		
		if (profile != null && profile.playerState == PlayerState.IN_GAME_ALIVE) {

			Player profileOwner = Bukkit.getPlayer(p);
			profile.playerState = PlayerState.IN_GAME_DOWN;
			
			hologram = (ArmorStand)profileOwner.getWorld().spawnEntity(profileOwner.getLocation().add(0, -1, 0), EntityType.ARMOR_STAND);
			((CraftEntity)hologram).getHandle().setInvisible(true);
			hologram.setGravity(false);
			hologram.setCustomName("HELOGDHWYUWD");
			hologram.setCustomNameVisible(true);
			
			ScoreboardRule.getScoreboardRule().removePlayer(profileOwner, ScoreboardRule.TEAM_IN_GAME_PLAYERS);
			ScoreboardRule.getScoreboardRule().addPlayer(profileOwner, ScoreboardRule.TEAM_CORPSE);

			saveInventory(profileOwner);

			((CraftEntity)profileOwner).getHandle().setInvisible(true);
			playerCorpse.setInvisible(false);

			scheduleKnockdownTask(profileOwner, playerCorpse);
		}
	}

	private void scheduleKnockdownTask(Player profileOwner, Corpse playerCorpse) {
		// start revive task and look for players nearby who is sneaking
		// this task runs asynchronously yet synchronized with server thread.
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		Runnable reviveLookforTask = () -> {
			Location corpseLoc = playerCorpse.getLocation();
			profileOwner.getWorld().getNearbyEntities(corpseLoc, 0.5, 0.5, 0.5).forEach(p -> {
				if (!(p instanceof Player)) 
					return;
				Player player = ((Player)p);
				// player is trying to revive profileOwner
				if (player.isSneaking()) {
					plugin.getServer().getScheduler().cancelTask(reviveLookforTaskID);
					this.revLifeRemaining = 30.f;
					this.scheduleReviveTickingTask(player, profileOwner, playerCorpse);
					return;
				}
			});
			this.revLifeRemaining -= 0.1f;
			hologram.setCustomName(String.format("Dies in %.1f sec", this.revLifeRemaining));
			// if time runs out
			if (this.revLifeRemaining <= 0f) {
				GameRunningRule.getZombies().inServerPlayers.get(profileOwner.getUniqueId()).playerState = PlayerState.IN_GAME_DEAD;
				profileOwner.setGameMode(GameMode.CREATIVE);
				HypixelZombiesProject.getSchedular().cancelTask(reviveLookforTaskID);
				hologram.setHealth(0);
				return;
			}
		};
		this.reviveLookforTaskID = HypixelZombiesProject.getSchedular().scheduleSyncRepeatingTask(
				plugin, reviveLookforTask, 0, 2);
	}

	private void scheduleReviveTickingTask(Player reviver, Player profileOwner, Corpse playerCorpse) {
		ActionBarConstructor actionBar = ActionBarConstructor.constractActionBarText(
				new ChatJsonBuilder().withText(String.format("Reviving %s %.1f", profileOwner.getName(), this.revRemaining))
			);
		actionBar.addViewers(reviver);
		HypixelZombiesProject plugin = HypixelZombiesProject.getPlugin();
		this.reviveTickingTaskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
				plugin, () -> {
					
					actionBar.setTextVisible(false);
					actionBar.editMessage(new ChatJsonBuilder().withText(String.format("Reviving %s %.1f", profileOwner.getName(), this.revRemaining)));
					actionBar.setTextVisible(true);
					hologram.setCustomName(String.format("Being revived %.1f ", this.revLifeRemaining));
					// player cancels revive
					if (!reviver.isSneaking()) {
						this.scheduleKnockdownTask(profileOwner, playerCorpse);
						plugin.getServer().getScheduler().cancelTask(this.reviveTickingTaskID);
						actionBar.setTextVisible(false);
						this.revRemaining = 1.5f;
						return;
					}
					this.revRemaining -= 0.1f;
					
					// player revived
					if (this.revRemaining <= 0f) {

						HypixelZombiesProject.getSchedular().cancelTask(reviveTickingTaskID);
						GameProfile profile = GameRunningRule.getZombies().inServerPlayers.get(profileOwner.getUniqueId());
						profile.playerState = PlayerState.IN_GAME_ALIVE;

						ScoreboardRule.getScoreboardRule().removePlayer(profileOwner, ScoreboardRule.TEAM_CORPSE);
						ScoreboardRule.getScoreboardRule().addPlayer(profileOwner, ScoreboardRule.TEAM_IN_GAME_PLAYERS);

						restoreInventory(profileOwner);

						((CraftPlayer)profileOwner).getHandle().setInvisible(false);
						playerCorpse.setInvisible(true);

						profileOwner.setGameMode(GameMode.ADVENTURE);
						
						actionBar.setTextVisible(false);
						
						hologram.remove();
						
						return;
					}
				}, 0, 2);
	}

	private void saveInventory(Player p) {
		GameRunningRule.getZombies().inServerPlayers.get(p.getUniqueId()).playerCorpse.savedInventory 
			= p.getInventory().getContents();
		p.getInventory().clear();
	}
	
	private void restoreInventory(Player p) {
		ItemStack[] saved = GameRunningRule.getZombies().inServerPlayers.get(p.getUniqueId()).playerCorpse.savedInventory;
		p.getInventory().setContents(saved);
	}
}
