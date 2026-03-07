package mindustry.type.weapons;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;

//TODO
class TilesCraftWeapon extends Weapon{
    public TilesCraftWeapon(){
        super();
    }
    public TilesCraftWeapon(String name){
        super(name);
    }

    @Override
    public void draw(Unit unit, WeaponMount mount){
        // Do not draw
    }

    @Override
    public void drawOutline(Unit unit, WeaponMount mount){
    }
}
