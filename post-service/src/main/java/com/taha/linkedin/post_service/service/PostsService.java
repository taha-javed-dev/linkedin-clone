package com.taha.linkedin.post_service.service;

import com.taha.linkedin.post_service.auth.UserContextHolder;
import com.taha.linkedin.post_service.dto.PostCreateRequestDto;
import com.taha.linkedin.post_service.dto.PostDto;
import com.taha.linkedin.post_service.entity.Post;
import com.taha.linkedin.post_service.event.PostCreatedEvent;
import com.taha.linkedin.post_service.exceptions.ResourceNotFoundException;
import com.taha.linkedin.post_service.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostsService {

    private final PostsRepository postsRepository;
    private final ModelMapper modelMapper;

    private final KafkaTemplate<Long, PostCreatedEvent> kafkaTemplate;

    public PostDto createPost(PostCreateRequestDto postDto) {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("User with id : {} creating post ", userId);
        Post post = modelMapper.map(postDto, Post.class);
        post.setUserId(userId);

        Post savedPost = postsRepository.save(post);

        log.info("post created!!!!!!");
        PostCreatedEvent postCreatedEvent = PostCreatedEvent.builder()
                .postId(savedPost.getId())
                .creatorId(userId)
                .content(savedPost.getContent())
                .build();

        kafkaTemplate.send("post-created-topic", postCreatedEvent);

        return modelMapper.map(savedPost, PostDto.class);
    }

    public List<PostDto> getAllPostsOfUser(Long userId) {
        List<Post> posts = postsRepository.findByUserId(userId);
        return posts.stream()
                .map( (post) -> modelMapper.map(post, PostDto.class))
                .toList();

    }

    public PostDto getPostById(Long postId) {
        Post post = postsRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post does not exists"));
        return modelMapper.map(post, PostDto.class);
    }
}
