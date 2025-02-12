package mindustry.ui.fragments;

import mindustry.ctype.*;
import arc.scene.style.*;

public abstract class HudFragmentI implements Fragment {
  public boolean shown = true;
  public PlacementFragment blockfrag;

  public abstract void showUnlock(UnlockableContent content);

  public abstract void showToast(String text);

  public abstract void showToast(Drawable icon, String text);

  public abstract void showToast(Drawable icon, float size, String text);

  public abstract boolean hasToast();

  public abstract void setHudText(String text);

  public abstract void toggleHudText(boolean shown);
}
