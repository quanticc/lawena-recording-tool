package com.github.lawena.util;

import com.github.lawena.domain.AppProfile;
import com.github.lawena.domain.Profile;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class BindUtilsTest {

    @Test
    public void testCorrectLoad() {
        Integer oldValue = 10;
        Integer newValue = 100;
        String key = "integer";
        Property<Integer> integerProperty = new SimpleObjectProperty<>(null, key, oldValue);
        Profile profile = new AppProfile();
        profile.set(key, newValue);
        // attempting to load profile's value, should work
        BindUtils.load(profile, integerProperty);
        Assert.assertEquals(newValue, integerProperty.getValue());
    }

    @Test
    public void testIncorrectLoad() {
        Integer oldValue = 10;
        String newValue = "100";
        String key = "integer";
        Property<Integer> integerProperty = new SimpleObjectProperty<>(null, key, oldValue);
        Profile profile = new AppProfile();
        profile.set(key, newValue);
        // attempting to load profile's value, but it's a string, so don't load it
        BindUtils.load(profile, integerProperty);
        // value must have not changed
        Assert.assertEquals(oldValue, integerProperty.getValue());
    }

    @Test
    public void testCorrectListLoad() {
        List<String> oldValue = Arrays.asList("A", "B");
        List<String> newValue = Arrays.asList("C", "D");
        String key = "list";
        // this kind of property works
        Property<List<String>> listProperty = new SimpleObjectProperty<>(null, key, oldValue);
        Profile profile = new AppProfile();
        profile.set(key, newValue);
        BindUtils.load(profile, listProperty);
        Assert.assertEquals(newValue, listProperty.getValue());
    }

    @Test
    public void testIncorrectListLoad() {
        List<String> oldValue = Arrays.asList("A", "B");
        List<String> newValue = Arrays.asList("C", "D");
        String key = "list";
        // this kind of property does not work with load method
        ListProperty<String> listProperty = new SimpleListProperty<>(null, key, FXCollections.observableArrayList(oldValue));
        Profile profile = new AppProfile();
        profile.set(key, newValue);
        BindUtils.load(profile, listProperty);
        Assert.assertEquals(oldValue, listProperty.getValue());
    }

    @Test
    public void testSave() {
        Integer oldValue = 10;
        Integer newValue = 100;
        String key = "integer";
        Property<Integer> integerProperty = new SimpleObjectProperty<>(null, key, newValue);
        Profile profile = new AppProfile();
        profile.set(key, oldValue);
        BindUtils.save(profile, integerProperty);
        Assert.assertEquals(newValue, profile.get(key).get());
    }

    @Test
    public void testSaveWithMapper() {
        String oldValue = "0";
        String mappedValue = "A";
        Pair<String, String> originalValue = new Pair<>(mappedValue, "B");
        String key = "pair";
        Property<Pair<String, String>> pairProperty = new SimpleObjectProperty<>(null, key, originalValue);
        Profile profile = new AppProfile();
        profile.set(key, oldValue);
        BindUtils.save(profile, pairProperty, Pair::getKey);
        Assert.assertEquals(mappedValue, profile.get(key).get());
    }

}
