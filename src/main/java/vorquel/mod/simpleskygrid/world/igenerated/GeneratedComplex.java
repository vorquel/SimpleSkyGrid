package vorquel.mod.simpleskygrid.world.igenerated;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Random;

public class GeneratedComplex implements IGeneratedObject {

    protected HashMap<ChunkCoordinates, IGeneratedObject> generationMap = new HashMap<>();

    public IGeneratedObject put(ChunkCoordinates key, IGeneratedObject value) {
        return generationMap.put(key, value);
    }

    @Override
    public void provideObject(Random random, World world, Chunk chunk, int x, int y, int z) {
        for(ChunkCoordinates key : generationMap.keySet()) {
            generationMap.get(key).provideObject(random, world, chunk, x + key.posX, y + key.posY, z + key.posZ);
        }
    }
}