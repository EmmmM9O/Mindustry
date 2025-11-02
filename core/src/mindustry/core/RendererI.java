package mindustry.core;

import arc.*;
import arc.math.*;
import arc.math.geom.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.world.blocks.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public abstract class RendererI implements ApplicationListener{
    public BlockRendererI blocks;
    public final FogRenderer fog = new FogRenderer();
    public final MinimapRenderer minimap = new MinimapRenderer();
    public final OverlayRenderer overlays = new OverlayRenderer();
    public final LightRenderer lights = new LightRenderer();
    public final Pixelator pixelator = new Pixelator();

    public ObjectMap<String, Runnable> customBackgrounds = new ObjectMap<>();
    public TextureRegion[] bubbles = new TextureRegion[16], splashes = new TextureRegion[12];
    public TextureRegion[][] fluidFrames;

    public PlanetRenderer planets;
    public @Nullable Bloom bloom;
    public @Nullable FrameBuffer backgroundBuffer;
    public FrameBuffer effectBuffer = new FrameBuffer();
    public boolean animateShields, animateWater, drawWeather = true, drawStatus, enableEffects, drawDisplays = true, drawLight = true, pixelate = false;
    public float weatherAlpha;
    /** minZoom = zooming out, maxZoom = zooming in, used by cutscenes */
    public float minZoom = 1.5f, maxZoom = 6f;
    /** minZoom = zooming out, maxZoom = zooming in, used by actual gameplay zoom and regulated by settings **/
    public float minZoomInGame = 0.5f, maxZoomInGame = 6f;
    public float
    //target camera scale that is lerp-ed to
    targetscale = Scl.scl(4),
    //current actual camera scale
    camerascale = targetscale,
    //starts at coreLandDuration, ends at 0. if positive, core is landing.
    landTime,
    //intensity for screen shake
    shakeIntensity,
    //reduction rate of screen shake
    shakeReduction,
    //current duration of screen shake
    shakeTime;
    //currently landing core, null if there are no cores or it has finished landing.
    public @Nullable LaunchAnimator launchAnimator;
    public boolean launching;
    
    public void shake(float intensity, float duration){
        shakeIntensity = Math.max(shakeIntensity, Mathf.clamp(intensity, 0, 100));
        shakeTime = Math.max(shakeTime, duration);
        shakeReduction = shakeIntensity / shakeTime;
    }

    public abstract void addEnvRenderer(int mask, Runnable render);

    public abstract void addCustomBackground(String name, Runnable render);

    public abstract void loadFluidFrames();
    public abstract TextureRegion[][] getFluidFrames();
    public abstract void updateAllDarkness();

    /** @return whether a launch/land cutscene is playing. */
    public boolean isCutscene(){
        return landTime > 0;
    }

    public float landScale(){
        return landTime > 0 ? camerascale : 1f;
    }

    public abstract void toggleBloom(boolean enabled);

    public abstract void draw();

    public void scaleCamera(float amount){
        targetscale *= (amount / 4) + 1;
        clampScale();
    }

    public void clampScale(){
        targetscale = Mathf.clamp(targetscale, minScale(), maxScale());
    }

    public float getDisplayScale(){
        return camerascale;
    }

    public float minScale(){
        if(control.input.logicCutscene) return Scl.scl(minZoom);
        return Scl.scl(minZoomInGame);
    }

    public float maxScale(){
        if(control.input.logicCutscene) return Mathf.round(Scl.scl(maxZoom));
        return Mathf.round(Scl.scl(maxZoomInGame));
    }

    public float getScale(){
        return targetscale;
    }

    public void setScale(float scl){
        targetscale = scl;
        clampScale();
    }

    public boolean isLaunching(){
        return launching;
    }

    public float getLandTime(){
        return landTime;
    }

    public float getLandTimeIn(){
        if(launchAnimator == null) return 0f;
        float fin = landTime / launchAnimator.launchDuration();
        if(!launching) fin = 1f - fin;
        return fin;
    }

    public abstract void showLanding(LaunchAnimator landCore);
    public abstract void showLaunch(LaunchAnimator landCore);
    public abstract void takeMapScreenshot();

}
