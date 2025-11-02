package mindustry.graphics;

import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import mindustry.world.*;
import mindustry.gen.*;

public abstract class BlockRendererI {
    public final FloorRenderer floor = new FloorRenderer();

    public abstract void reload();

    public abstract void updateShadows(boolean ignoreBuildings, boolean ignoreTerrain);

    public abstract void updateDarkness();

    public abstract void invalidateTile(Tile tile);
    
    public abstract FrameBuffer getShadowBuffer();

    public abstract void removeFloorIndex(Tile tile);

    public abstract void addFloorIndex(Tile tile);

    public abstract void processShadows();

    public abstract void updateShadow(Building build);

    public abstract void updateShadowTile(Tile tile);

    public abstract void recacheWall(Tile tile);

    public abstract TextureRegion[][] getCracks(Building building);
}
