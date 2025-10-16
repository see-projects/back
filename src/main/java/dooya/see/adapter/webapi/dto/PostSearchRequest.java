package dooya.see.adapter.webapi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record PostSearchRequest(
        String keyword,
        @Min(0)
        Integer page,
        @Min(1)
        @Max(100)
        Integer size
) {
    public Pageable toPageable() {
        return PageRequest.of(page != null ? page : 0, size != null ? size : 10);
    }
}
