package com.github.lawena.util;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Markers {

    public static final Marker OK = MarkerFactory.getMarker("OK");
    public static final Marker WARN = MarkerFactory.getMarker("WARN");
    public static final Marker ERROR = MarkerFactory.getMarker("ERROR");
    public static final Marker INFO = MarkerFactory.getMarker("INFO");
    public static final Marker NOGUI = MarkerFactory.getMarker("no-ui-log");

    private Markers() {
    }

}
