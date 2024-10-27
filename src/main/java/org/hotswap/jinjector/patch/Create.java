package org.hotswap.jinjector.patch;

import java.util.ArrayList;
import java.util.List;

public class Create {
    private final String className;
    private final String _extends;
    private final List<String> _implements;
    private List<String> fields = new ArrayList<>();
    private List<String> constructors = new ArrayList<>();
    private List<String> methods = new ArrayList<>();

    public Create(String className, String _extends, List<String> _implements) {
        this.className = className;
        this._extends = _extends;
        this._implements = _implements;
    }

    public String getClassName() {
        return className;
    }

    public String get_extends() {
        return _extends;
    }

    public List<String> get_implements() {
        return _implements;
    }

    public List<String> getFields() {
        return fields;
    }

    public List<String> getConstructors() {
        return constructors;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void appendField(String src) {
        fields.add(src);
    }
    public void appendConstructor(String src) {
        constructors.add(src);
    }

    public void appendMethod(String src) {
        methods.add(src);
    }

}
