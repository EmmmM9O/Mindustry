package mindustry.world.tiles;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.content.*;
import mindustry.world.*;

import java.io.*;
import java.util.*;

public class MutableTiles extends Tiles{
    public static int max = 250;
    public static int w = max * 2 + 1;

    public static int hash(int x, int y){
        return (y + max) * w + x + max;
    }

    final IntMap<Tile> map;
    final IntMap<Puddle> puddles;
    final IntMap<Fire> fires;

    public MutableTiles(){
        super(0, 0);
        type = 2;
        map = new IntMap<>();
        puddles = new IntMap<>();
        fires = new IntMap<>();
    }

    @Override
    public Puddle getPuddle(int pos){
        return puddles.get(pos);
    }

    @Override
    public void setPuddle(int pos, Puddle p){
        puddles.put(pos, p);
        updateWH(pos % w - max, pos / w - max);
    }

    @Nullable
    @Override
    public Fire getFire(int pos){
        return fires.get(pos);
    }

    @Override
    public void setFire(int pos, Fire f){
        fires.put(pos, f);
        updateWH(pos % w - max, pos / w - max);
    }

    @Override
    public void each(Intc2 cons){
        IntMap.Keys keys = map.keys();
        while(keys.hasNext){
            int key = keys.next();
            cons.get(key % w - max, key / w - max);
        }
    }

    @Override
    public void fill(){
        IntMap.Keys keys = map.keys();
        while(keys.hasNext){
            int key = keys.next();
            map.put(key, new Tile(key % w - max, key / w - max, key, this));
        }
    }

    boolean init;

    int minX, maxX, minY, maxY;

    void updateWH(int x, int y){
        if(!init){
            minX = x;
            minY = y;
            maxX = x;
            maxY = y;
            init = true;
        }else{
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        width = maxX - minX + 1;
        height = maxY - minY + 1;
    }

    @Override
    public int getMinX(){
        return minX;
    }

    @Override
    public int getMaxX(){
        return maxX + 1;
    }

    @Override
    public int getMinY(){
        return minY;
    }

    @Override
    public int getMaxY(){
        return maxY + 1;
    }
    @Override
    public void set(int x, int y, Tile tile){
        int hash = hash(x, y);
        tile.tiles = this;
        tile.id = hash;
        map.put(hash, tile);
        updateWH(x, y);
    }

    @Override
    public void seti(int i, Tile tile){
        tile.tiles = this;
        tile.id = i;
        map.put(i, tile);
        updateWH(i % w, i / w);
    }

    @Override
    public Tile get(int x, int y){
        return map.get(hash(x, y), (Tile)null);
    }

    @Override
    public Tile geti(int idx){
        return map.get(idx);
    }

    @Override
    public void eachTile(Cons<Tile> cons){
        for(Tile value : map.values()){
            cons.get(value);
        }
    }

    @Override
    public Iterator<Tile> iterator(){
        return map.values().iterator();
    }

    @Override
    public void write(DataOutput stream, SaveVersion version) throws IOException{
        stream.writeShort(getMinX());
        stream.writeShort(getMinY());
        stream.writeShort(getMaxX());
        stream.writeShort(getMaxY());
        for(int y = getMinY(); y <= getMaxY(); y++){
            for(int x = getMinX(); x <= getMaxX(); x++){
                Tile tile = get(x, y);
                if(tile == null){
                    stream.writeByte(0);
                    continue;
                }
                stream.writeByte(1);

                stream.writeShort(tile.floorID());
                stream.writeShort(tile.overlayID());

                writeBlock(stream, tile, version);
            }
        }
    }

    @Override
    public void read(DataInput stream, SaveVersion version) throws IOException{
        minX = stream.readShort();
        minY = stream.readShort();
        maxX = stream.readShort();
        maxY = stream.readShort();
        for(int y = getMinY(); y <= getMaxY(); y++){
            for(int x = getMinX(); x <= getMaxX(); x++){
                byte type = stream.readByte();
                if(type == 0) continue;
                short floorid = stream.readShort();
                short oreid = stream.readShort();
                Tile tile = create(x, y, floorid, oreid, (short)0);

                readBlock(stream, tile, version);
            }
        }
        endLoad();
    }
}
