package dooya.see.domain.post;

import java.util.regex.Pattern;

public record Tag(String name) {
    private static final Pattern TAG_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]+$");

    public Tag {
        validateTagName(name);
        name = name.toLowerCase();
    }

    private static void validateTagName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("태그명은 비어있을 수 없습니다");

        String trimmedName = name.trim();

        if (trimmedName.length() > 20)
            throw new IllegalArgumentException("태그명은 20자를 초과할 수 없습니다");

        if (!TAG_PATTERN.matcher(trimmedName).matches())
            throw new IllegalArgumentException("태그명은 한글, 영문, 숫자만 사용할 수 있습니다");
    }

    public String displayName() {
        return "#" + name;
    }

    public String urlSafe() {
        return name.toLowerCase();
    }
}
