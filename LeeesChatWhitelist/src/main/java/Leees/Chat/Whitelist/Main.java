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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
    public static ArrayList<String> verified = new ArrayList();

    public static File completedFile;

    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        File dir = new File("plugins/recaptcha");
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
            br.lines().forEach(name ->
                    verified.add(name));
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
        })).start();
    }

    @EventHandler
    public void onChat(PlayerChatEvent e) {
        if (!verified.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
            e.getPlayer().kickPlayer("§6[AntiBot] Please verify you're not a bot at http://yoursitehere.com/whitelist/ to chat on 6b6t");
        }
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        if (!verified.contains(e.getPlayer().getName())) {
            e.setCancelled(true);
            e.getPlayer().kickPlayer("§6[AntiBot] Please verify you're not a bot a http://yoursitehere.com/whitelist/ to run commands on 6b6t");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!verified.contains(e.getPlayer().getName())) {
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
