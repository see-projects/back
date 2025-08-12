package dooya.see.domain.post;

import dooya.see.domain.AbstractEntity;
import dooya.see.domain.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Post extends AbstractEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    Member member;

    String title;

    String content;

    public static Post createPost(String title, String content, Member member) {
        return Post.builder()
                .member(member)
                .title(title)
                .content(content)
                .build();
    }
}
