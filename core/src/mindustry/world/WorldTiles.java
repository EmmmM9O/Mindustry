package mindustry.world;

import arc.func.*;
import arc.math.*;
import arc.util.*;
import arc.math.geom.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;

import java.util.*;

public class WorldTiles extends Tiles{

    final Tile[] array;
    final Puddle[] puddles;
    final Fire[] fires;

    public WorldTiles(int width, int height){
        super(width, height);
        setFather(null);
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
        tile.id = y * width + x;
        array[y * width + x] = tile;
    }

    @Override
    public void seti(int i, Tile tile){
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
