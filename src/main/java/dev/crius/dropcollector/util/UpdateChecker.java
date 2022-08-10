package dev.crius.dropcollector.util;

import dev.crius.dropcollector.DropCollectorPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@RequiredArgsConstructor
@Getter
public class UpdateChecker {

    private static final String URL = "https://raw.githubusercontent.com/CriusDevelopment/CDropCollector-Public/main/version";

    private final DropCollectorPlugin plugin;

    private String updateMessage;
    private boolean upToDate = true;

    public void checkUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> {
            try {
                HttpsURLConnection con = (HttpsURLConnection) new URL(URL).openConnection();
                con.setUseCaches(false);
                InputStreamReader reader = new InputStreamReader(con.getInputStream());
                String[] split = (new BufferedReader(reader)).readLine().split(";");
                String latestVersion = split[0];
                updateMessage = split[1];
                this.upToDate = latestVersion.equals(plugin.getDescription().getVersion());

                if (!this.upToDate) {
                    this.plugin.log("An update was found for CDropCollector!");
                    this.plugin.log("Update message:");
                    this.plugin.log(updateMessage);
                } else
                    this.plugin.log("Plugin is up to date, no update found.");
            } catch (IOException exception) {
                this.plugin.log("Could not check for updates!", exception);
            }
        });
    }

}
