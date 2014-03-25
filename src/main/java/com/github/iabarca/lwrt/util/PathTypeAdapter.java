
package com.github.iabarca.lwrt.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathTypeAdapter extends TypeAdapter<Path> {

    @Override
    public void write(JsonWriter out, Path value) throws IOException {
        if (value == null) {
            out.value("");
        } else {
            out.value(value.toAbsolutePath().toString());
        }
    }

    @Override
    public Path read(JsonReader in) throws IOException {
        if (in.hasNext()) {
            String name = in.nextString();
            return (name != null ? Paths.get(name) : Paths.get(""));
        }
        return null;
    }

}
