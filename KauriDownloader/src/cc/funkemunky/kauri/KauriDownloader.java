package cc.funkemunky.kauri;

import cc.funkemunky.kauri.utils.DownloaderUtils;
import cc.funkemunky.kauri.utils.ReflectionsUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Getter
public class KauriDownloader extends JavaPlugin {

    @Getter
    private static KauriDownloader instance;
    private File kauri;
    private ScheduledExecutorService scheduledExecutorService;
    private String[] requitedVersionOfAlias = new String[] {"1.1.3.1"};
    private Plugin plugin;

    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        getLogger().log(Level.INFO, "Downloading Atlas...");
        DownloaderUtils.downloadAppropriateVersion();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        try {
            if(Bukkit.getPluginManager().getPlugin("Kauri") == null || !Bukkit.getPluginManager().getPlugin("Kauri").isEnabled()) {
                getLogger().log(Level.INFO, "Finding suitable server location...");
                URL url = new URL("https://funkemunky.cc/download?name=Kauri_Trial&license=" + getConfig().getString("license") + "&version=" + getDescription().getVersion());
                getLogger().log(Level.INFO, "Downloading Kauri...");
                InputStream is = null;
                try {
                    is = url.openStream();
                    ObjectInputStream baos = new ObjectInputStream(is);
                    byte[] byteChunk = (byte[]) baos.readObject(); // Or whatever size you want to read in at a time.

                    if(ReflectionsUtil.version.contains("7")) {
                        kauri = new File("plugins", net.minecraft.util.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(30) + ".jar");
                        net.minecraft.util.org.apache.commons.io.FileUtils.writeByteArrayToFile(kauri, byteChunk);
                    } else {
                        kauri = new File("plugins", org.apache.commons.lang3.RandomStringUtils.randomAlphabetic(30) + ".jar");
                        org.apache.commons.io.FileUtils.writeByteArrayToFile(kauri, byteChunk);
                    }

                    getLogger().log(Level.INFO, "Downloaded! Loading plugin...");
                     plugin = getServer().getPluginManager().loadPlugin(kauri);
                    is.close();
                    getServer().getPluginManager().enablePlugin(plugin);

                    getLogger().log(Level.INFO, "Loaded successfully!");
                    kauri.delete();
                }
                catch (IOException e) {
                    System.err.print("An error occurred in the attempt to download Kauri. Please double check your licence key in the config.");
                    // Perform any other exception handling that's appropriate.
                } catch (InvalidDescriptionException | InvalidPluginException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.print("An error occurred in the attempt to download Kauri.");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void onDisable() {
        URLClassLoader classLoader = (URLClassLoader) plugin.getClass().getClassLoader();

        try {
            classLoader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        scheduledExecutorService.schedule(() -> kauri.delete(), 2L, TimeUnit.SECONDS);
    }
}
