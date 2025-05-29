package dooya.see.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "user.profile")
@Getter
@Setter
public class UserProfileProperties {

    private String defaultImageUrl;
}
