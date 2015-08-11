package com.github.lawena.exts;

import java.util.Collection;

import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * An extension to provide tags and classify resources.
 *
 * @author Ivan
 */
public interface TagProvider extends ExtensionPoint {

    String getName();

    Collection<String> tag(Collection<String> contents);

}
