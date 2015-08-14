package com.github.lawena.security;

import java.io.FilePermission;
import java.net.SocketPermission;
import java.net.URL;
import java.nio.file.Paths;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.Optional;
import java.util.PropertyPermission;

/**
 * A standard security policy for plugins used in the application.
 */
public class PluginPolicy extends Policy {

    private PermissionCollection pluginPermissions = pluginPermissions();
    private PermissionCollection applicationPermissions = applicationPermissions();

    @Override
    public final PermissionCollection getPermissions(ProtectionDomain domain) {
        if (isPlugin(domain)) {
            return pluginPermissions;
        } else {
            return applicationPermissions;
        }
    }

    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        if (isPlugin(codesource)) {
            return pluginPermissions;
        } else {
            return applicationPermissions;
        }
    }

    @Override
    public final boolean implies(ProtectionDomain domain, Permission permission) {
        return !isPlugin(domain) || super.implies(domain, permission);
    }

    private boolean isPlugin(ProtectionDomain domain) {
        return Optional.ofNullable(domain)
                .map(ProtectionDomain::getCodeSource)
                .map(CodeSource::getLocation)
                .map(URL::toString)
                .map(url -> url.contains("/plugins/"))
                .orElse(false);
    }

    private boolean isPlugin(CodeSource codesource) {
        return Optional.ofNullable(codesource)
                .map(CodeSource::getLocation)
                .map(URL::toString)
                .map(url -> url.contains("/plugins/"))
                .orElse(false);
    }

    private PermissionCollection pluginPermissions() {
        Permissions p = new Permissions();
        p.add(new FilePermission("<<ALL FILES>>", "read"));
        p.add(new FilePermission(Paths.get("").toAbsolutePath().getParent() + "/-", "read, write, execute, delete, readlink"));
        p.add(new RuntimePermission("getClassLoader"));
        p.add(new PropertyPermission("user.dir", "read"));
        p.add(new RuntimePermission("preferences"));

        p.add(new SocketPermission("localhost:0", "listen"));
        p.add(new PropertyPermission("java.version", "read"));
        p.add(new PropertyPermission("java.vendor", "read"));
        p.add(new PropertyPermission("java.vendor.url", "read"));
        p.add(new PropertyPermission("java.class.version", "read"));
        p.add(new PropertyPermission("os.name", "read"));
        p.add(new PropertyPermission("os.version", "read"));
        p.add(new PropertyPermission("os.arch", "read"));
        p.add(new PropertyPermission("file.separator", "read"));
        p.add(new PropertyPermission("path.separator", "read"));
        p.add(new PropertyPermission("line.separator", "read"));
        p.add(new PropertyPermission("java.specification.version", "read"));
        p.add(new PropertyPermission("java.specification.vendor", "read"));
        p.add(new PropertyPermission("java.specification.name", "read"));
        p.add(new PropertyPermission("java.vm.specification.version", "read"));
        p.add(new PropertyPermission("java.vm.specification.vendor", "read"));
        p.add(new PropertyPermission("java.vm.specification.name", "read"));
        p.add(new PropertyPermission("java.vm.version", "read"));
        p.add(new PropertyPermission("java.vm.vendor", "read"));
        p.add(new PropertyPermission("java.vm.name", "read"));
        return p;
    }

    private PermissionCollection applicationPermissions() {
        Permissions p = new Permissions();
        p.add(new AllPermission());
        return p;
    }
}
