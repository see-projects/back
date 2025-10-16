package dooya.see.adapter.search.elasticsearch.mapper;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import dooya.see.application.post.dto.PostSearchResult;
import dooya.see.domain.post.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostDocumentMapper {
    public PostDocument toDocument(Post post, String authorNickname) {
        if (post == null) {
            throw new IllegalArgumentException("");
        }

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
                .createdAt(post.getMetaData().createdAt().toLocalDate())
                .build();
    }

    public PostSearchResult toSearchResult(PostDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("");
        }

        PostCategory category = document.getCategory() != null
                ? PostCategory.valueOf(document.getCategory())
                : null;

        List<String> tags = document.getTags() != null ? List.copyOf(document.getTags()) : List.of();

        return new PostSearchResult(
                document.getId() != null ? Long.valueOf(document.getId()) : null,
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
