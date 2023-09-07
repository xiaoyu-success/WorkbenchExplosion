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
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        getLogger().info("WorkbenchExplosionPlugin 已加载!");

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, this);

        // 注册命令
        Objects.requireNonNull(getCommand("wbe")).setExecutor(this);
    }


    private Map<UUID, BukkitRunnable> timers = new HashMap<>();
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

        if (event.getPlayer() instanceof Player player) {
            Inventory openedInventory = event.getInventory();

            // 检查打开的 Inventory 是否是 CraftingInventory
            if (openedInventory instanceof CraftingInventory craftingInventory) {

                BukkitRunnable timer = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (craftingInventory.getLocation() != null) {
                            // 在合成台位置创建爆炸
                            player.getWorld().createExplosion(craftingInventory.getLocation(), 4.0f, true, true);
                        }
                    }
                };

                timer.runTaskLater(this, explosionDelay); // 触发时间由命令设置

                // 将计时器与玩家关联起来
                timers.put(player.getUniqueId(), timer);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            // 检查是否有与该玩家关联的计时器
            BukkitRunnable timer = timers.get(player.getUniqueId());
            if (timer != null) {
                // 玩家关闭工作台，取消计时器
                timer.cancel();
                timers.remove(player.getUniqueId());
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("wbe") || !sender.isOp()) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("使用: /wbe set <秒数> 或 /wbe on/off");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            try {
                long newDelay = Long.parseLong(args[1]);
                setExplosionDelay(newDelay * 20);
                sender.sendMessage("爆炸延时已设置为 " + newDelay + " 秒");
            } catch (NumberFormatException e) {
                sender.sendMessage("请输入有效的秒数。");
            }
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("on")) {
                setPluginEnabled(true);
                sender.sendMessage("插件已启用");
                return true;
            } else if (args[0].equalsIgnoreCase("off")) {
                setPluginEnabled(false);
                sender.sendMessage("插件已禁用");
                return true;
            }
        }

        sender.sendMessage("您无权执行此命令");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("wbe")) {
            List<String> completions = new ArrayList<>();

            if (args.length == 1) {
                completions.add("set");
                completions.add("on");
                completions.add("off");
            }

            return completions;
        }

        return null;
    }

}
