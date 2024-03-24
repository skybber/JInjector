package org.hotswap.patcher.patch;

import java.util.ArrayList;
import java.util.List;

public class Patch {
    private List<Create> creations = new ArrayList<>();
    private List<Transform> transforms = new ArrayList<>();

    public List<Create> getCreations() {
        return creations;
    }

    public List<Transform> getTransforms() {
        return transforms;
    }

    public void addTransform(Transform transform) {
        transforms.add(transform);
    }

    public void addCreate(Create create) {
        creations.add(create);
    }
}
