
package com.github.iabarca.lwrt.lwrt;

import com.github.iabarca.lwrt.managers.Profiles;
import com.github.iabarca.lwrt.profile.ProfilesListener;

public interface LwrtPresenter extends ProfilesListener {

    public void startUI();

    public void hide();

    public Profiles getProfiles();

    public void restoreGameFiles();

    public void showAboutDialog();

    public void setModel(Lwrt model);

    public void saveAndExit();

}
