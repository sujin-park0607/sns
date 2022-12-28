package com.likelion.finalproject.service;

import com.likelion.finalproject.domain.dto.post.PostGetResponse;
import com.likelion.finalproject.domain.dto.post.PostRequest;
import com.likelion.finalproject.domain.entity.Post;
import com.likelion.finalproject.domain.entity.User;
import com.likelion.finalproject.exception.AppException;
import com.likelion.finalproject.exception.ErrorCode;
import com.likelion.finalproject.repository.PostRepository;
import com.likelion.finalproject.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostServiceTest {

    PostService postService;

    PostRepository postRepository =  mock(PostRepository.class);
    UserRepository userRepository = mock(UserRepository.class);

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, userRepository);
    }

    @Test
    @DisplayName("등록 성공")
    void postAdd_success() {
        Post mockPost = mock(Post.class);
        User mockUser = mock(User.class);

        when(userRepository.findByUserName(mockUser.getUserName())).thenReturn(Optional.of(mockUser));
        when(postRepository.save(any())).thenReturn(mockPost);

        Assertions.assertDoesNotThrow(() -> postService.add(new PostRequest(mockPost.getTitle(), mockPost.getBody()), mockUser.getUserName()));
    }

    @Test
    @DisplayName("등록 실패 : 유저가 존재하지 않을 때")
    void postAdd_fail() {
        Post mockPost = mock(Post.class);
        User mockUser = mock(User.class);

        when(postRepository.save(any())).thenReturn(mockPost);
        when(userRepository.findByUserName(mockUser.getUserName())).thenReturn(Optional.empty());


        AppException exception = Assertions.assertThrows(AppException.class, ()-> {
            postService.add(new PostRequest(mockPost.getTitle(), mockPost.getBody()),mockUser.getUserName());
        });

        Assertions.assertEquals(ErrorCode.USERNAME_NOT_FOUND, exception.getErrorCode());

    }

    @Test
    @DisplayName("조회 성공")
    void postGet_success() {
        User user = mock(User.class);

        //test데이터
        Post post = Post.builder()
                .user(user)
                .title("제목")
                .body("내용")
                .build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        PostGetResponse postDto = postService.getPost(post.getId());

        Assertions.assertEquals(postDto.getUserName(), post.getUser().getUserName());
    }

    @Test
    @DisplayName("수정 실패 : 포스트 존재하지 않음")
    void postUpdate_fail_non_post() {
        User user = mock(User.class);

        //test데이터
        Post post = Post.builder()
                .user(user)
                .title("제목")
                .body("내용")
                .build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        AppException exception = Assertions.assertThrows(AppException.class, ()-> {
            postService.update(post.getId(), new PostRequest(post.getTitle(), post.getBody()),user.getUserName());
        });

        Assertions.assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("수정 실패 : 작성자!=유저")
    void postUpdate_fail_mismatch_author_user() {
        User user1 = User.builder()
                .userName("hello")
                .build();

        User user2 = User.builder()
                .userName("sujin")
                .build();

        //test데이터
        Post post = Post.builder()
                .user(user1)
                .title("제목")
                .body("내용")
                .build();


        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findByUserName(user2.getUserName())).thenReturn(Optional.of(user2));

        AppException exception = Assertions.assertThrows(AppException.class, ()-> {
            postService.update(post.getId(), new PostRequest(post.getTitle(), post.getBody()),user2.getUserName());
        });
        Assertions.assertEquals(ErrorCode.INVALID_PERMISSION, exception.getErrorCode());
    }

    @Test
    @DisplayName("수정 실패 : 유저 존재하지 않음")
    void postUpdate_fail_non_user() {
        User user = mock(User.class);

        //test데이터
        Post post = Post.builder()
                .user(user)
                .title("제목")
                .body("내용")
                .build();


        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findByUserName(user.getUserName())).thenReturn(Optional.empty());

        AppException exception = Assertions.assertThrows(AppException.class, ()-> {
            postService.update(post.getId(), new PostRequest(post.getTitle(), post.getBody()),user.getUserName());
        });
        Assertions.assertEquals(ErrorCode.USERNAME_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("삭제 실패 : 포스트 존재하지 않음")
    void postDelete_fail_non_post() {
        User user = mock(User.class);
        Post post = mock(Post.class);

        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());
        when(userRepository.findByUserName(user.getUserName())).thenReturn(Optional.of(user));

        AppException exception = Assertions.assertThrows(AppException.class, ()-> {
            postService.delete(post.getId() ,user.getUserName());
        });

        Assertions.assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("삭제 실패 : 유저 존재하지 않음")
    void postDelete_fail_non_user() {
        User user = mock(User.class);
        Post post = mock(Post.class);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findByUserName(user.getUserName())).thenReturn(Optional.empty());

        AppException exception = Assertions.assertThrows(AppException.class, ()-> {
            postService.delete(post.getId() ,user.getUserName());
        });

        Assertions.assertEquals(ErrorCode.USERNAME_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("삭제 실패 : 작성자와 유저가 같지 않음")
    void postDelete_fail_mismatch_user_author() {
        User user1 = User.builder()
                .userName("hello")
                .build();

        User user2 = User.builder()
                .userName("sujin")
                .build();

        //test데이터
        Post post = Post.builder()
                .user(user1)
                .title("제목")
                .body("내용")
                .build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findByUserName(user2.getUserName())).thenReturn(Optional.of(user2));

        AppException exception = Assertions.assertThrows(AppException.class, ()-> {
            postService.delete(post.getId() ,user2.getUserName());
        });

        Assertions.assertEquals(ErrorCode.INVALID_PERMISSION, exception.getErrorCode());
    }

}