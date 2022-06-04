package dev.xbit.combattag.combat.listener;

import dev.xbit.combattag.combat.CombatHandler;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import dev.xbit.combattag.combat.bossbar.BossBarTask;
import net.kyori.adventure.text.Component;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
public class CombatListener implements Listener {

    private Set<UUID> deathsForLoggingOut = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        Player damagee = (Player) event.getEntity();
        Player damager;

        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (shooter == null || !(shooter instanceof Player))
                return;

            damager = (Player) shooter;
        } else {
            return;
        }

        if (damager.equals(damagee))
            return;

        CombatHandler.applyTag(damagee);
        CombatHandler.applyTag(damager);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        BossBarTask.remove(player);

        if (!CombatHandler.isTagged(player))
            return;

        CombatHandler.removeTag(player);

        TownBlock townBlock = TownyAPI.getInstance().getTownBlock(player.getLocation());
        if(townBlock != null && townBlock.getType() == TownBlockType.ARENA && townBlock.hasTown())
            return;

        deathsForLoggingOut.add(player.getUniqueId());
        player.setHealth(0.0);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (deathsForLoggingOut.contains(player.getUniqueId())) {
            deathsForLoggingOut.remove(player.getUniqueId());
            if(Objects.equals(GameRule.SHOW_DEATH_MESSAGES, true)) {
                event.deathMessage(Component.text(player.getName() + " was killed for logging out in combat."));
            }
        }

        if (!CombatHandler.isTagged(player))
            return;

        CombatHandler.removeTag(player);
    }


    // Prevent claim hopping
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPvP(TownyPlayerDamagePlayerEvent event) {
        if (!event.isCancelled())
            return;

        TownyWorld world = TownyAPI.getInstance().getTownyWorld(event.getVictimPlayer().getWorld().getName());
        Player attacker = event.getAttackingPlayer();
        Player victim = event.getVictimPlayer();

        assert world != null;
        if (!world.isFriendlyFireEnabled() && CombatUtil.isAlly(attacker.getName(), victim.getName()))
            return;

        if (!CombatHandler.isTagged(victim))
            return;

        event.setCancelled(false);
    }

}