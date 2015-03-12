package vorquel.mod.simpleskygrid.config;

import vorquel.mod.simpleskygrid.SimpleSkyGrid;
import vorquel.mod.simpleskygrid.config.prototype.IPrototype;
import vorquel.mod.simpleskygrid.config.prototype.PFactory;
import vorquel.mod.simpleskygrid.config.prototype.PNull;
import vorquel.mod.simpleskygrid.world.generated.IGeneratedObject;
import vorquel.mod.simpleskygrid.world.loot.ILootSource;

import java.util.HashMap;

public class Config {

    public static HashMap<Integer, DimensionProperties> dimensionPropertiesMap = new HashMap<>();
    public static ConfigDataMap<IPrototype<IGeneratedObject>, Double> generationData = new ConfigDataMap<>();
    public static ConfigDataMap<IPrototype<IGeneratedObject>, UniqueQuantity> uniqueGenData = new ConfigDataMap<>();
    public static LootLocationData lootLocationData = new LootLocationData();

    public static void loadConfigs() {
        SimpleSkyGridConfigReader reader = new SimpleSkyGridConfigReader("SimpleSkyGrid");
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            switch(name) {
                case "generation":     readGeneration(reader);    break;
                case "unique_gen":     readUniqueGen(reader);     break;
                case "loot_placement": readLootPlacement(reader); break;
                case "loot":           readLoot(reader);          break;
                default:
                    if(name.startsWith("dim"))
                        readDimension(reader, name);
                    else {
                        reader.unknownOnce("label " + name, "top level objects");
                    }
            }
        }
        reader.endObject();
        reader.close();
    }

    private static void readDimension(SimpleSkyGridConfigReader reader, String dimName) {
        int dim;
        try {
            dim = Integer.decode(dimName.substring(3));
        } catch(NumberFormatException e) {
            SimpleSkyGrid.logger.warn(String.format("Unknown label %s in config file", dimName));
            reader.skipValue();
            return;
        }
        DimensionProperties prop = new DimensionProperties();
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            switch(name) {
                case "height":         prop.height             = reader.nextInt();    break;
                case "radius":         prop.radius             = reader.nextInt();    break;
                case "spawn_height":   prop.spawnHeight        = reader.nextInt();    break;
                case "generation":     prop.generationLabel    = reader.nextString(); break;
                case "unique_gen":     prop.uniqueGenLabel     = reader.nextString(); break;
                case "loot_placement": prop.lootPlacementLabel = reader.nextString(); break;
                default:
                    SimpleSkyGrid.logger.warn(String.format("Unknown label %s in config file", name));
                    reader.skipValue();
            }
        }
        reader.endObject();
        if(prop.height == -1 || prop.generationLabel == null) {
            SimpleSkyGrid.logger.fatal(String.format("Underspecified dimension %d in config file", dim));
            throw new RuntimeException(String.format("Underspecified dimension %d in config file", dim));
        }
        dimensionPropertiesMap.put(dim, prop);
    }

    private static void readGeneration(SimpleSkyGridConfigReader reader) {
        reader.beginObject();
        while(reader.hasNext()) {
            String label = reader.nextName();
            reader.beginArray();
            while(reader.hasNext()) {
                reader.beginObject();
                IPrototype<IGeneratedObject> prototype = PNull.generatedObject;
                double weight = 0;
                while(reader.hasNext()) {
                    String innerLabel = reader.nextName();
                    switch(innerLabel) {
                        case "object": prototype = PFactory.readGeneratedObject(reader); break;
                        case "weight": weight    = readWeight(reader);                   break;
                        default:
                            SimpleSkyGrid.logger.warn(String.format("Unknown generation label %s in config file", innerLabel));
                            reader.skipValue();
                    }
                }
                if(prototype.isComplete() && weight > 0)
                    generationData.put(label, prototype, weight);
                reader.endObject();
            }
            reader.endArray();
        }
        reader.endObject();
    }

    private static void readUniqueGen(SimpleSkyGridConfigReader reader) {
        reader.beginObject();
        while(reader.hasNext()) {
            String label = reader.nextName();
            reader.beginArray();
            while(reader.hasNext()) {
                reader.beginObject();
                IPrototype<IGeneratedObject> prototype = PNull.generatedObject;
                UniqueQuantity quantity = new UniqueQuantity();
                while(reader.hasNext()) {
                    String innerLabel = reader.nextName();
                    switch(innerLabel) {
                        case "object":   prototype = PFactory.readGeneratedObject(reader); break;
                        case "count":    quantity.countSource = PFactory.readCount(reader).getObject(); break;
                        case "location": quantity.pointSource = PFactory.readPoint(reader).getObject(); break;
                        default:
                            SimpleSkyGrid.logger.warn(String.format("Unknown uniqueGen label %s in config file", innerLabel));
                            reader.skipValue();
                    }
                }
                if(prototype.isComplete() && quantity.isComplete())
                    uniqueGenData.put(label, prototype, quantity);
                reader.endObject();
            }
            reader.endArray();
        }
        reader.endObject();
    }

    private static void readLootPlacement(SimpleSkyGridConfigReader reader) {
        reader.beginObject();
        while(reader.hasNext()) {
            String label = reader.nextName();
            reader.beginArray();
            while(reader.hasNext()) {
                reader.beginObject();
                String TARGET = reader.nextName();
                if(!TARGET.equals("target")) {
                    SimpleSkyGrid.logger.fatal(String.format("\"target\" expected in config file, found \"%s\"", TARGET));
                    throw new RuntimeException(String.format("\"target\" expected in config file, found \"%s\"", TARGET));
                }
                IPrototype<IGeneratedObject> target = PFactory.readGeneratedObject(reader);
                String LOOT = reader.nextName();
                if(!LOOT.equals("loot")) {
                    SimpleSkyGrid.logger.fatal(String.format("\"loot\" expected in config file, found \"%s\"", LOOT));
                    throw new RuntimeException(String.format("\"loot\" expected in config file, found \"%s\"", LOOT));
                }
                reader.beginArray();
                while(reader.hasNext()) {
                    String innerLabel = reader.nextName();
                    IPrototype<ILootSource> lootSource = PNull.lootSource;
                    double weight = 0;
                    switch(innerLabel) {
                        case "object": lootSource = PFactory.readLootSource(reader); break;
                        case "weight": weight = readWeight(reader);
                    }
                    if(lootSource.isComplete() && weight > 0)
                        lootLocationData.put(label, target, lootSource, weight);
                }
                reader.endArray();
            }
            reader.endArray();
        }
    }

    private static void readLoot(SimpleSkyGridConfigReader reader) {
        reader.skipValue();
    }

    private static double readWeight(SimpleSkyGridConfigReader reader) {
        double weight = reader.nextDouble();
        if(weight < 0) {
            SimpleSkyGrid.logger.error("Negative weight in config file");
            weight = 0;
        } else if(Double.isInfinite(weight) || Double.isNaN(weight)) {
            SimpleSkyGrid.logger.error("Crazy weight in config file");
            weight = 0;
        }
        return weight;
    }

    public static class DimensionProperties {
        public int    height             = -1;
        public int    radius             = -1;
        public int    spawnHeight        = 65;
        public String generationLabel    = null;
        public String uniqueGenLabel     = null;
        public String lootPlacementLabel = null;

        public boolean isFinite() {
            return radius != -1;
        }

        public boolean inRadius(int xChunk, int zChunk) {
            int xAbs = xChunk < 0 ? -xChunk : xChunk + 1;
            int zAbs = zChunk < 0 ? -zChunk : zChunk + 1;
            return xAbs <= radius && zAbs <= radius;
        }
    }
}
