package mindustry.entities.comp;

import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.annotations.Annotations.*;

@Component
abstract class HeightComp implements Entityc, Heightc{
    @SyncField(true)
    @SyncLocal
    float height;

    @Override
    @MethodPriority(-1f)
    public void update(){
        ZDraw.height = effectHeight();
    }

    public float effectHeight(){
        return height() + 0f;
    }

    public float getHeight(){
        return height;
    }
}
