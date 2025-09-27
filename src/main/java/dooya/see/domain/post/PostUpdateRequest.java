package dooya.see.domain.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Optional;

public record PostUpdateRequest(
        Optional<@NotBlank @Size(max = 100) String> title,
        Optional<@NotBlank @Size(max = 50000) String> body,
        Optional<@NotNull PostCategory> category,
        Optional<List<String>> tags
) {
    public boolean hasAnyUpdate() {
        return title.isPresent() || body.isPresent() || category.isPresent();
    }
}
