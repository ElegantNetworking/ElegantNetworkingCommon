package hohserg.elegant.networking.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Config {
    private int packetSizeLimit;
    private BackgroundPacketSystem backgroundPacketSystem = BackgroundPacketSystem.ForgeImpl;

    public int getPacketSizeLimit() {
        return packetSizeLimit;
    }

    public BackgroundPacketSystem getBackgroundPacketSystem() {
        return backgroundPacketSystem;
    }

    public enum BackgroundPacketSystem {
        CCLImpl,
        ForgeImpl
    }

    public static Config init(File configFolder) {
        File configFile = new File(configFolder, "elegant_networking.cfg");
        Config config = new Config();

        if (configFile.exists())
            loadConfig(configFile, config);
        saveConfig(configFile, config);

        return config;
    }

    private static void saveConfig(File configFile, Config config) {
        try (FileWriter fileWriter = new FileWriter(configFile)) {
            fileWriter.write("# How many bytes can contains received packet. WIP feature!\n");
            fileWriter.write("packetSizeLimit = " + config.packetSizeLimit + "\n");
            fileWriter.write("\n");
            fileWriter.write("# What is a background packet system will be used\n");
            fileWriter.write("# Possible values: CCLImpl, ForgeImpl\n");
            fileWriter.write("# Setting it to `CCLImpl` may fix some troubles\n");
            fileWriter.write("backgroundPacketSystem = " + config.backgroundPacketSystem + "\n");
            fileWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig(File configFile, Config config) {
        try (
                FileInputStream fileInputStream = new FileInputStream(configFile);
                Scanner s = new Scanner(fileInputStream)
        ) {

            while (s.hasNextLine()) {
                String line = s.nextLine();
                int commentStart = line.indexOf('#');
                String withoutComment = line.substring(0, commentStart == -1 ? line.length() : commentStart);
                if (!withoutComment.isEmpty()) {
                    String[] split = withoutComment.split("=");
                    if (split.length == 2) {
                        String fieldName = split[0].trim();
                        String value = split[1].trim();
                        if (fieldName.equals("packetSizeLimit"))
                            config.packetSizeLimit = Integer.parseInt(value);
                        if (fieldName.equals("backgroundPacketSystem"))
                            config.backgroundPacketSystem = BackgroundPacketSystem.valueOf(value);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
