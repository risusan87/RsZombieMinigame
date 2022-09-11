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

/**
 * Class {@link PlayerDeathRule} provides methods to make custom behaviors under regarding deaths of players.<br>
 * Use of this class should be pretty straight forward.<br>
 * For example, to make player Player123 knocked down state: <br>
 * <br>
 * {@code Player player = Bukkit.getPlayer("Player123");} <br>
 * {@code new ReviveTask().knockdownPlayer(player.getUniqueID());}<br>
 * <br>
 * Brand new instances are mandatory for each player to work.<br>
 * Do NOT reuse instances between different players.<br>
 * <br>
 * Methods in this class will not function if target players are under mismatch {@link PlayerState}.<br>
 * Such like calling knockdownPlayer() 
 * for a player who is already dead.
 */
public class PlayerDeathRule {

	private int reviveLookforTaskID = -1;
	private int reviveTickingTaskID = -1;
	private float revLifeRemaining = 30.0f;
	private float revRemaining = 1.5f;
	
	private ArmorStand hologram;
	
	public void knockdownPlayer(UUID p) {

		GameProfile profile = GameRunningRule.getZombies().getInGamePlayers().get(p);
		
		if (profile != null && profile.playerState == PlayerState.IN_GAME_ALIVE) {
			
			Corpse playerCorpse = new Corpse(Bukkit.getPlayer(p));
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
				GameRunningRule.getZombies().getInGamePlayers().get(profileOwner.getUniqueId()).playerState = PlayerState.IN_GAME_DEAD;
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
					hologram.setCustomName(String.format("Being revived %.1f ", this.revRemaining));
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
						GameProfile profile = GameRunningRule.getZombies().getInGamePlayers().get(profileOwner.getUniqueId());
						profile.playerState = PlayerState.IN_GAME_ALIVE;

						ScoreboardRule.getScoreboardRule().removePlayer(profileOwner, ScoreboardRule.TEAM_CORPSE);
						ScoreboardRule.getScoreboardRule().addPlayer(profileOwner, ScoreboardRule.TEAM_IN_GAME_PLAYERS);

						restoreInventory(profileOwner);

						((CraftPlayer)profileOwner).getHandle().setInvisible(false);
						playerCorpse.kill();

						profileOwner.setGameMode(GameMode.ADVENTURE);
						
						actionBar.setTextVisible(false);
						
						hologram.remove();
						
						return;
					}
				}, 0, 2);
	}
	
	private void saveInventory(Player p) {
		GameRunningRule.getZombies().getInGamePlayers().get(p.getUniqueId()).savedInventory 
				= p.getInventory().getContents();
		p.getInventory().clear();
	}
	
	private void restoreInventory(Player p) {
		ItemStack[] saved = GameRunningRule.getZombies().getInGamePlayers().get(p.getUniqueId()).savedInventory;
		p.getInventory().setContents(saved);
		GameRunningRule.getZombies().getInGamePlayers().get(p.getUniqueId()).savedInventory = null;
	}
}
