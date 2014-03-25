
package com.github.iabarca.lwrt.profile;

import com.github.iabarca.lwrt.managers.Profiles;

public interface ProfilesListener {

    /**
     * Notifies all listeners that a new {@linkplain Profiles} manager was
     * selected and therefore they should reflect the changes accordingly.
     * 
     * @param profiles
     */
    public void onRefresh(Profiles profiles);

}
