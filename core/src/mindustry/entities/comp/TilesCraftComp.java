package mindustry.entities.comp;

import arc.util.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;

import mindustry.type.*;
import mindustry.gen.*;
import mindustry.annotations.Annotations.*;
import mindustry.game.*;
import mindustry.world.*;

import static mindustry.world.TilesHandler.*;

@Component
abstract class TilesCraftComp implements Unitc, TilesCraftc{
    @Import
    float x, y, height, rotation;
    @Import
    int id;
    @Import
    Team team;
    @Import
    UnitType type;
    Tiles tiles;
    int fatherId;

    @SyncField(true)
    @SyncLocal
    float originX, originY;
    @SyncField(true)
    @SyncLocal
    float offsetX, offsetY;

    transient TilesCraftc father;
    transient Seq<TilesCraftc> children = new Seq<>();

    transient int index__craft = -1;

    transient Mat trans = new Mat(), inv = new Mat();

    public TilesUnitType craftType(){
        return (TilesUnitType)type;
    }

    public void addChild(TilesCraftc child){
        child.setFather(self());
    }

    public void removeChild(TilesCraftc child){
        children.remove(child);
        child.setFather(father);
        m1.setToRotation(rotation);
        getTrans(m2, m1);
        v2.set(child.x(), child.y()).mul(m2);
        child.x(v2.x);
        child.y(v2.y);
    }

    public void freeChildren(){
        m1.setToRotation(rotation);
        getTrans(m2, m1);
        for(TilesCraftc child : children){
            child.setFather(father);
            v2.set(child.x(), child.y()).mul(m2);
            child.x(v2.x);
            child.y(v2.y);
        }
        children.clear();
    }

    @Override
    public void remove(){
        if(father != null) father.children().remove((TilesCraftc)(self()));
        freeChildren();
        if(tiles != null) tiles.removeCraft();
    }

    public void setFather(TilesCraftc value){
        if(father == value) return;
        if(father != null){
            father.children().remove((TilesCraftc)(self()));
        }
        father = value;
        if(value != null){
            fatherId = father.id();
            father.children().add((TilesCraftc)(self()));
        }else fatherId = 0;
    }

    // This will be called before every fps
    public void updateMat(){
        resetMat();
        for(TilesCraftc child : children){
            child.resetMat();
        }
    }

    public void resetMat(){
        m1.setToRotation(rotation);
        getTrans(trans, m1);
        getInv(inv, m1);
    }

    public Mat getTrans(Mat mat, Mat rotateMat){
        return mat.setToTranslation(originX + x + offsetX, originY + y + offsetY).mul(rotateMat).translate(-originX, -originY);
    }

    public Mat getInv(Mat mat, Mat rotateMat){
        return mat.setToTranslation(originX, originY).mul(rotateMat.transpose()).translate(-originX - x - offsetX, -originY - y - offsetY);
    }

    @Override
    public void afterReadAll(){
        setFather(Groups.craft.getByID(fatherId));
        tiles.craft = self();
    }

    @Override
    public void afterSync(){
        setFather(Groups.craft.getByID(fatherId));
        tiles.craft = self();
    }

    public Rect oriRect(Rect out){
        if(tiles == null) return out.set(-4f, -4f, 8f, 8f);
        return out.set(tiles.getMinX() * tiles.tilesize - tiles.tilesize / 2, tiles.getMinY() * tiles.tilesize - tiles.tilesize / 2, tiles.width * tiles.tilesize, tiles.height * tiles.tilesize);
    }

    @Override
    @MethodPriority(-1)
    public void draw(){
        if(!craftType().drawUnit) return;
    }

    @Override
    @Replace
    public void hitbox(Rect out){
        if(craftType().noHitbox) out.setCentered(x, y, 1f, 1f);
        else rect(oriRect(out), trans);
    }

    @Override
    @Replace
    public String toString(){
        return "TilesCraft#" + id() + "[" + tiles.toString() + "]";
    }
}
