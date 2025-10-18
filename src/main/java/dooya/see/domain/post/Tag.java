package dooya.see.domain.post;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.regex.Pattern;

@Embeddable
public record Tag(String name) implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Pattern TAG_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]+$");

    public Tag {
        name = normalizeTagName(name);
    }

    private String normalizeTagName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty())
            throw new IllegalArgumentException("태그명은 비어있을 수 없습니다");

        String trimmedName = rawName.trim();

        if (trimmedName.length() > 20)
            throw new IllegalArgumentException("태그명은 20자를 초과할 수 없습니다");

        if (!TAG_PATTERN.matcher(trimmedName).matches())
            throw new IllegalArgumentException("태그명은 한글, 영문, 숫자만 사용할 수 없습니다");

        return trimmedName.toLowerCase();
    }

    public String displayName() {
        return "#" + name;
    }

    public String urlSafe() {
        return name.toLowerCase();
    }
}
