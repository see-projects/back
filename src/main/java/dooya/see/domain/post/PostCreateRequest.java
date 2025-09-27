package dooya.see.domain.post;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(
        @Size(min = 5, max = 100) String title,
        @Size(min = 5, max = 50000) String body,
        @NotNull PostCategory category,
        boolean publishImmediately,
        List<String> tags
) {
}
