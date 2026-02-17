package mindustry.mod;

import mindustry.*;

public abstract class Preloader {
    /* even before the batch */
    public void beforeAll(){
    }

    /* Similar to before all but only called on client*/
    public void setupGraphics(){
    }

    public void modifyApplication(ClientLauncher launcher){
    }

    public void preload(){
    }
}
