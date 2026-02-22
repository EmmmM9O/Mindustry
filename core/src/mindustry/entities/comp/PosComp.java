package mindustry.entities.comp;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.math.geom.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

import static mindustry.Vars.*;

@Component
abstract class PosComp implements Position{
    @SyncField(true) @SyncLocal float x, y;

    void set(float x, float y){
        this.x = x;
        this.y = y;
    }

    void set(Position pos){
        set(pos.getX(), pos.getY());
    }

    void trns(float x, float y){
        set(this.x + x, this.y + y);
    }

    void trns(Position pos){
        trns(pos.getX(), pos.getY());
    }

    int tileX(){
        return World.toTile(x);
    }

    int tileY(){
        return World.toTile(y);
    }

    /** Returns air if this unit is on a non-air top block. */
    Floor floorOn(){
        Tile tile = tileOn();
        return tile == null || tile.block() != Blocks.air ? (Floor)Blocks.air : tile.floor();
    }

    Block blockOn(){
        Tile tile = tileOn();
        return tile == null ? Blocks.air : tile.block();
    }

    @Nullable
    Building buildOn(){
        return world.buildWorld(x, y);
    }

    @Nullable
    Tile tileOn(){
        return world.tileWorld(x, y);
    }

    boolean onSolid(){
        Tile tile = tileOn();
        return tile == null || tile.solid();
    }

    @Override
    public float getX(){
        return x;
    }

    @Override
    public float getY(){
        return y;
    }

    @Replace
    @Override
    public float angleTo(Position other){
        if(other instanceof AbsolutePos abs){
            return Angles.angle(getX(), getY(), abs.getAbsoluteX(), abs.getAbsoluteY());
        }
        return Angles.angle(getX(), getY(), other.getX(), other.getY());
    }

    @Replace
    @Override
    public float dst(Position other){
        if(other instanceof AbsolutePos abs){
            return dst(abs.getAbsoluteX(), abs.getAbsoluteY());
        }
        return dst(other.getX(), other.getY());
    }

    @Replace
    @Override
    public float dst2(Position other){
        if(other instanceof AbsolutePos abs){
            return dst2(abs.getAbsoluteX(), abs.getAbsoluteY());
        }
        return dst2(other.getX(), other.getY());
    }

    @Replace
    @Override
    public float dst(float x, float y){
        float dx = getX() - x;
        float dy = getY() - y;
        return Mathf.sqrt(dx * dx + dy * dy);
    }

    @Replace
    @Override
    public float dst2(float x, float y){
        float dx = getX() - x;
        float dy = getY() - y;
        return dx * dx + dy * dy;
    }


    @Replace
    @Override
    public boolean within(Position other, float dst){
        if(other instanceof AbsolutePos abs){
            return within(abs.getAbsoluteX(), abs.getAbsoluteY(), dst);
        }
        return within(other.getX(), other.getY(), dst);
    }

    @Replace
    @Override
    public boolean within(float x, float y, float dst){
        return Mathf.dst2(getX(), getY(), x, y) <= dst * dst;
    }

}
