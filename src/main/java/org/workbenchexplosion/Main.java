package org.workbenchexplosion;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        getLogger().info("WorkbenchExplosionPlugin 已加载!");

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, this);

        // 注册命令
        Objects.requireNonNull(getCommand("wbe")).setExecutor(this);
    }


    private Map<Player, BukkitRunnable> timers = new HashMap<>();
    private boolean pluginEnabled = true;
    private long explosionDelay = 60L; // 默认3秒

    public void setPluginEnabled(boolean enabled) {
        this.pluginEnabled = enabled;
    }

    public void setExplosionDelay(long delay) {
        this.explosionDelay = delay;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!pluginEnabled) {
            return;
        }

        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (event.getView().getTitle().contains("Crafting")) {

                BukkitRunnable timer = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.getOpenInventory().getTitle().contains("Crafting")) {
                            player.getWorld().createExplosion(player.getLocation(), 4.0f, true, true);
                        }
                    }
                };

                timer.runTaskLater(this, explosionDelay); // 触发时间由命令设置

                // 将计时器与玩家关联起来
                timers.put(player, timer);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            // 检查是否有与该玩家关联的计时器
            BukkitRunnable timer = timers.get(player);
            if (timer != null) {
                // 玩家关闭工作台，取消计时器
                timer.cancel();
                timers.remove(player);
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("wbe") && sender.isOp()) {
            if (args.length == 0) {
                sender.sendMessage("使用: /wbe set <秒数> 或 /wbe on/off");
                return true;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
                try {
                    long newDelay = Long.parseLong(args[1]);
                    setExplosionDelay(newDelay * 20);
                    sender.sendMessage("爆炸延时已设置为 " + newDelay + " 秒");
                } catch (NumberFormatException e) {
                    sender.sendMessage("请输入有效的秒数。");
                }
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("on")) {
                setPluginEnabled(true);
                sender.sendMessage("插件已启用");
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("off")) {
                setPluginEnabled(false);
                sender.sendMessage("插件已禁用");
                return true;
            } else {
                sender.sendMessage("您无权执行此命令");
                return true;
            }
        }
        return false;
    }
}
