package com.github.lawena.util;

import com.github.lawena.profile.Profile;

import java.util.function.Function;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;

public class BindUtils {

    private BindUtils() {
    }

    /**
     * Load the value of a given property from a {@link Profile}, convert it to the type of property
     * using a given function and bidirectionally bind it to another given property.
     *
     * @param profile The source {@link Profile} of the property value
     * @param model   A {@link Property} that will contain the value loaded
     * @param view    Another Property that will be bidirectionally bound to <code>model</code>
     * @param mapper  A {@link Function} that converts a given <code>String</code> to the type of
     *                the property
     * @param <T>     The type of the property value
     * @return The <code>model</code> property that now contains the loaded value
     */
    public static <T> Property<T> load(Profile profile, Property<T> model, Property<T> view,
                                       Function<String, T> mapper) {
        checkProperty(model);
        profile.get(model.getName()).ifPresent(s -> model.setValue(mapper.apply(s)));
        Bindings.bindBidirectional(view, model);
        return model;
    }

    /**
     * Load the value of a given property from a {@link Profile} and convert it to the type of
     * property using a given function.
     *
     * @param profile The source {@link Profile} of the property value
     * @param model   A {@link Property} that will contain the value loaded
     * @param mapper  A {@link Function} that converts a given <code>String</code> to the type of
     *                the property
     * @param <T>     The type of the property value
     * @return The <code>model</code> property that now contains the loaded value
     */
    public static <T> Property<T> load(Profile profile, Property<T> model, Function<String, T> mapper) {
        checkProperty(model);
        profile.get(model.getName()).ifPresent(s -> model.setValue(mapper.apply(s)));
        return model;
    }

    /**
     * Save the value of a given property to a given {@link Profile} after unbinding it from another
     * given property. The saved value will be converted to a <code>String</code> using {@link
     * Object#toString()}.
     *
     * @param profile The destination {@link Profile} of the property value
     * @param model   A {@link Property} that contains the value to save
     * @param view    Another Property that will be bidirectionally unbound from <code>model</code>
     * @param <T>     The type of the property value
     */
    public static <T> void save(Profile profile, Property<T> model, Property<T> view) {
        checkProperty(model);
        Bindings.unbindBidirectional(view, model);
        profile.set(model.getName(), model.getValue().toString());
    }

    /**
     * Save the value of a given property to a given {@link Profile} after converting it to a
     * <code>String</code> using a given <code>Function</code> and unbinding it from another given
     * property.
     *
     * @param profile The destination {@link Profile} of the property value
     * @param model   A {@link Property} that contains the value to save
     * @param view    Another Property that will be bidirectionally unbound from <code>model</code>
     * @param mapper  A {@link Function} that converts a given value from the type of the property
     *                to a <code>String</code>
     * @param <T>     The type of the property value
     */
    public static <T> void save(Profile profile, Property<T> model, Property<T> view,
                                Function<T, String> mapper) {
        checkProperty(model);
        Bindings.unbindBidirectional(view, model);
        profile.set(model.getName(), mapper.apply(model.getValue()));
    }

    private static void checkProperty(Property<?> property) {
        if (property.getName() == null || property.getName().isEmpty())
            throw new IllegalArgumentException(String.format(
                    "Property %s does not have a name!", property)); //$NON-NLS-1$
    }

}
