package mindustry.world;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.math.geom.QuadTree.*;
import arc.struct.*;
import arc.util.*;
import mindustry.io.*;
import mindustry.gen.*;
import mindustry.content.*;
import mindustry.world.blocks.legacy.*;

import java.io.*;
import java.util.*;

import static mindustry.Vars.*;

/** A tile container. */
public abstract class Tiles implements Iterable<Tile>, QuadTreeObject{
    public int width;
    public int height;
    public int type;

    public Tiles(int width, int height){
        this.width = width;
        this.height = height;
    }

    public TilesCraftc craft;

    public abstract Puddle getPuddle(int pos);

    public abstract void setPuddle(int pos, Puddle p);

    public abstract @Nullable Fire getFire(int pos);

    public abstract void setFire(int pos, Fire f);

    public abstract void each(Intc2 cons);

    /** fills this tile set with empty air tiles. */
    public abstract void fill();

    /** set a tile at a position; does not range-check. use with caution. */
    public abstract void set(int x, int y, Tile tile);

    /** set a tile at a raw array position; used for fast iteration / 1-D for-loops */
    public abstract void seti(int i, Tile tile);

    /** @return whether these coordinates are in bounds */
    public boolean in(int x, int y){
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /** @return a tile at coordinates, or null if out of bounds */
    @Nullable
    public abstract Tile get(int x, int y);

    /** @return a tile at coordinates; throws an exception if out of bounds */
    public Tile getn(int x, int y){
        if(x < 0 || x >= width || y < 0 || y >= height) throw new IllegalArgumentException(x + ", " + y + " out of bounds: width=" + width + ", height=" + height);
        return get(x, y);
    }

    /** @return a tile at coordinates, clamped. */
    public Tile getc(int x, int y){
        x = Mathf.clamp(x, 0, width - 1);
        y = Mathf.clamp(y, 0, height - 1);
        return get(x, y);
    }

    /** @return a tile at an iteration index [0, width * height] */
    public abstract Tile geti(int idx);

    /** @return a tile at an int position (not equivalent to geti) */
    public @Nullable Tile getp(int pos){
        return get(Point2.x(pos), Point2.y(pos));
    }

    public abstract void eachTile(Cons<Tile> cons);

    public int id;

    public void reset(int width, int height){
    }

    @Nullable
    public Tile tile(int pos){
        return tile(Point2.x(pos), Point2.y(pos));
    }

    @Nullable
    public Tile tile(int x, int y){
        return get(x, y);
    }

    public Tile rawTile(int x, int y){
        return getn(x, y);
    }

    @Nullable
    public Building build(int x, int y){
        Tile tile = tile(x, y);
        if(tile == null) return null;
        return tile.build;
    }


    public int getMinX(){
        return 0;
    }
    
    public int getMaxX(){
        return width;
    }

    public int getMinY(){
        return 0;
    }

    public int getMaxY(){
        return height;
    }

    public float tilesize = 8f;

    @Nullable
    public Building build(int pos){
        Tile tile = tile(pos);
        if(tile == null) return null;
        return tile.build;
    }

    @Nullable
    public Tile tileWorld(float x, float y){
        return get(Math.round(x / tilesize), Math.round(y / tilesize));
    }

    public void removeCraft(){
        if(craft == null) return;
        craft.remove();
        craft.tiles(null);
        craft = null;
    }

    public void setCraft(TilesCraftc value){
        if(value == craft) return;
        if(craft != null) removeCraft();
        craft = value;
        if(craft != null) craft.tiles(this);
    }

    @Override
    public void hitbox(Rect out){
        if(craft == null) out.set(0f, 0f, width * tilesize, height * tilesize);
        else craft.hitbox(out);
    }

    public boolean writeBlock(DataOutput stream, Tile tile, SaveVersion version) throws IOException{
        stream.writeShort(tile.blockID());

        boolean savedata = tile.shouldSaveData();

        //in the old version, the second bit was set to indicate presence of data, but that approach was flawed - it didn't allow buildings + data on the same tile
        //so now the third bit is used instead
        byte packed = (byte)((tile.build != null ? 1 : 0) | (savedata ? 4 : 0));

        //make note of whether there was an entity or custom tile data here
        stream.writeByte(packed);

        if(savedata){
            //the new 'extra data' format writes 7 bytes of data instead of 1
            stream.writeByte(tile.data);
            stream.writeByte(tile.floorData);
            stream.writeByte(tile.overlayData);
            stream.writeInt(tile.extraData);
        }

        //only write the entity for multiblocks once - in the center
        if(tile.build != null){
            if(tile.isCenter()){
                stream.writeBoolean(true);
                version.writeChunk(stream, out -> {
                    out.b(tile.build.version());
                    tile.build.writeAll(out);
                });
            }else{
                stream.writeBoolean(false);
            }
        }else if(!savedata){ //don't write consecutive blocks when there is custom data
            return true;
        }
        return false;
    }

    public Block readBlock(DataInput stream, Tile tile, SaveVersion version) throws IOException{
        Block block = content.block(stream.readShort());
        if(block == null) block = Blocks.air;
        boolean isCenter = true;
        byte packedCheck = stream.readByte();
        boolean hadEntity = (packedCheck & 1) != 0;
        //data check (bit 3): 7 bytes (3x block-specific bytes + 1x 4-byte extra data int)
        boolean hadData = (packedCheck & 4) != 0;

        byte data = 0, floorData = 0, overlayData = 0;
        int extraData = 0;

        if(hadData){
            data = stream.readByte();
            floorData = stream.readByte();
            overlayData = stream.readByte();
            extraData = stream.readInt();
        }

        if(hadEntity){
            isCenter = stream.readBoolean();
        }

        //set block only if this is the center; otherwise, it's handled elsewhere
        if(isCenter){
            tile.setBlock(block);
        }

        //must be assigned after setBlock, because that can reset data
        if(hadData){
            tile.data = data;
            tile.floorData = floorData;
            tile.overlayData = overlayData;
            tile.extraData = extraData;
            onReadTileData();
        }

        if(hadEntity){
            if(isCenter){ //only read entity for center blocks
                if(block.hasBuilding()){
                    try{
                        version.readChunkReads(stream, (in, len) -> {
                            byte revision = in.b();
                            tile.build.readAll(in, revision);
                        });
                    }catch(Throwable e){
                        throw new IOException("Failed to read tile entity of block: " + block, e);
                    }
                }else{
                    //skip the entity region, as the entity and its IO code are now gone
                    version.skipChunk(stream);
                }

                onReadBuilding();
            }
        }else if(!hadData){
            return block;//never read consecutive blocks if there's data
        }
        return null;
    }

    public abstract void write(DataOutput stream, SaveVersion version) throws IOException;

    public abstract void read(DataInput stream, SaveVersion version) throws IOException;

    public Tile create(int x, int y, int floorID, int overlayID, int wallID){
        Tile tile = new Tile(x, y, floorID, overlayID, wallID, this);
        set(x, y, tile);
        return tile;
    }

    public void onReadTileData(){
    }

    public void onReadBuilding(){
    }

    public void endLoad(){
        for(Tile tile : this){
            //remove legacy blocks; they need to stop existing
            if(tile.block() instanceof LegacyBlock l){
                l.removeSelf(tile);
                continue;
            }

            if(tile.build != null){
                tile.build.updateProximity();
            }
        }
    }
}
