package mindustry.math.geom;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;

public interface AbsolutePos extends Position{
    float getAbsoluteX();

    float getAbsoluteY();

    @Override
    default float angleTo(Position other){
        if(other instanceof AbsolutePos abs){
            return Angles.angle(getAbsoluteX(), getAbsoluteY(), abs.getAbsoluteX(), abs.getAbsoluteY());
        }
        return Angles.angle(getAbsoluteX(), getAbsoluteY(), other.getX(), other.getY());
    }

    @Override
    default float angleTo(float x, float y){
        return Angles.angle(getAbsoluteX(), getAbsoluteY(), x, y);
    }

    @Override
    default float dst(Position other){
        if(other instanceof AbsolutePos abs){
            return dst(abs.getAbsoluteX(), abs.getAbsoluteY());
        }
        return dst(other.getX(), other.getY());
    }

    @Override
    default float dst2(Position other){
        if(other instanceof AbsolutePos abs){
            return dst2(abs.getAbsoluteX(), abs.getAbsoluteY());
        }
        return dst2(other.getX(), other.getY());
    }

    @Override
    default float dst(float x, float y){
        float dx = getAbsoluteX() - x;
        float dy = getAbsoluteY() - y;
        return Mathf.sqrt(dx * dx + dy * dy);
    }

    @Override
    default float dst2(float x, float y){
        float dx = getAbsoluteX() - x;
        float dy = getAbsoluteY() - y;
        return dx * dx + dy * dy;
    }

    @Override
    default boolean within(Position other, float dst){
        if(other instanceof AbsolutePos abs){
            return within(abs.getAbsoluteX(), abs.getAbsoluteY(), dst);
        }
        return within(other.getX(), other.getY(), dst);
    }

    @Override
    default boolean within(float x, float y, float dst){
        return Mathf.dst2(getAbsoluteX(), getAbsoluteY(), x, y) <= dst * dst;
    }

    default boolean relWithin(float x, float y, float dst){
        return Mathf.dst2(getX(), getY(), x, y) <= dst * dst;
    }
}
