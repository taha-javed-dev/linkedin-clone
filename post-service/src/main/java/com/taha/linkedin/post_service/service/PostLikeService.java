package com.taha.linkedin.post_service.service;


import com.taha.linkedin.post_service.auth.UserContextHolder;
import com.taha.linkedin.post_service.entity.Post;
import com.taha.linkedin.post_service.entity.PostLike;
import com.taha.linkedin.post_service.event.PostLikeEvent;
import com.taha.linkedin.post_service.exceptions.BadRequestException;
import com.taha.linkedin.post_service.exceptions.ResourceNotFoundException;
import com.taha.linkedin.post_service.repository.PostLikeRepository;
import com.taha.linkedin.post_service.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostsRepository postsRepository;

    private final KafkaTemplate<Long, PostLikeEvent> kafkaTemplate;

    public void likePost(Long postId) {
        log.info("Attempting to like the post with id: {}", postId);

        Long userId = UserContextHolder.getCurrentUserId();

        boolean exists = postsRepository.existsById(postId);
        if(!exists) throw new ResourceNotFoundException("Post not found with id: "+postId);

        Post post = postsRepository.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found with id: "+postId));

        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLikeRepository.save(postLike);
        log.info("Post with id: {} liked successfully", postId);

        PostLikeEvent postLikeEvent = PostLikeEvent.builder()
                .postId(postId)
                .likedByUser(userId)
                .creatorId(post.getUserId())
                .build();

        kafkaTemplate.send("post-like-topic", postId, postLikeEvent);
    }

    public void unlikePost(Long postId) {
        log.info("Attempting to unlike the post with id: {}", postId);
        Long userId = UserContextHolder.getCurrentUserId();
        boolean exists = postsRepository.existsById(postId);
        if(!exists) throw new ResourceNotFoundException("Post not found with id: "+postId);

        boolean alreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(!alreadyLiked) throw new BadRequestException("Cannot unlike the post which is not liked.");

        postLikeRepository.deleteByUserIdAndPostId(userId, postId);

        log.info("Post with id: {} unliked successfully", postId);
    }
}
