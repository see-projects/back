package dooya.see.domain.post;

import dooya.see.domain.AbstractEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends AbstractEntity {
    @Embedded
    private PostContent content;

    private Long memberId;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    private PostMetaData metaData;
}
