package com.github.lawena.security;

import java.io.FilePermission;
import java.nio.file.Paths;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.PropertyPermission;

import ro.fortsoft.pf4j.PluginClassLoader;

/**
 * A standard security policy for plugins used in the application.
 */
public class PluginPolicy extends Policy {

    private PermissionCollection pluginPermissions = pluginPermissions();
    private PermissionCollection applicationPermissions = applicationPermissions();

    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        if (isPlugin(domain)) {
            return pluginPermissions;
        } else {
            return applicationPermissions;
        }
    }

    @Override
    public boolean implies(ProtectionDomain domain, Permission permission) {
        return !isPlugin(domain) || super.implies(domain, permission);
    }

    private boolean isPlugin(ProtectionDomain domain) {
        return domain.getClassLoader() instanceof PluginClassLoader;
    }

    private PermissionCollection pluginPermissions() {
        Permissions p = new Permissions();
        p.add(new FilePermission("<<ALL FILES>>", "read"));
        p.add(new FilePermission(Paths.get("").toAbsolutePath().getParent() + "/-", "read, write, execute, delete, readlink"));
        p.add(new PropertyPermission("*", "read"));
        p.add(new RuntimePermission("accessClassInPackage.*"));
        p.add(new RuntimePermission("getClassLoader"));
        p.add(new RuntimePermission("preferences"));
        return p;
    }

    private PermissionCollection applicationPermissions() {
        Permissions p = new Permissions();
        p.add(new AllPermission());
        return p;
    }
}
