package mindustry.graphics;

import arc.graphics.gl.*;
import mindustry.world.*;

public abstract class FloorRendererI{
    public abstract IndexData getIndexData();

    public abstract float[] getVertexBuffer();

    /** Queues up a cache change for a tile. Only runs in render loop. */
    public abstract void recacheTile(Tile tile);

    public abstract void recacheTile(int x, int y);

    public abstract void drawFloor();

    public abstract void checkChanges();

    public abstract void checkChanges(boolean ignoreWalls);

    public abstract void drawUnderwater(Runnable run);

    public abstract void beginDraw();

    public abstract void drawLayer(CacheLayer layer);

    public void reload(){
        reload(false);
    }

    public abstract void reload(boolean ignoreWalls);
}
