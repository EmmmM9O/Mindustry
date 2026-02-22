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
import mindustry.gen.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.world.*;
import mindustry.game.*;
import mindustry.type.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.ConstructBlock.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public abstract class RendererI implements ApplicationListener{
    public final Rand rand = new Rand();
    public BlockRendererI blocks;
    public final FogRenderer fog = new FogRenderer();
    public final MinimapRenderer minimap = new MinimapRenderer();
    public OverlayRendererI overlays;
    public final LightRenderer lights = new LightRenderer();
    public final Pixelator pixelator = new Pixelator();

    public TextureRegion[] bubbles = new TextureRegion[16], splashes = new TextureRegion[12];
    public TextureRegion[][] fluidFrames;

    public PlanetRenderer planets;
    public @Nullable Bloom bloom;
    public @Nullable FrameBuffer backgroundBuffer;
    public FrameBuffer effectBuffer = new FrameBuffer();
    public boolean animateShields, animateWater, drawWeather = true, drawStatus, enableEffects, drawDisplays = true, drawLight = true, pixelate = false, drawUnitShaodw = true;
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

    public void tilesHitbox(Tiles tiles, Rect tmp){
    }

    public void perspectiveWorldPos(Tiles tiles, Vec2 tmp){
    }

    public void drawBuildPlans(Unitc builder){
        UnitType type = builder.type();
        for(int i = 0; i < 2; i++){
            for(BuildPlan plan : builder.plans()){
                if(plan.progress > 0.01f || (builder.buildPlan() == plan && plan.initialized && (builder.within(plan.x * tilesize, plan.y * tilesize, type.buildRange) || state.isEditor()))) continue;
                if(i == 0){
                    drawPlan(builder, plan, 1f);
                }else{
                    drawPlanTop(builder, plan, 1f);
                }
            }
        }

        Draw.reset();
    }

    public void drawPlan(Unitc builder, BuildPlan plan, float alpha){
        plan.animScale = 1f;
        if(plan.breaking){
            control.input.drawBreaking(plan);
        }else{
            plan.block.drawPlan(plan, control.input.allPlans(),
            Build.validPlace(plan.block, builder.team(), plan.tiles, plan.x, plan.y, plan.rotation) || control.input.planMatches(plan),
            alpha);
        }
    }

    public void drawPlanTop(Unitc builder, BuildPlan plan, float alpha){
        if(!plan.breaking){
            Draw.reset();
            Draw.mixcol(Color.white, 0.24f + Mathf.absin(Time.globalTime, 6f, 0.28f));
            Draw.alpha(alpha);
            plan.block.drawPlanConfigTop(plan, builder.plans());
        }
    }

    public void drawBuilding(Unitc builder){
        //TODO make this more generic so it works with builder "weapons"
        boolean active = builder.activelyBuilding();
        if(!active && builder.lastActive() == null) return;
        UnitType type = builder.type();

        Draw.z(Layer.flyingUnit);

        BuildPlan plan = active ? builder.buildPlan() : builder.lastActive();
        Tile tile = plan.tile();
        var core = builder.team().core();

        if(tile == null || !builder.within(plan, state.rules.infiniteResources ? Float.MAX_VALUE : type.buildRange)){
            return;
        }

        //draw remote plans.
        if(core != null && active && !builder.isLocal() && !(tile.block() instanceof ConstructBlock)){
            Draw.z(Layer.plans - 1f);
            drawPlan(builder, plan, 0.5f);
            drawPlanTop(builder, plan, 0.5f);
            Draw.z(Layer.flyingUnit);
        }

        if(type.drawBuildBeam){
            float focusLen = type.buildBeamOffset + Mathf.absin(Time.time, 3f, 0.6f);
            float px = builder.x() + Angles.trnsx(builder.rotation(), focusLen);
            float py = builder.y() + Angles.trnsy(builder.rotation(), focusLen);

            drawBuildingBeam(builder, px, py);
        }
    }

    public void drawBuildingBeam(Unitc builder, float px, float py){
        boolean active = builder.activelyBuilding();
        if(!active && builder.lastActive() == null) return;
        UnitType type = builder.type();

        Draw.z(Layer.flyingUnit);

        BuildPlan plan = active ? builder.buildPlan() : builder.lastActive();
        Tile tile = world.tile(plan.x, plan.y);

        if(tile == null || !builder.within(plan, state.rules.infiniteResources ? Float.MAX_VALUE : type.buildRange)){
            return;
        }

        int size = plan.breaking ? active ? tile.block().size : builder.lastSize() : plan.block.size;
        float tx = plan.drawx(), ty = plan.drawy();

        Lines.stroke(1f, plan.breaking ? Pal.remove : Pal.accent);
        Draw.z(Layer.buildBeam);

        Draw.alpha(builder.buildAlpha());

        if(!active && !(tile.build instanceof ConstructBuild)){
            Fill.square(plan.drawx(), plan.drawy(), size * tilesize / 2f);
        }

        Drawf.buildBeam(px, py, tx, ty, tilesize * size / 2f);

        Fill.square(px, py, 1.8f + Mathf.absin(Time.time, 2.2f, 1.1f), builder.rotation() + 45);

        Draw.reset();
        Draw.z(Layer.flyingUnit);
    }

    public void drawBeam(float x, float y, float height, float rotation, float length, int id, @Nullable Sized target, Team team,
                         float strength, float pulseStroke, float pulseRadius, float beamWidth,
                         Vec2 lastEnd, Vec2 offset,
                         Color laserColor, Color laserTopColor,
                         TextureRegion laser, TextureRegion laserEnd, TextureRegion laserTop, TextureRegion laserTopEnd){
        rand.setSeed(id + (target instanceof Entityc e ? e.id() : 0));

        if(target != null){
            float
            originX = x + Angles.trnsx(rotation, length),
            originY = y + Angles.trnsy(rotation, length);

            lastEnd.set(target).sub(originX, originY);
            lastEnd.setLength(Math.max(2f, lastEnd.len()));

            lastEnd.add(offset.trns(
            rand.random(360f) + Time.time / 2f,
            Mathf.sin(Time.time + rand.random(200f), 55f, rand.random(target.hitSize() * 0.2f, target.hitSize() * 0.45f))
            ).rotate(target instanceof Rotc rot ? rot.rotation() : 0f));

            lastEnd.add(originX, originY);
        }

        if(strength > 0.01f){
            float
            originX = x + Angles.trnsx(rotation, length),
            originY = y + Angles.trnsy(rotation, length);

            Draw.z(Layer.flyingUnit + 1); //above all units

            Draw.color(laserColor);

            float f = (Time.time / 85f + rand.random(1f)) % 1f;

            Draw.alpha(1f - Interp.pow5In.apply(f));
            Lines.stroke(strength * pulseStroke);

            Lines.circle(lastEnd.x, lastEnd.y, 1f + f * pulseRadius);

            Draw.color(laserColor);
            Drawf.laser(laser, laserEnd, originX, originY, lastEnd.x, lastEnd.y, strength * beamWidth);
            Draw.z(Layer.flyingUnit + 1.1f);
            Draw.color(laserTopColor);
            Drawf.laser(laserTop, laserTopEnd, originX, originY, lastEnd.x, lastEnd.y, strength * beamWidth);
            Draw.color();
        }
    }
}
