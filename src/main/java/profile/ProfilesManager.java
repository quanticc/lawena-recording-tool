package profile;

import java.io.Serializable;
import java.util.Map;

public class ProfilesManager implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Map<String, Profile> profiles;
    private String selected;
    
    public ProfilesManager() {
        
    }

}
