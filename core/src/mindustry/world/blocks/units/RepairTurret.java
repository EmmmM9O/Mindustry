package mindustry.world.blocks.units;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class RepairTurret extends Block{
    public int timerTarget = timers++;
    public int timerEffect = timers++;

    public float repairRadius = 50f;
    public float repairSpeed = 0.3f;
    public float powerUse;
    public float length = 5f;
    public float beamWidth = 1f;
    public float pulseRadius = 6f;
    public float pulseStroke = 2f;
    public boolean acceptCoolant = false;

    public float coolantUse = 0.5f;
    /** Effect displayed when coolant is used. */
    public Effect coolEffect = Fx.fuelburn;
    /** How much healing is increased by with heat capacity. */
    public float coolantMultiplier = 1f;

    public @Load(value = "@-base", fallback = "block-@size") TextureRegion baseRegion;
    public @Load("laser-white") TextureRegion laser;
    public @Load("laser-white-end") TextureRegion laserEnd;
    public @Load("laser-top") TextureRegion laserTop;
    public @Load("laser-top-end") TextureRegion laserTopEnd;

    public Color laserColor = Color.valueOf("98ffa9"), laserTopColor = Color.white.cpy();

    public RepairTurret(String name){
        super(name);
        update = true;
        solid = true;
        flags = EnumSet.of(BlockFlag.repair);
        hasPower = true;
        outlineIcon = true;
        //yeah, this isn't the same thing, but it's close enough
        group = BlockGroup.projectors;

        envEnabled |= Env.space;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.range, repairRadius / tilesize, StatUnit.blocks);
        stats.add(Stat.repairSpeed, repairSpeed * 60f, StatUnit.perSecond);

        if(acceptCoolant){
            stats.remove(Stat.booster);
            stats.add(Stat.booster, StatValues.speedBoosters(Core.bundle.get("bar.strength"), coolantUse, coolantMultiplier, true, this::consumesLiquid));
        }
    }

    @Override
    public void init(){
        if(acceptCoolant){
            hasLiquids = true;
            consume(new ConsumeCoolant(coolantUse)).optional(true, true);
        }

        consumePowerCond(powerUse, (RepairPointBuild entity) -> entity.target != null);
        updateClipRadius(repairRadius + tilesize);
        super.init();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, repairRadius, Pal.accent);
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{baseRegion, region};
    }

    public class RepairPointBuild extends Building implements Ranged, RotBlock{
        public Unit target;
        public Vec2 offset = new Vec2(), lastEnd = new Vec2();
        public float strength, rotation = 90;

        @Override
        public float buildRotation(){
            return rotation;
        }

        @Override
        public float drawrot(){
            return rotation - 90 - getTilesRotation();
        }

        @Override
        public void draw(){
            Draw.rect(baseRegion, x, y);

            Draw.z(Layer.turret);
            Drawf.shadow(region, x - (size / 2f), y - (size / 2f), drawrot());
            Draw.rect(region, x, y, drawrot());
        }

        @Override
        public void drawAbsolute(){
            renderer.drawBeam(absoluteX, absoluteY, effectHeight(), rotation, length, id, target, team, strength,
                pulseStroke, pulseRadius, beamWidth, lastEnd, offset, laserColor, laserTopColor,
                laser, laserEnd, laserTop, laserTopEnd);
        }

        @Override
        public void drawSelect(){
            Drawf.dashCircle(x, y, repairRadius, Pal.accent);
        }

        @Override
        public void updateTile(){
            float multiplier = 1f;
            if(acceptCoolant){
                multiplier = 1f + liquids.current().heatCapacity * coolantMultiplier * optionalEfficiency;
            }

            if(target != null && (target.dead() || target.dst(this) - target.hitSize/2f > repairRadius || target.health() >= target.maxHealth())){
                target = null;
            }

            if(target == null){
                offset.setZero();
            }

            boolean healed = false;

            if(target != null && efficiency > 0){
                float angle = Angles.angle(absoluteX, absoluteY, target.x + offset.x, target.y + offset.y);
                if(Angles.angleDist(angle, rotation) < 30f){
                    healed = true;
                    target.heal(repairSpeed * strength * edelta() * multiplier);
                }
                rotation = Mathf.slerpDelta(rotation, angle, 0.5f * efficiency * timeScale);
            }

            strength = Mathf.lerpDelta(strength, healed ? 1f : 0f, 0.08f * Time.delta);

            if(timer(timerTarget, 20)){
                //rect.setSize(repairRadius * 2).setCenter(absoluteX, absoluteY);
                target = Units.closest(team, absoluteX, absoluteY, repairRadius, Unit::damaged);
            }
        }

        @Override
        public boolean shouldConsume(){
            return target != null && enabled;
        }

        @Override
        public BlockStatus status(){
            return Mathf.equal(potentialEfficiency, 0f, 0.01f) ? BlockStatus.noInput : super.status();
        }

        @Override
        public float range(){
            return repairRadius;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(rotation);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                rotation = read.f();
            }
        }

        @Override
        public byte version(){
            return 1;
        }
    }
}
