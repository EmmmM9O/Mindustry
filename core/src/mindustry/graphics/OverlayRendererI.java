package mindustry.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.game.Teams.*;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.CoreBlock.*;

import static mindustry.Vars.*;

public abstract class OverlayRendererI{
    public abstract void drawBottom();

    public abstract void drawTop();

    public abstract void checkApplySelection(Unit u);
}
