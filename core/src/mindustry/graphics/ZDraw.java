package mindustry.graphics;

import static mindustry.Vars.*;

public class ZDraw{
    public static float height = 0f;

    public static void realZ(float z){
        zdraw.z(z);
    }

    public static void sclColor(float r,float g,float b,float a){
        zdraw.scl(r,g,b,a);
    }

    public static void colorLayer(int layer){
        zdraw.colorL(layer);
    }

    public static void reset(){
        zdraw.scl(1f,1f,1f,1f);
    }


    public void z(float z){

    }

    public void scl(float r,float g,float b,float a){
    }

    public void colorL(int layer){
        switch(layer){
            case ColorLayer.cell -> scl(1.4f, 1.4f, 1.4f, 1f);
            case ColorLayer.engine -> scl(1.5f, 1.1f, 1.1f, 1f);
            case ColorLayer.suppressionField -> scl(1.2f, 1.2f, 1.2f, 1f);
        }
    }
}
