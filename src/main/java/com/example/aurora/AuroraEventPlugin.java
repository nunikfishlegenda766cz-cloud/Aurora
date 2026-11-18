package com.example.aurora;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public final class AuroraEventPlugin extends JavaPlugin implements Listener, CommandExecutor {

    private boolean eventActive = false;
    private BukkitTask mainEventTask;
    private BukkitTask zombieTask;
    private BukkitTask effectTask;
    private final Random random = new Random();

    @Override
    public void onEnable() {
        getCommand("aurora").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("AuroraEventPlugin byl úspěšně spuštěn!");
    }

    @Override
    public void onDisable() {
        stopAuroraEvent();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("aurora.admin")) {
            sender.sendMessage(ChatColor.RED + "Na tento příkaz nemáš práva!");
            return true;
        }

        if (eventActive) {
            sender.sendMessage(ChatColor.RED + "Polární event už probíhá!");
            return true;
        }

        startAuroraEvent();
        Bukkit.broadcastMessage(ChatColor.AQUA + "✨ Na obloze se začíná objevovat čarovná polární záře! Event začal!");
        return true;
    }

    private void startAuroraEvent() {
        eventActive = true;

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setTime(18000);
        }

        // 1. Efekt polární záře na obloze
        effectTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location loc = player.getLocation().add(0, 15, 0);
                player.spawnParticle(Particle.VILLAGER_HAPPY, loc, 25, 10, 2, 10, 0.05);
                player.spawnParticle(Particle.END_ROD, loc.add(0, 2, 0), 5, 8, 1, 8, 0.01);
            }
        }, 0L, 40L);

        // 2. Spawn speciálních zombie každých 5 minut (6000 ticků = 5 minut)
        zombieTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location loc = player.getLocation().add(random.nextInt(6) - 3, 0, random.nextInt(6) - 3);
                Zombie zombie = player.getWorld().spawn(loc, Zombie.class);
                zombie.setCustomName(ChatColor.AQUA + "❄ Polární Zombie");
                zombie.setCustomNameVisible(true);
                zombie.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                if (zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                    zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
                }
                zombie.setHealth(40.0);
                player.sendMessage(ChatColor.YELLOW + "⚠ V blízkosti se vynořil Polární Zombie!");
            }
        }, 0L, 6000L);

        // 3. Ukončení eventu po 30 minutách (36000 ticků)
        mainEventTask = Bukkit.getScheduler().runTaskLater(this, () -> {
            stopAuroraEvent();
            Bukkit.broadcastMessage(ChatColor.GOLD + "✨ Polární záře pomalu mizí. Event skončil, čas se opět rozběhl!");
        }, 36000L);
    }

    private void stopAuroraEvent() {
        if (!eventActive) return;
        eventActive = false;

        if (effectTask != null) effectTask.cancel();
        if (zombieTask != null) zombieTask.cancel();
        if (mainEventTask != null) mainEventTask.cancel();

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        }
    }

    // Blokování spawnu mobů na povrchu (Y >= 50), povolení v jeskyních
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!eventActive) return;

        if (event.getEntity() instanceof Monster) {
            if (!(event.getEntity() instanceof Zombie && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().contains("Polární"))) {
                if (event.getLocation().getY() >= 50) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // Drop 5 náhodných rud po zabití Polárního Zombie
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!eventActive) return;

        if (event.getEntity() instanceof Zombie) {
            Zombie zombie = (Zombie) event.getEntity();
            if (zombie.getCustomName() != null && zombie.getCustomName().contains("Polární")) {
                event.getDrops().clear();

                Material[] ores = {
                    Material.DIAMOND,
                    Material.GOLD_INGOT,
                    Material.IRON_INGOT,
                    Material.EMERALD,
                    Material.LAPIS_LAZULI,
                    Material.REDSTONE,
                    Material.COAL
                };

                for (int i = 0; i < 5; i++) {
                    Material randomOre = ores[random.nextInt(ores.length)];
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(randomOre, 1));
                }
            }
        }
    }
}
        }
    }
}
