package dooya.see.adapter.search.elasticsearch.mapper;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import dooya.see.application.post.dto.PostSearchResult;
import dooya.see.domain.post.Post;
import dooya.see.domain.post.PostCategory;
import dooya.see.domain.post.PostMetaData;
import dooya.see.domain.post.Tag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Component
public class PostDocumentMapper {
    public PostDocument toDocument(Post post, String authorNickname) {
        requireNonNull(post, "게시글 정보는 필수입니다");
        requireNonNull(authorNickname, "작성자 닉네임은 필수입니다");

        PostMetaData metaData = requireNonNull(post.getMetaData(), "게시글 메타데이터가 없습니다");
        LocalDate createdAt = requireNonNull(metaData.createdAt(), "게시글 생성일이 없습니다").toLocalDate();

        return PostDocument.builder()
                .id(String.valueOf(post.getId()))
                .title(post.getContent().title())
                .content(post.getContent().body())
                .category(post.getCategory().name())
                .memberId(post.getMemberId())
                .authorNickname(authorNickname)
                .tags(post.getTags().stream()
                        .map(Tag::name)
                        .collect(Collectors.toList()))
                .createdAt(createdAt)
                .build();
    }

    public PostSearchResult toSearchResult(PostDocument document) {
        requireNonNull(document, "검색 문서는 필수입니다");

        Long id = null;
        if (document.getId() != null) {
            try {
                id = Long.parseLong(document.getId());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("검색 문서 ID는 숫자여야 합니다", ex);
            }
        }

        PostCategory category = document.getCategory() != null
                ? PostCategory.valueOf(document.getCategory())
                : null;

        List<String> tags = document.getTags() != null ? List.copyOf(document.getTags()) : List.of();

        return new PostSearchResult(
                id,
                document.getTitle(),
                document.getContent(),
                category,
                document.getMemberId(),
                document.getAuthorNickname(),
                tags,
                document.getCreatedAt()
        );
    }
}
