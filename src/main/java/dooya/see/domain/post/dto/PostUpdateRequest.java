package dooya.see.domain.post.dto;

import dooya.see.domain.post.PostCategory;
import jakarta.validation.constraints.AssertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record PostUpdateRequest(
        Optional<String> title,
        Optional<String> body,
        Optional<PostCategory> category,
        Optional<List<String>> tags
) {
    public PostUpdateRequest {
        title = normalizeStringOptional(title);
        body = normalizeStringOptional(body);
        category = category == null ? Optional.empty() : category;
        tags = sanitizeTags(tags);
    }

    public boolean hasAnyUpdate() {
        return title.isPresent() || body.isPresent() || category.isPresent() || tags.isPresent();
    }

    @AssertTrue(message = "게시글 제목은 비어있을 수 없습니다")
    public boolean isTitlePresentValid() {
        return title.map(value -> !value.trim().isEmpty()).orElse(true);
    }

    @AssertTrue(message = "게시글 제목은 100자를 초과할 수 없습니다")
    public boolean isTitleLengthValid() {
        return title.map(value -> value.length() <= 100).orElse(true);
    }

    @AssertTrue(message = "게시글 내용은 비어있을 수 없습니다")
    public boolean isBodyPresentValid() {
        return body.map(value -> !value.trim().isEmpty()).orElse(true);
    }

    @AssertTrue(message = "게시글 내용은 50,000자를 초과할 수 없습니다")
    public boolean isBodyLengthValid() {
        return body.map(value -> value.length() <= 50_000).orElse(true);
    }

    @AssertTrue(message = "태그 목록에 비어있거나 null인 값이 포함될 수 없습니다")
    public boolean isTagsContentValid() {
        return tags.map(values -> values.stream().allMatch(value -> value != null && !value.trim().isEmpty()))
                .orElse(true);
    }

    private static Optional<String> normalizeStringOptional(Optional<String> value) {
        return value == null ? Optional.empty() : value;
    }

    private static Optional<List<String>> sanitizeTags(Optional<List<String>> value) {
        if (value == null || value.isEmpty()) {
            return Optional.empty();
        }
        List<String> sanitized = value.get().stream()
                .map(tag -> tag == null ? null : tag.trim())
                .collect(Collectors.toCollection(ArrayList::new));
        return Optional.of(Collections.unmodifiableList(sanitized));
    }
}
