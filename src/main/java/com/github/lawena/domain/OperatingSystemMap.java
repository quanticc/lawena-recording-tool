package com.github.lawena.domain;

import com.github.lawena.util.LwrtUtils;

public class OperatingSystemMap {

    public static OperatingSystemMap create(String windows, String osx, String linux) {
        OperatingSystemMap map = new OperatingSystemMap();
        map.setWindows(windows);
        map.setOsx(osx);
        map.setLinux(linux);
        return map;
    }

    public static OperatingSystemMap create(String all) {
        return create(all, all, all);
    }

    private String windows;
    private String osx;
    private String linux;

    public String get() {
        if (LwrtUtils.isWindows()) {
            return windows;
        } else if (LwrtUtils.isMacOS()) {
            return osx;
        } else if (LwrtUtils.isLinux()) {
            return linux;
        } else {
            throw new UnsupportedOperationException("Unsupported OS");
        }
    }

    public void set(String value) {
        if (LwrtUtils.isWindows()) {
            windows = value;
        } else if (LwrtUtils.isMacOS()) {
            osx = value;
        } else if (LwrtUtils.isLinux()) {
            linux = value;
        } else {
            throw new UnsupportedOperationException("Unsupported OS");
        }
    }

    public String getWindows() {
        return windows;
    }

    public void setWindows(String windows) {
        this.windows = windows;
    }

    public String getOsx() {
        return osx;
    }

    public void setOsx(String osx) {
        this.osx = osx;
    }

    public String getLinux() {
        return linux;
    }

    public void setLinux(String linux) {
        this.linux = linux;
    }

    @Override
    public String toString() {
        return "OperatingSystemMap [" +
                "windows='" + windows + '\'' +
                ", osx='" + osx + '\'' +
                ", linux='" + linux + '\'' +
                ']';
    }
}
