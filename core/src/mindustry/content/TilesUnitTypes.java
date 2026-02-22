package mindustry.content;

import mindustry.gen.*;
import mindustry.type.*;
import mindustry.annotations.Annotations.*;

public class TilesUnitTypes{
    //fixed
    public static @EntityDef({Unitc.class, TilesCraftc.class}) TilesUnitType fixedTiles, movableTiles;

    public static void load(){
        fixedTiles = new TilesUnitType("fixed-tiles"){{
            speed = 0f;
            hitSize = 0f;
            health = 10;
            rotateSpeed = 0f;
            itemCapacity = 0;
            internal = true;
            defaultHeight = 0f;
            physics = false;

            hittable = false;
            killable = false;
            targetable = false;

            noHitbox = true;
        }};
        movableTiles = new TilesUnitType("movable-tiles"){{
            internal = true;

            speed = 1.5f;
            accel = 0.08f;
            drag = 0.04f;
            hitSize = 9f;
            health = 100;
            itemCapacity = 0;
            defaultHeight = 16f;
            flying = true;

            noHitbox = true;
        }};
    }
}
