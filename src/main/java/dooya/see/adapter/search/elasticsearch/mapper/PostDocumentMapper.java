package dooya.see.adapter.search.elasticsearch.mapper;

import dooya.see.adapter.search.elasticsearch.document.PostDocument;
import dooya.see.domain.post.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostDocumentMapper {
    public PostDocument toDocument(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("");
        }

        return PostDocument.builder()
                .id(String.valueOf(post.getId()))
                .title(post.getContent().title())
                .content(post.getContent().body())
                .category(post.getCategory().name())
                .memberId(post.getMemberId())
                .tags(post.getTags().stream()
                        .map(Tag::displayName)
                        .collect(Collectors.toList()))
                .createdAt(post.getMetaData().createdAt().toLocalDate())
                .build();

    }

    public Post toDomain(PostDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("");
        }

        PostMetaData metaData = new PostMetaData(
                document.getCreatedAt().atStartOfDay(),
                null,
                null
        );

        List<Tag> tags = document.getTags().stream()
                .map(Tag::new)
                .toList();

        return Post.fromSearchIndex(
                Long.valueOf(document.getId()),
                document.getTitle(),
                document.getContent(),
                PostCategory.valueOf(document.getCategory()),
                document.getMemberId(),
                tags,
                metaData
        );
    }
}
