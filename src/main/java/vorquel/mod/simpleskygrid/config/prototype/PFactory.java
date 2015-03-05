package vorquel.mod.simpleskygrid.config.prototype;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import net.minecraft.util.ChunkCoordinates;
import vorquel.mod.simpleskygrid.SimpleSkyGrid;
import vorquel.mod.simpleskygrid.config.prototype.count.PNormalCount;
import vorquel.mod.simpleskygrid.config.prototype.count.PSingleCount;
import vorquel.mod.simpleskygrid.config.prototype.count.PUniformCount;
import vorquel.mod.simpleskygrid.config.prototype.generation.PBlock;
import vorquel.mod.simpleskygrid.config.prototype.generation.PEntity;
import vorquel.mod.simpleskygrid.config.prototype.point.PCirclePoint;
import vorquel.mod.simpleskygrid.config.prototype.point.PNormalPoint;
import vorquel.mod.simpleskygrid.config.prototype.point.PSinglePoint;
import vorquel.mod.simpleskygrid.config.prototype.point.PUniformPoint;
import vorquel.mod.simpleskygrid.world.igenerated.IGeneratedObject;
import vorquel.mod.simpleskygrid.world.igenerated.random.IRandom;

import java.io.IOException;

public class PFactory {

    public static IPrototype<IGeneratedObject> readGeneratedObject(JsonReader jsonReader) throws IOException {
        IPrototype<IGeneratedObject> prototype = PNull.generatedObject;
        jsonReader.beginObject();
        String TYPE = jsonReader.nextName();
        if(!TYPE.equals("type")) {
            SimpleSkyGrid.logger.fatal(String.format("\"type\" expected in config file, found \"%s\"", TYPE));
            throw new RuntimeException(String.format("\"type\" expected in config file, found \"%s\"", TYPE));
        }
        String type = jsonReader.nextString();
        switch (type) {
            case "label":  prototype = new PLabel<>(jsonReader);  break;
            case "block":  prototype = new PBlock(jsonReader);    break;
            case "entity": prototype = new PEntity(jsonReader);   break;
            default:
                SimpleSkyGrid.logger.warn(String.format("Unknown generation type %s in config file", type));
                while (jsonReader.hasNext()) {
                    jsonReader.nextName();
                    jsonReader.skipValue();
                }
        }
        jsonReader.endObject();
        return prototype;
    }

    public static IPrototype<IRandom<Integer>> readCount(JsonReader jsonReader) throws IOException {
        IPrototype<IRandom<Integer>> prototype = PNull.count;
        if(jsonReader.peek() == JsonToken.NUMBER)
            return new PSingleCount(jsonReader);
        else {
            jsonReader.beginObject();
            String TYPE = jsonReader.nextName();
            if(!TYPE.equals("type")) {
                SimpleSkyGrid.logger.fatal(String.format("\"type\" expected in config file, found \"%s\"", TYPE));
                throw new RuntimeException(String.format("\"type\" expected in config file, found \"%s\"", TYPE));
            }
            String type = jsonReader.nextString();
            switch (type) {
                case "normal":  prototype = new PNormalCount(jsonReader);  break;
                case "uniform": prototype = new PUniformCount(jsonReader); break;
                default:
                    SimpleSkyGrid.logger.warn(String.format("Unknown count type %s in config file", type));
                    while (jsonReader.hasNext()) {
                        jsonReader.nextName();
                        jsonReader.skipValue();
                    }
            }
            jsonReader.endObject();
        }
        return prototype;
    }

    public static IPrototype<IRandom<ChunkCoordinates>> readPoint(JsonReader jsonReader) throws IOException {
        IPrototype<IRandom<ChunkCoordinates>> prototype = PNull.point;
        jsonReader.beginObject();
        String TYPE = jsonReader.nextName();
        if(!TYPE.equals("type")) {
            SimpleSkyGrid.logger.fatal(String.format("\"type\" expected in config file, found \"%s\"", TYPE));
            throw new RuntimeException(String.format("\"type\" expected in config file, found \"%s\"", TYPE));
        }
        String type = jsonReader.nextString();
        switch (type) {
            case "point":   prototype = new PSinglePoint(jsonReader);  break;
            case "circle":  prototype = new PCirclePoint(jsonReader);  break;
            case "normal":  prototype = new PNormalPoint(jsonReader);  break;
            case "uniform": prototype = new PUniformPoint(jsonReader); break;
            default:
                SimpleSkyGrid.logger.warn(String.format("Unknown location type %s in config file", type));
                while (jsonReader.hasNext()) {
                    jsonReader.nextName();
                    jsonReader.skipValue();
                }
        }
        jsonReader.endObject();
        return prototype;
    }
}
