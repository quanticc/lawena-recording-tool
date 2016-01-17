package com.github.lawena.task;

import com.github.lawena.util.StringUtils;
import com.threerings.getdown.data.Resource;
import com.threerings.getdown.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Task to scan local resource meta-data, retrieving update candidates.
 */
public class CollectorTask extends LawenaTask<List<Resource>> {

    private static final Logger log = LoggerFactory.getLogger(CollectorTask.class);
    private static final File APP_DIR = new File("").getAbsoluteFile();

    private final Map<String, Object> properties;
    private final URL appbase;

    public CollectorTask(Map<String, Object> properties, URL appbase) {
        this.properties = properties;
        this.appbase = appbase;
    }

    @Override
    protected List<Resource> call() throws Exception {
        List<Resource> list = new ArrayList<>();
        parseResources("code", false, list);
        parseResources("ucode", true, list);
        parseResources("resource", false, list);
        parseResources("uresource", true, list);
        for (String auxgroup : parseList("auxgroups")) {
            if (isAuxGroupActive(auxgroup)) {
                parseResources(auxgroup + ".code", false, list);
                parseResources(auxgroup + ".ucode", true, list);
                parseResources(auxgroup + ".resource", false, list);
                parseResources(auxgroup + ".uresource", true, list);
            }
        }
        return list;
    }

    private void parseResources(String name, boolean unpack, List<Resource> list) {
        String[] resources = ConfigUtil.getMultiValue(properties, name);
        if (resources == null) {
            return;
        }
        for (String resource : resources) {
            try {
                list.add(createResource(resource, unpack));
            } catch (Exception e) {
                log.warn("Invalid resource '{}'. {}", resource, e.toString());
            }
        }
    }

    private Resource createResource(String path, boolean unpack)
            throws MalformedURLException {
        URL remoteUrl = new URL(appbase, path.replace(" ", "%20"));
        return new Resource(path, remoteUrl, new File(APP_DIR, path), unpack);
    }

    private String[] parseList(String name) {
        String value = (String) properties.get(name);
        return (value == null) ? new String[0] : StringUtils.parseStringArray(value);
    }

    public boolean isAuxGroupActive(String auxgroup) {
        return new File(APP_DIR, auxgroup + ".dat").exists();
    }
}
