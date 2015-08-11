package com.github.lawena;

import com.github.lawena.exts.ViewProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.Node;

public class AppNodeGroups implements NodeGroups {

    private Map<ViewProvider, Map<String, List<Node>>> groups = new HashMap<>();

    @Override
    public Map<String, List<Node>> getGroups(ViewProvider key) {
        return groups.computeIfAbsent(key, k -> new HashMap<>());
    }

    @Override
    public List<Node> getNodes(ViewProvider key, String location) {
        return getGroups(key).computeIfAbsent(location, k -> new ArrayList<>());
    }

}
