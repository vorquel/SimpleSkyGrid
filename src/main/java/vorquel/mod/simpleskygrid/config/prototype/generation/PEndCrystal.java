package vorquel.mod.simpleskygrid.config.prototype.generation;

import com.google.gson.stream.JsonReader;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import vorquel.mod.simpleskygrid.SimpleSkyGrid;
import vorquel.mod.simpleskygrid.config.prototype.Prototype;
import vorquel.mod.simpleskygrid.world.igenerated.GeneratedBlock;
import vorquel.mod.simpleskygrid.world.igenerated.GeneratedComplex;
import vorquel.mod.simpleskygrid.world.igenerated.GeneratedEntity;
import vorquel.mod.simpleskygrid.world.igenerated.IGeneratedObject;

import java.io.IOException;

public class PEndCrystal extends Prototype<IGeneratedObject> {
    public PEndCrystal(JsonReader jsonReader) throws IOException {
        super(jsonReader);
    }

    @Override
    protected void readLabel(JsonReader jsonReader, String label) throws IOException {
        SimpleSkyGrid.logger.warn("What are you doing? End crystals have no extra data.");
        jsonReader.skipValue();
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public IGeneratedObject getObject() {
        GeneratedComplex complex = new GeneratedComplex();
        complex.put(new ChunkCoordinates(0, 0, 0), new GeneratedBlock(Blocks.bedrock, 0, null));
        complex.put(new ChunkCoordinates(0, 1, 0), new GeneratedEntity("", null));
        return complex;
    }
}