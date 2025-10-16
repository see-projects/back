package dooya.see.adapter.webapi.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record PostSearchRequest(
        String keyword,
        Integer page,
        Integer size
) {
    public Pageable toPageable() {
        return PageRequest.of(page != null ? page : 0, size != null ? size : 10);
    }
}
