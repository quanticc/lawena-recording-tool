package com.github.lawena.repository;

import com.github.lawena.domain.Tag;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TagRepository {

    private final Map<String, Tag> tagMap = new HashMap<>();

    public Tag save(Tag tag) {
        tagMap.put(tag.getName(), tag);
        return tag;
    }

    public Optional<Tag> findOne(String name) {
        return Optional.ofNullable(tagMap.get(name));
    }

    public Tag findOneOrSave(Tag tag) {
        return tagMap.computeIfAbsent(tag.getName(), k -> tag);
    }

    public List<Tag> findAll() {
        return new ArrayList<>(tagMap.values());
    }
}
