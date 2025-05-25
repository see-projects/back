package dooya.see.post.application.service.impl;

import dooya.see.common.exception.CustomException;
import dooya.see.common.exception.ErrorCode;
import dooya.see.post.application.service.PostQueryService;
import org.springframework.stereotype.Service;

@Service
public class PostQueryServiceImpl implements PostQueryService {

    @Override
    public void getPosts() {
        throw new CustomException(ErrorCode.POST_NOT_FOUNT);
    }
}
