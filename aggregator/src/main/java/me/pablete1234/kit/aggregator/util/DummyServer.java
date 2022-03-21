package me.pablete1234.kit.aggregator.util;

import net.minecraft.server.v1_8_R3.DedicatedPlayerList;
import net.minecraft.server.v1_8_R3.DedicatedServer;
import net.minecraft.server.v1_8_R3.DispenserRegistry;
import net.minecraft.server.v1_8_R3.PlayerList;
import net.minecraft.server.v1_8_R3.PropertyManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.jline.console.ConsoleReader;
import org.bukkit.craftbukkit.libs.joptsimple.OptionParser;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.scheduler.CraftAsyncScheduler;
import org.bukkit.craftbukkit.v1_8_R3.scheduler.CraftScheduler;
import org.bukkit.craftbukkit.v1_8_R3.util.ForwardLogHandler;
import org.spigotmc.WatchdogThread;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Handler;

public class DummyServer {

    public DummyServer() throws IOException {
        System.out.println("Setting up dummy minecraft server...");
        long start = System.currentTimeMillis();

        setupLogging();
        new Server();

        System.out.println("Done! took " + (System.currentTimeMillis() -  start) + "ms");
    }

    private static void setupLogging() {
        // Log4j 1, used by netty
        setPropertyToResource("log4j.configuration", "log4j.xml");

        // Log4j 2, used by minecraft
        setPropertyToResource("log4j2.configurationFile", "log4j2.xml");
        System.setProperty("log4j.skipJansi", "true");

        // java Logger used by bukkit
        java.util.logging.Logger global = java.util.logging.Logger.getLogger("");
        global.setUseParentHandlers(false);
        for (Handler handler : global.getHandlers()) global.removeHandler(handler);
        global.addHandler(new ForwardLogHandler());
    }

    private static void setPropertyToResource(String property, String file) {
        URL url = DummyServer.class.getClassLoader().getResource(file);
        if (url != null)
            System.setProperty(property, url.toString());
    }

    static class Server extends DedicatedServer {

        public Server() throws IOException {
            super(new OptionParser() {{
                this.accepts("config").withRequiredArg().ofType(File.class)
                        .defaultsTo(new File("server.properties"));
                this.accepts("sportpaper-settings").withRequiredArg().ofType(File.class)
                        .defaultsTo(Files.createTempFile("sportpaper", "yml").toFile());
            }}.parse());

            this.propertyManager = new DummyPropertyManager(options);

            // Create a minimal dedicated player list without calling the constructor.
            // This avoids creating a bunch of files we don't want (ops list, banned list, whitelist, etc)
            DedicatedPlayerList list = ReflectionUtils.allocateInstance(DedicatedPlayerList.class);
            ReflectionUtils.set(list, PlayerList.class, "players", new CopyOnWriteArrayList<>());

            this.server = new CraftServer(this, list);
            Files.deleteIfExists(Paths.get("help.yml"));
            a(list);
            DispenserRegistry.c();


            // Someone had the brilliant idea to make async executor threads non-daemon threads, and to start them
            // before anything even needs them.
            // This is a workaround that shuts down the executor, stopping the threads.
            CraftScheduler scheduler = (CraftScheduler) Bukkit.getServer().getScheduler();
            CraftAsyncScheduler asyncScheduler = ReflectionUtils.get(scheduler, "asyncScheduler");
            ThreadPoolExecutor ex = ReflectionUtils.get(asyncScheduler, "executor");
            if (ex != null) ex.shutdown();

            // Pretend server is stopping, this shuts down the watchdog thread
            WatchdogThread.doStop();
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public boolean getSnooperEnabled() {
            return false;
        }

        @Override public void run() {}

        @Override public void stop() {}
    }

    private static class DummyPropertyManager extends PropertyManager {

        public DummyPropertyManager(OptionSet options) {
            super(options);
        }

        @Override
        public void a() {
            // no-op for saving prop file
        }

        @Override
        public void savePropertiesFile() {}
    }
}
