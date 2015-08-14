package com.github.lawena.exts;

import com.github.lawena.Controller;

import ro.fortsoft.pf4j.ExtensionPoint;

public interface MenuProvider extends ExtensionPoint {

    void install(Controller parent);

    void remove(Controller parent);

}
