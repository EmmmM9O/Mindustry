package mindustry.world.tiles;

import arc.func.*;
import arc.math.*;
import arc.util.*;
import arc.math.geom.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.world.*;
import mindustry.content.*;

import java.io.*;
import java.util.*;

import static mindustry.Vars.*;

public class WorldTiles extends Tiles{
    Tile[] array;
    Puddle[] puddles;
    Fire[] fires;

    public WorldTiles(){
        super(0, 0);
        type = 1;
    }

    public WorldTiles(int width, int height){
        super(width, height);
        type = 1;
        this.array = new Tile[width * height];
        this.puddles = new Puddle[width * height];
        this.fires = new Fire[width * height];
    }

    @Override
    public void reset(int width, int height){
        this.width = width;
        this.height = height;
        this.array = new Tile[width * height];
        this.puddles = new Puddle[width * height];
        this.fires = new Fire[width * height];
    }

    @Override
    public Puddle getPuddle(int pos){
        return puddles[pos];
    }

    @Override
    public void setPuddle(int pos, Puddle p){
        puddles[pos] = p;
    }

    @Nullable
    @Override
    public Fire getFire(int pos){
        return fires[pos];
    }

    @Override
    public void setFire(int pos, Fire f){
        fires[pos] = f;
    }

    @Override
    public void each(Intc2 cons){
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                cons.get(x, y);
            }
        }
    }

    @Override
    public void fill(){
        for(int i = 0; i < array.length; i++){
            array[i] = new Tile(i % width, i / width, i, this);
        }
    }

    @Override
    public void set(int x, int y, Tile tile){
        tile.tiles = this;
        tile.id = y * width + x;
        array[y * width + x] = tile;
    }

    @Override
    public void seti(int i, Tile tile){
        tile.tiles = this;
        tile.id = i;
        array[i] = tile;
    }

    @Nullable
    @Override
    public Tile get(int x, int y){
        return (x < 0 || x >= width || y < 0 || y >= height) ? null : array[y * width + x];
    }

    @Override
    public Tile geti(int idx){
        return array[idx];
    }

    @Override
    public void eachTile(Cons<Tile> cons){
        for(Tile tile : array){
            cons.get(tile);
        }
    }

    @Override
    public void write(DataOutput stream, SaveVersion version) throws IOException{
        //write world size
        stream.writeShort(width);
        stream.writeShort(height);

        //floor + overlay
        for(int i = 0; i < width * height; i++){
            Tile tile = geti(i);
            stream.writeShort(tile.floorID());
            stream.writeShort(tile.overlayID());
            int consecutives = 0;

            for(int j = i + 1; j < width * height && consecutives < 255; j++){
                Tile nextTile = getn(j % width, j / width);

                if(nextTile.floorID() != tile.floorID() || nextTile.overlayID() != tile.overlayID()){
                    break;
                }

                consecutives++;
            }

            stream.writeByte(consecutives);
            i += consecutives;
        }

        //blocks
        for(int i = 0; i < width * height; i++){
            Tile tile = geti(i);
            if(writeBlock(stream, tile, version)){
                //write consecutive non-entity blocks
                int consecutives = 0;

                for(int j = i + 1; j < width * height && consecutives < 255; j++){
                    Tile nextTile = getn(j % width, j / width);

                    if(nextTile.blockID() != tile.blockID() || tile.shouldSaveData() != nextTile.shouldSaveData()){
                        break;
                    }

                    consecutives++;
                }

                stream.writeByte(consecutives);
                i += consecutives;
            }
        }

    }

    @Override
    public void read(DataInput stream, SaveVersion version) throws IOException{
        int width = stream.readUnsignedShort();
        int height = stream.readUnsignedShort();
        reset(width, height);
        //read floor and create tiles first
        for(int i = 0; i < width * height; i++){
            int x = i % width, y = i / width;
            short floorid = stream.readShort();
            short oreid = stream.readShort();
            int consecutives = stream.readUnsignedByte();
            if(content.block(floorid) == Blocks.air) floorid = Blocks.stone.id;

            create(x, y, floorid, oreid, (short)0);

            for(int j = i + 1; j < i + 1 + consecutives; j++){
                int newx = j % width, newy = j / width;
                create(newx, newy, floorid, oreid, (short)0);
            }

            i += consecutives;
        }

        //read blocks
        for(int i = 0; i < width * height; i++){
            Block block = readBlock(stream, geti(i), version);
            if(block != null){
                int consecutives = stream.readUnsignedByte();

                for(int j = i + 1; j < i + 1 + consecutives; j++){
                    geti(j).setBlock(block);
                }

                i += consecutives;
            }
        }

        endLoad();
    }

    @Override
    public Iterator<Tile> iterator(){
        //iterating through the entire map is expensive anyway, so a new allocation doesn't make much of a difference
        return new TileIterator();
    }

    private class TileIterator implements Iterator<Tile>{
        int index = 0;

        TileIterator(){
        }

        @Override
        public boolean hasNext(){
            return index < array.length;
        }

        @Override
        public Tile next(){
            return array[index++];
        }
    }
}
