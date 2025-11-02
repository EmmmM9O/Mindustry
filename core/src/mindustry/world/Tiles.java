package mindustry.world;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.math.geom.QuadTree.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;

import java.util.*;

/** A tile container. */
public abstract class Tiles implements Iterable<Tile>, QuadTreeObject{
    public int width;
    public int height;

    public Tiles(int width, int height){
        this.width = width;
        this.height = height;
    }

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

    public int id = -1;

    protected float rotation;

    public float getOriginX(){
        return originX;
    }

    public void setOriginX(float originX){
        this.originX = originX;
        resetMat();
    }

    protected float originX;

    public float getOriginY(){
        return originY;
    }

    public void setOriginY(float originY){
        this.originY = originY;
        resetMat();
    }

    protected float originY;

    public float getOffsetX(){
        return offsetX;
    }

    public void setOffsetX(float offsetX){
        this.offsetX = offsetX;
        resetMat();
    }

    protected float offsetX;

    public float getOffsetY(){
        return offsetY;
    }

    public void setOffsetY(float offsetY){
        this.offsetY = offsetY;
        resetMat();
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
    protected float offsetY;
    public Mat rotateMat = new Mat(),
    trans = new Mat(), inv = new Mat();
    public static Mat tmpM = new Mat();

    public Tiles root, father;

    public void setFather(Tiles father){
        this.father = father;
        if(father == null)
            root = this;
        else root = father.root;
        for(Tiles child : children){
            child.root = root;
        }
    }

    public Seq<Tiles> children = Seq.with();
    public float tilesize = 8f;

    public float getRotation(){
        return rotation;
    }

    public void resetMat(){
        trans(trans);
        inv(inv);
        if(father != null && root != father){
            // We do not want to compute root's mat
            trans.mul(father.trans);
            inv.mul(father.inv);
        }
        update();
    }

    void updateRotation(){
        rotateMat.setToRotation(rotation);
        resetMat();
    }

    public void update(){
        for(Tiles child : children){
            child.resetMat();
        }
    }

    public Mat trans(Mat mat){
        return mat.setToTranslation(originX + offsetX, originY + offsetY).mul(rotateMat).translate(-originX, -originY);
    }

    public Mat inv(Mat mat){
        return mat.setToTranslation(originX, originY).mul(tmpM.set(rotateMat).transpose()).translate(-originX - offsetX, -originY - offsetY);
    }

    public void setRotation(float rotation){
        this.rotation = rotation;
        updateRotation();
    }

    public static Vec2[] tmp = {new Vec2(), new Vec2(), new Vec2(), new Vec2()};

    public static Rect rect(Rect rect, Mat mat){
        tmp[0].set(rect.x, rect.y);
        tmp[1].set(rect.x + rect.width, rect.y);
        tmp[2].set(rect.x + rect.width, rect.y + rect.height);
        tmp[3].set(rect.x, rect.y + rect.height);
        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        for(int i = 0; i < 4; i++){
            tmp[i].mul(mat);
            minX = Math.min(minX, tmp[i].x);
            maxX = Math.max(maxX, tmp[i].x);
            minY = Math.min(minY, tmp[i].y);
            maxY = Math.max(maxY, tmp[i].y);
        }
        rect.x = minX;
        rect.y = minY;
        rect.width = maxX - minX;
        rect.height = maxY - minY;
        return rect;
    }

    public Rect oriRect(Rect out){
        return out.set(getMinX() * tilesize - tilesize / 2, getMinY() * tilesize - tilesize / 2, width * tilesize, height * tilesize);
    }

    @Override
    public void hitbox(Rect out){
        if(root == this){
            oriRect(out);
        }else{
            rect(oriRect(out), trans);
        }
    }
}
