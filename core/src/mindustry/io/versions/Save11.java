package mindustry.io.versions;

import arc.*;
import arc.func.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.content.TechTree.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.game.Teams.*;
import mindustry.gen.*;
import mindustry.maps.Map;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import java.util.*;
import java.io.*;
import mindustry.io.*;

import static mindustry.Vars.*;
/** Adds patches in content header. */
public class Save11 extends SaveVersion{

    public Save11(){
        super(11);
    }

    @Override
    public void writeTeamBlocks(DataOutput stream) throws IOException{
        //write team data with entities.
        Seq<TeamData> data = state.teams.getActive().copy();
        if(!data.contains(Team.sharded.data())) data.add(Team.sharded.data());

        Writes writes = new Writes(stream);

        stream.writeInt(data.size);
        for(TeamData team : data){
            stream.writeInt(team.team.id);
            stream.writeInt(team.plans.size);
            for(BlockPlan block : team.plans){
                stream.writeShort(block.x);
                stream.writeShort(block.y);
                stream.writeShort(block.rotation);
                stream.writeShort(block.block.id);
                TypeIO.writeObject(writes, block.config);
            }
        }
    }

    @Override
    public void readTeamBlocks(DataInput stream) throws IOException{
        int teamc = stream.readInt();

        var reads = new Reads(stream);

        for(int i = 0; i < teamc; i++){
            Team team = Team.get(stream.readInt());
            TeamData data = team.data();
            int blocks = stream.readInt();
            data.plans.clear();
            data.plans.ensureCapacity(Math.min(blocks, 1000));
            var set = new IntSet();

            for(int j = 0; j < blocks; j++){
                short x = stream.readShort(), y = stream.readShort(), rot = stream.readShort(), bid = stream.readShort();
                var obj = TypeIO.readObject(reads);
                //cannot have two in the same position
                if(set.add(Point2.pack(x, y))){
                    data.plans.addLast(new BlockPlan(x, y, rot, content.block(bid), obj));
                }
            }
        }
    }
}
