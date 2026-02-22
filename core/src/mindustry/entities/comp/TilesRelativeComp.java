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
import static mindustry.world.TilesHandler.*;

@Component
abstract class TilesRelativeComp implements Posc, AbsolutePos{
    transient Tiles tiles;
    transient float absoluteX, absoluteY;

    public void updateAbsolute(){
        if(tiles.craft == null){
            absoluteX = getX();
            absoluteY = getY();
            return;
        }
        v2p.set(getX(), getY()).mul(tiles.craft.trans());
        absoluteX = v2p.x;
        absoluteY = v2p.y;
    }

    public Vec2 getAbsolute(float x, float y){
        return v2p.set(x, y).mul(tiles.craft.trans());
    }

    @Override
    public float getAbsoluteX(){
        return absoluteX;
    }

    @Override
    public float getAbsoluteY(){
        return absoluteY;
    }

    @Replace
    @Override
    public float angleTo(Position other){
        if(other instanceof AbsolutePos abs){
            return Angles.angle(getAbsoluteX(), getAbsoluteY(), abs.getAbsoluteX(), abs.getAbsoluteY());
        }
        return Angles.angle(getAbsoluteX(), getAbsoluteY(), other.getX(), other.getY());
    }

    @Replace
    @Override
    public float angleTo(float x, float y){
        return Angles.angle(getAbsoluteX(), getAbsoluteY(), x, y);
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
        float dx = getAbsoluteX() - x;
        float dy = getAbsoluteY() - y;
        return Mathf.sqrt(dx * dx + dy * dy);
    }

    @Replace
    @Override
    public float dst2(float x, float y){
        float dx = getAbsoluteX() - x;
        float dy = getAbsoluteY() - y;
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
        return Mathf.dst2(getAbsoluteX(), getAbsoluteY(), x, y) <= dst * dst;
    }

}
