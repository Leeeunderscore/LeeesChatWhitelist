package Leees.Chat.Whitelist;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class Main extends JavaPlugin implements Listener {
    public static ArrayList<String> verified = new ArrayList();
    public static ArrayList<String> blacklisted = new ArrayList();

    public static File completedFile;
    public static File blaclistFile;

    public static Main getPlugin() {
        return (Main) getPlugin(Main.class);
    }

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        File dir = new File("plugins/LeeesChatWhitelist");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        completedFile = new File(dir, "completed.txt");
        if (!completedFile.exists()) {
            try {
                completedFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(completedFile));
            br.lines().forEach(uuid ->
                    verified.add(uuid));
        } catch (IOException x) {
            x.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        (new Thread(() -> {
            try {
                ReCaptcha.main(null);
            } catch (IOException e) {
                e.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
            }
            blaclistFile = new File(dir, "blacklist.txt");
            if (!blaclistFile.exists()) {
                try {
                    blaclistFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
            }
            try {
                BufferedReader br = new BufferedReader(new FileReader(blaclistFile));
                br.lines().forEach(uuid2 ->
                        blacklisted.add(uuid2));
            } catch (IOException x) {
                x.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        })).start();
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("blacklist")) {
            sender.sendMessage(ChatColor.DARK_BLUE + "----------------------");
            sender.sendMessage(ChatColor.GOLD + "/blacklist add username");
            sender.sendMessage(ChatColor.DARK_BLUE + "----------------------");
            if (sender.hasPermission("whitelist.admin")) {
                if (args.length > 0) {
                    if (args[0].equals("add") && args.length > 1) {
                        UUID uuid2;
                        uuid2 = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                        Main.blacklisted.add(String.valueOf(uuid2));
                        sender.sendMessage(ChatColor.GREEN + "The player " + args[1] + " has been added to the blacklist");
                        return true;
                    }
                }
            }
        }
        if (cmd.getName().equals("whitelist")) {
            sender.sendMessage(ChatColor.DARK_BLUE + "----------------------");
            sender.sendMessage(ChatColor.GOLD + "/whitelist add username");
            sender.sendMessage(ChatColor.DARK_BLUE + "----------------------");
            if (sender.hasPermission("whitelist.admin")) {
                if (args.length > 0) {
                    if (args[0].equals("add") && args.length > 1) {
                        UUID uuid;
                        uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                        Main.verified.add(String.valueOf(uuid));
                        sender.sendMessage(ChatColor.GREEN + "The player " + args[1] + " has been added to the whitelist");
                        return true;
                    }
                }
            }
        }
        return false;
    }
        @EventHandler
    public void onChat(PlayerChatEvent e) {
        if (!verified.contains(e.getPlayer().getUniqueId().toString())) {
            e.setCancelled(true);
            e.getPlayer().kickPlayer(this.getConfig().getString("kickmessage").replace("&", "§"));
        } else if(blacklisted.contains(e.getPlayer().getUniqueId().toString())) {
            e.getPlayer().kickPlayer(this.getConfig().getString("blacklistedkick").replace("&", "§"));
        }
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        if (!verified.contains(e.getPlayer().getUniqueId().toString())) {
                e.setCancelled(true);
                e.getPlayer().kickPlayer(this.getConfig().getString("kickmessage").replace("&", "§"));
        } else if(blacklisted.contains(e.getPlayer().getUniqueId().toString())) {
            e.getPlayer().kickPlayer(this.getConfig().getString("blacklistedkick").replace("&", "§"));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!verified.contains(e.getPlayer().getUniqueId().toString())) {
            Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                int time = Main.getPlugin().getConfig().getInt("countdownkick"); //or any other number you want to start countdown from

                @Override
                public void run() {
                    if (this.time == 0) {
                        e.getPlayer().kickPlayer(Main.getPlugin().getConfig().getString("kickmessage").replace("&", "§"));
                    }

                    this.time--;
                }
            }, 0L, 20L);
            List strings = this.getConfig().getStringList("not-whitelisted-message");
            ArrayList finalStrings = new ArrayList();
            (new Thread(() -> {
                Iterator var3 = strings.iterator();

                String sss;
                while (var3.hasNext()) {
                    sss = (String) var3.next();
                    finalStrings.add(sss.replace("&", "§").replace("{playername}", e.getPlayer().getDisplayName()));
                }

                var3 = finalStrings.iterator();

                while (var3.hasNext()) {
                    sss = (String) var3.next();
                    e.getPlayer().sendMessage(sss);
                }

            })).start();
        } else if (blacklisted.contains(e.getPlayer().getUniqueId().toString())) {
                Bukkit.getScheduler().runTaskTimer(this, new Runnable()
                {
                    int time = Main.getPlugin().getConfig().getInt("countdownkick"); //or any other number you want to start countdown from

                    @Override
                    public void run()
                    {
                        if (this.time == 0)
                        {
                            e.getPlayer().kickPlayer(Main.getPlugin().getConfig().getString("blacklistedkick").replace("&", "§"));
                        }

                        this.time--;
                    }
                }, 0L, 20L);
                List strings = this.getConfig().getStringList("blacklisted-message");
                ArrayList finalStrings = new ArrayList();
                (new Thread(() -> {
                    Iterator var3 = strings.iterator();

                    String sss;
                    while (var3.hasNext()) {
                        sss = (String) var3.next();
                        finalStrings.add(sss.replace("&", "§").replace("{playername}", e.getPlayer().getDisplayName()));
                    }

                    var3 = finalStrings.iterator();

                    while (var3.hasNext()) {
                        sss = (String) var3.next();
                        e.getPlayer().sendMessage(sss);
                    }

                })).start();
        } else {
            List strings = this.getConfig().getStringList("whitelisted-message");
            ArrayList finalStrings = new ArrayList();
            (new Thread(() -> {
                Iterator var3 = strings.iterator();

                String sss;
                while (var3.hasNext()) {
                    sss = (String) var3.next();
                    finalStrings.add(sss.replace("&", "§").replace("{playername}", e.getPlayer().getDisplayName()));
                }

                var3 = finalStrings.iterator();

                while (var3.hasNext()) {
                    sss = (String) var3.next();
                    e.getPlayer().sendMessage(sss);
                }

            })).start();
        }
}
    public void onDisable() {}


    public static void save() {
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(blaclistFile));
            blacklisted.forEach(s -> {
                try {
                    br.append(s);
                    br.newLine();
                    br.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(completedFile));
            verified.forEach(s -> {
                try {
                    br.append(s);
                    br.newLine();
                    br.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
