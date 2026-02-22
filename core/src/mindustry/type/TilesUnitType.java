package mindustry.type;

import mindustry.gen.*;
import mindustry.game.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

/** Config class for special Erekir unit properties. */
public class TilesUnitType extends UnitType{
    public boolean noHitbox = false;
    public boolean drawUnit = false;
    public boolean drawFloor = true;

    public TilesUnitType(String name){
        super(name);
        hidden = true;
    }

    public TilesCraftc spawnCraft(Team team, float x, float y, float rotation){
        return (TilesCraftc)spawn(team, x, y, rotation);
    }

    public TilesCraftc spawnCraft(float x, float y){
        return spawnCraft(state.rules.defaultTeam, x, y, 0f);
    }
}
