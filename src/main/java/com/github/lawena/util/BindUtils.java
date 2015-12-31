package com.github.lawena.util;

import com.github.lawena.domain.Profile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BindUtils {

    private BindUtils() {
    }

    /**
     * Load the value of a given property from a {@link Profile}, convert it to the type of property using a given
     * function and bidirectionally bind it to another given property.
     *
     * @param profile The source {@link Profile} of the property value
     * @param model   A {@link Property} that will contain the value loaded
     * @param view    Another Property that will be bidirectionally bound to <code>model</code>
     * @param mapper  A {@link Function} that converts a given <code>String</code> to the type of the property
     * @param <T>     The type of the property value
     * @return The <code>model</code> property that now contains the loaded value
     */
    public static <T> Property<T> loadAsString(Profile profile, Property<T> model, Property<T> view,
                                       Function<String, T> mapper) {
        checkProperty(model);
        profile.get(model.getName()).ifPresent(s -> model.setValue(mapper.apply(s.toString())));
        Bindings.bindBidirectional(view, model);
        return model;
    }

    /**
     * Load the value of a given property from a {@link Profile} if it matches the properties type and bidirectionally
     * bind it to another given property. The model property must hold a value (not <code>null</code>) before calling
     * this method.
     *
     * @param profile The source {@link Profile} of the property value
     * @param model   A {@link Property} that will contain the value loaded
     * @param view    Another Property that will be bidirectionally bound to <code>model</code>
     * @param <T>     The type of the property value
     * @return the model property that contains the loaded value if the types matched, no changes otherwise.
     */
    public static <T> Property<T> load(Profile profile, Property<T> model, Property<T> view) {
        load(profile, model);
        Bindings.bindBidirectional(view, model);
        return model;
    }

    /**
     * Load the value of a given property from a {@link Profile} if it matches the properties type. The model property
     * must hold a value (not <code>null</code>) before calling this method.
     *
     * @param profile The source {@link Profile} of the property value
     * @param model   A {@link Property} that will contain the value loaded
     * @param <T>     The type of the property value
     * @return the model property that contains the loaded value if the types matched, no changes otherwise.
     */
    @SuppressWarnings("unchecked")
    public static <T> Property<T> load(Profile profile, Property<T> model) {
        checkProperty(model);
        profile.get(model.getName())
                .filter(value -> model.getValue().getClass().isInstance(value))
                .ifPresent(value -> model.setValue((T) value));
        return model;
    }

    /**
     * Save the value of a given property to a given {@link Profile} after unbinding it from another given property.
     *
     * @param profile The destination {@link Profile} of the property value
     * @param model   A {@link Property} that contains the value to save
     * @param view    Another Property that will be bidirectionally unbound from <code>model</code>
     * @param <T>     The type of the property value
     */
    public static <T> void save(Profile profile, Property<T> model, Property<T> view) {
        save(profile, model);
        Bindings.unbindBidirectional(view, model);
    }

    /**
     * Save the value of a given property to a given {@link Profile} after converting it to a <code>String</code> using
     * a given <code>Function</code> and unbinding it from another given property.
     *
     * @param profile The destination {@link Profile} of the property value
     * @param model   A {@link Property} that contains the value to save
     * @param view    Another Property that will be bidirectionally unbound from <code>model</code>
     * @param mapper  A {@link Function} that converts a given value from the type of the property to a
     *                <code>String</code>
     * @param <T>     The type of the property value
     */
    public static <T> void save(Profile profile, Property<T> model, Property<T> view,
                                Function<T, String> mapper) {
        save(profile, model, mapper);
        Bindings.unbindBidirectional(view, model);
    }

    /**
     * Save the value of a given property to a given {@link Profile}.
     *
     * @param profile The destination {@link Profile} of the property value
     * @param model   A {@link Property} that contains the value to save
     * @param <T>     The type of the property value
     */
    public static <T> void save(Profile profile, Property<T> model) {
        checkProperty(model);
        profile.set(model.getName(), model.getValue());
    }

    /**
     * Save the value of a given property to a given {@link Profile} after converting it to a <code>String</code> using
     * a given <code>Function</code>.
     *
     * @param profile The destination {@link Profile} of the property value
     * @param model   A {@link Property} that contains the value to save
     * @param mapper  A {@link Function} that converts a given value from the type of the property to a
     *                <code>String</code>
     * @param <T>     The type of the property value
     */
    public static <T> void save(Profile profile, Property<T> model, Function<T, String> mapper) {
        checkProperty(model);
        profile.set(model.getName(), mapper.apply(model.getValue()));
    }

    public static List<String> split(String str) {
        return Arrays.asList(str.split(";")).stream().map(String::trim).collect(Collectors.toList());
    }

    public static String join(List<String> list) {
        return list.stream().collect(Collectors.joining(";"));
    }

    public static Optional<ExternalString> exists(List<ExternalString> list, String key) {
        Objects.requireNonNull(key);
        return list.stream().filter(s -> key.equals(s.getKey())).findFirst();
    }

    private static void checkProperty(Property<?> property) {
        if (property.getName() == null || property.getName().isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "Property %s does not have a name!", property));
        }
        if (property.getValue() == null) {
            throw new IllegalArgumentException(String.format(
                    "Property %s does not have a default value!", property));
        }
    }

}
