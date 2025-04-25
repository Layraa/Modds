package com.custommobsforge.custommobsforge.server;

import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ResourceConfig {
    private static final Logger LOGGER = LogManager.getLogger("CustomMobsForge");
    private static final File CONFIG_FILE = new File("config/custommobsforge/resources.bin");
    private static ResourceConfig instance;

    public static class ResourceEntry {
        public String id;
        public String path;

        public ResourceEntry(String id, String path) {
            this.id = id;
            this.path = path;
        }

        public void writeToBuf(FriendlyByteBuf buf) {
            buf.writeUtf(id);
            buf.writeUtf(path);
        }

        public static ResourceEntry readFromBuf(FriendlyByteBuf buf) {
            return new ResourceEntry(buf.readUtf(), buf.readUtf());
        }

        @Override
        public String toString() {
            return "ResourceEntry{id=" + id + ", path=" + path + "}";
        }
    }

    private List<ResourceEntry> models;
    private List<ResourceEntry> animations;
    private List<ResourceEntry> textures;

    private ResourceConfig() {
        this.models = new ArrayList<>();
        this.animations = new ArrayList<>();
        this.textures = new ArrayList<>();
    }

    public static void init() {
        if (instance != null) {
            return;
        }

        instance = new ResourceConfig();

        if (CONFIG_FILE.exists()) {
            try (DataInputStream input = new DataInputStream(new FileInputStream(CONFIG_FILE))) {
                byte[] bytes = new byte[input.available()];
                input.readFully(bytes);
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
                if (buf.readableBytes() < 4) {
                    throw new IOException("File is too small to contain valid data");
                }
                instance.models = readResourceList(buf);
                if (buf.readableBytes() < 4) {
                    throw new IOException("Not enough data for animations list");
                }
                instance.animations = readResourceList(buf);
                if (buf.readableBytes() < 4) {
                    throw new IOException("Not enough data for textures list");
                }
                instance.textures = readResourceList(buf);
                LOGGER.info("Loaded resources from config file: {}", CONFIG_FILE.getPath());
                LOGGER.info("Loaded {} models: {}", instance.models.size(), instance.models);
                LOGGER.info("Loaded {} animations: {}", instance.animations.size(), instance.animations);
                LOGGER.info("Loaded {} textures: {}", instance.textures.size(), instance.textures);
                return;
            } catch (Exception e) {
                LOGGER.error("Failed to load resources config from file: {}. Recreating file.", CONFIG_FILE.getPath(), e);
                if (CONFIG_FILE.delete()) {
                    LOGGER.info("Deleted corrupted resources.bin");
                } else {
                    LOGGER.warn("Failed to delete corrupted resources.bin");
                }
            }
        }

        scanResourceFolders();
        saveConfig();
        LOGGER.info("Initialized resources config: {}", CONFIG_FILE.getPath());
    }

    private static void scanResourceFolders() {
        instance.models = scanResourcesManually("assets/custommobsforge/geo", ".geo.json");
        LOGGER.info("Found {} models: {}", instance.models.size(), instance.models);

        instance.animations = scanResourcesManually("assets/custommobsforge/animations", ".animation.json");
        LOGGER.info("Found {} animations: {}", instance.animations.size(), instance.animations);

        instance.textures = scanResourcesManually("assets/custommobsforge/textures/entity", ".png");
        LOGGER.info("Found {} textures: {}", instance.textures.size(), instance.textures);
    }

    private static List<ResourceEntry> scanResourcesManually(String path, String extension) {
        List<ResourceEntry> resources = new ArrayList<>();
        try {
            LOGGER.info("Manually scanning path: {}", path);

            ClassLoader classLoader = ResourceConfig.class.getClassLoader();
            java.net.URL resourceUrl = classLoader.getResource(path);

            if (resourceUrl == null) {
                LOGGER.warn("Resource path not found in mod JAR: {}", path);
                return resources;
            }

            if (resourceUrl.getProtocol().equals("jar")) {
                String jarPath = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));
                try (FileSystem fileSystem = FileSystems.newFileSystem(Paths.get(jarPath), classLoader)) {
                    Path jarResourcePath = fileSystem.getPath(path);
                    try (Stream<Path> paths = Files.walk(jarResourcePath)) {
                        paths.filter(Files::isRegularFile)
                                .filter(p -> p.toString().endsWith(extension))
                                .forEach(p -> {
                                    String fileName = p.getFileName().toString();
                                    String id = fileName.substring(0, fileName.lastIndexOf(extension));
                                    String relativePath = path.substring("assets/custommobsforge/".length()) + "/" + fileName;
                                    String fullResourcePath = "custommobsforge:" + relativePath;
                                    resources.add(new ResourceEntry(id, fullResourcePath));
                                    LOGGER.info("Manually added resource: id={}, path={}", id, fullResourcePath);
                                });
                    }
                }
            } else {
                Path resourcePath = Paths.get(resourceUrl.toURI());
                try (Stream<Path> paths = Files.walk(resourcePath)) {
                    paths.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(extension))
                            .forEach(p -> {
                                String fileName = p.getFileName().toString();
                                String id = fileName.substring(0, fileName.lastIndexOf(extension));
                                String relativePath = path.substring("assets/custommobsforge/".length()) + "/" + fileName;
                                String fullResourcePath = "custommobsforge:" + relativePath;
                                resources.add(new ResourceEntry(id, fullResourcePath));
                                LOGGER.info("Manually added resource: id={}, path={}", id, fullResourcePath);
                            });
                }
            }

            if (resources.isEmpty()) {
                LOGGER.warn("No resources found during manual scan in path: {} with extension: {}", path, extension);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to manually scan resources in path: {}", path, e);
        }
        return resources;
    }

    private static void saveConfig() {
        try {
            File parentDir = CONFIG_FILE.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (DataOutputStream output = new DataOutputStream(new FileOutputStream(CONFIG_FILE))) {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                writeResourceList(buf, instance.models);
                writeResourceList(buf, instance.animations);
                writeResourceList(buf, instance.textures);
                output.write(buf.array(), 0, buf.readableBytes());
                LOGGER.info("Saved resources config to: {}", CONFIG_FILE.getPath());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save resources config", e);
        }
    }

    private static void writeResourceList(FriendlyByteBuf buf, List<ResourceEntry> resources) {
        buf.writeInt(resources.size());
        for (ResourceEntry entry : resources) {
            entry.writeToBuf(buf);
        }
    }

    private static List<ResourceEntry> readResourceList(FriendlyByteBuf buf) {
        int size = buf.readInt();
        if (size < 0) {
            throw new IllegalArgumentException("Invalid resource list size: " + size);
        }
        List<ResourceEntry> resources = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            if (buf.readableBytes() < 2) {
                throw new IllegalArgumentException("Not enough data to read resource entry at index " + i);
            }
            resources.add(ResourceEntry.readFromBuf(buf));
        }
        return resources;
    }

    public static boolean validateResources(String model, String animation, String texture) {
        boolean modelValid = instance.models.stream().anyMatch(entry -> entry.id.equals(model));
        boolean animationValid = instance.animations.stream().anyMatch(entry -> entry.id.equals(animation));
        boolean textureValid = instance.textures.stream().anyMatch(entry -> entry.id.equals(texture));

        if (!modelValid) {
            LOGGER.warn("Model not found on server: {}", model);
        }
        if (!animationValid) {
            LOGGER.warn("Animation not found on server: {}", animation);
        }
        if (!textureValid) {
            LOGGER.warn("Texture not found on server: {}", texture);
        }

        return modelValid && animationValid && textureValid;
    }

    public static ResourceConfig getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    public List<ResourceEntry> getModels() {
        return models;
    }

    public List<ResourceEntry> getAnimations() {
        return animations;
    }

    public List<ResourceEntry> getTextures() {
        return textures;
    }
}