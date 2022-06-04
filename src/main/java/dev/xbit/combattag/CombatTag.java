package dev.xbit.combattag;

import dev.xbit.combattag.combat.CombatHandler;
import dev.xbit.combattag.combat.bossbar.BossBarTask;
import dev.xbit.combattag.combat.listener.CombatListener;
import dev.xbit.combattag.commands.CombatTagCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class CombatTag extends JavaPlugin {

    private static CombatTag instance;

    public static CombatTag getInstance(){
        return instance;
    }
    private static Logger log = Bukkit.getLogger();

    @Override
    public void onEnable() {
        instance = this;
        log.info("§e======= §aCombatTag §e=======");
        log.info("§e CombatTag has been §aenabled§e.");
        setupListeners();
        setupCommands();
        runTasks();
    }

    private void setupListeners(){
        getServer().getPluginManager().registerEvents(new CombatListener(), this);
    }

    private void setupCommands(){
        Objects.requireNonNull(getCommand("combattag")).setExecutor(new CombatTagCommand());
    }

    private void runTasks(){
        new BossBarTask().runTaskTimer(this, 10, 10);
    }

    @Override
    public void onDisable() {
        for (Player p: this.getServer().getOnlinePlayers()) {
            if (CombatHandler.isTagged(p)){
                CombatHandler.removeTag(p);
            }
        }
    }
}
