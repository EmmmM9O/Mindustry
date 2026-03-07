package mindustry.world;

import arc.struct.*;
import arc.math.*;
import arc.func.*;
import arc.math.geom.*;
import arc.math.geom.Geometry.*;
import mindustry.world.tiles.*;

public class TilesHandler{
    public static Mat m1 = new Mat(), m2 = new Mat(), m3 = new Mat();
    public static Vec2 v2 = new Vec2();
    public static Vec2 v2p = new Vec2();
    public static Vec2[] v24 = {new Vec2(), new Vec2(), new Vec2(), new Vec2()};
    public static IntMap<Prov<Tiles>> tilesType = IntMap.<Prov<Tiles>>of(
    0, (Prov<Tiles>)() -> null,
    1, (Prov<Tiles>)WorldTiles::new,
    2, (Prov<Tiles>)MutableTiles::new
    );
    public static Seq<Tiles> tTiles = Seq.with();

    public static Rect rect(Rect rect, Mat mat){
        v24[0].set(rect.x, rect.y);
        v24[1].set(rect.x + rect.width, rect.y);
        v24[2].set(rect.x + rect.width, rect.y + rect.height);
        v24[3].set(rect.x, rect.y + rect.height);
        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for(int i = 0; i < 4; i++){
            v24[i].mul(mat);
            minX = Math.min(minX, v24[i].x);
            maxX = Math.max(maxX, v24[i].x);
            minY = Math.min(minY, v24[i].y);
            maxY = Math.max(maxY, v24[i].y);
        }
        rect.x = minX;
        rect.y = minY;
        rect.width = maxX - minX;
        rect.height = maxY - minY;
        return rect;
    }
}
