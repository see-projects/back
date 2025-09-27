package dooya.see.domain.post;

import java.util.regex.Pattern;

public record Tag(String name) {
    private static final Pattern TAG_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]+$");

    public Tag {
        name = name.toLowerCase();
    }

    public String displayName() {
        return "#" + name;
    }

    public String urlSafe() {
        return name.toLowerCase();
    }
}
