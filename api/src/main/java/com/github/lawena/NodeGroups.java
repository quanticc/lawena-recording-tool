package com.github.lawena;

import com.github.lawena.exts.ViewProvider;

import java.util.List;
import java.util.Map;

import javafx.scene.Node;

/**
 * A group of nodes provided by extensions which are managed by the application controller.
 *
 * @author Ivan
 */
public interface NodeGroups {

    Map<String, List<Node>> getGroups(ViewProvider key);

    List<Node> getNodes(ViewProvider key, String location);

}
