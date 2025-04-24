package com.custommobsforge.custommobsforge.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ResourceConfig {
    private static final Logger LOGGER = LogManager.getLogger("CustomMobsForge");
    private static final File CONFIG_FILE = new File("config/custommobsforge/resources.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ResourceConfig instance;

    public static class ResourceEntry {
        public String id;
        public String path;

        public ResourceEntry(String id, String path) {
            this.id = id;
            this.path = path;
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

        // Пробуем загрузить данные из файла
        if (CONFIG_FILE.exists()) {
            try (Reader reader = new FileReader(CONFIG_FILE)) {
                ResourceConfig loadedConfig = GSON.fromJson(reader, ResourceConfig.class);
                if (loadedConfig != null) {
                    instance.models = loadedConfig.models != null ? loadedConfig.models : new ArrayList<>();
                    instance.animations = loadedConfig.animations != null ? loadedConfig.animations : new ArrayList<>();
                    instance.textures = loadedConfig.textures != null ? loadedConfig.textures : new ArrayList<>();
                    LOGGER.info("Loaded resources from config file: {}", CONFIG_FILE.getPath());
                    LOGGER.info("Loaded {} models: {}", instance.models.size(), instance.models);
                    LOGGER.info("Loaded {} animations: {}", instance.animations.size(), instance.animations);
                    LOGGER.info("Loaded {} textures: {}", instance.textures.size(), instance.textures);
                    return; // Если файл успешно загружен, пропускаем сканирование
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load resources config from file: {}", CONFIG_FILE.getPath(), e);
            }
        }

        // Если файла нет или загрузка не удалась, сканируем ресурсы
        scanResourceFolders();
        saveConfig();

        LOGGER.info("Initialized resources config: {}", CONFIG_FILE.getPath());
    }

    private static void scanResourceFolders() {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            LOGGER.error("Cannot scan resources: MinecraftServer is not available. Ensure this is called after server initialization.");
            return;
        }

        ResourceManager resourceManager = ServerLifecycleHooks.getCurrentServer().getResourceManager();
        LOGGER.info("ResourceManager: {}", resourceManager.getClass().getSimpleName());
        LOGGER.info("Available namespaces: {}", resourceManager.getNamespaces());

        instance.models = scanResources(resourceManager, "geo", ".geo.json");
        if (instance.models.isEmpty()) {
            LOGGER.warn("ResourceManager failed to find models, attempting manual scan...");
            instance.models = scanResourcesManually("assets/custommobsforge/geo", ".geo.json");
        }
        LOGGER.info("Found {} models: {}", instance.models.size(), instance.models);

        instance.animations = scanResources(resourceManager, "animations", ".animation.json");
        if (instance.animations.isEmpty()) {
            LOGGER.warn("ResourceManager failed to find animations, attempting manual scan...");
            instance.animations = scanResourcesManually("assets/custommobsforge/animations", ".animation.json");
        }
        LOGGER.info("Found {} animations: {}", instance.animations.size(), instance.animations);

        instance.textures = scanResources(resourceManager, "textures/entity", ".png");
        if (instance.textures.isEmpty()) {
            LOGGER.warn("ResourceManager failed to find textures, attempting manual scan...");
            instance.textures = scanResourcesManually("assets/custommobsforge/textures/entity", ".png");
        }
        LOGGER.info("Found {} textures: {}", instance.textures.size(), instance.textures);
    }

    private static List<ResourceEntry> scanResources(ResourceManager resourceManager, String path, String extension) {
        List<ResourceEntry> resources = new ArrayList<>();
        try {
            LOGGER.info("Scanning path with ResourceManager: assets/custommobsforge/{}", path);

            resourceManager.listResources("", fileName -> {
                if (fileName.getNamespace().equals("custommobsforge")) {
                    LOGGER.debug("Found resource in custommobsforge namespace: {}", fileName);
                }
                return fileName.getNamespace().equals("custommobsforge");
            }).forEach((resourceLocation, resource) -> {
                LOGGER.debug("Resource: {}", resourceLocation);
            });

            resourceManager.listResources(path, fileName -> {
                boolean matchesNamespace = fileName.getNamespace().equals("custommobsforge");
                boolean matchesExtension = fileName.getPath().endsWith(extension);
                LOGGER.debug("Checking resource: {} (namespace match: {}, extension match: {})",
                        fileName, matchesNamespace, matchesExtension);
                return matchesNamespace && matchesExtension;
            }).forEach((resourceLocation, resource) -> {
                String resourcePath = resourceLocation.getPath();
                LOGGER.debug("Found resource path: {}", resourcePath);
                if (resourcePath.endsWith(extension)) {
                    String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
                    String id = fileName.substring(0, fileName.lastIndexOf(extension));
                    String fullResourcePath = "custommobsforge:" + resourcePath;
                    resources.add(new ResourceEntry(id, fullResourcePath));
                    LOGGER.info("Added resource: id={}, path={}", id, fullResourcePath);
                }
            });

            if (resources.isEmpty()) {
                LOGGER.warn("No resources found in path: assets/custommobsforge/{} with extension: {}", path, extension);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to scan resources in path: assets/custommobsforge/{}", path, e);
        }
        return resources;
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
                if (parentDir.mkdirs()) {
                    LOGGER.info("Created config directory: {}", parentDir.getPath());
                } else {
                    LOGGER.error("Failed to create config directory: {}", parentDir.getPath());
                }
            }
            try (Writer writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(instance, writer);
                LOGGER.info("Saved resources config to: {}", CONFIG_FILE.getPath());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save resources config", e);
        }
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