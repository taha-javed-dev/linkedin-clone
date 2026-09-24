package com.taha.linkedin.notification_service.consumer;

import com.taha.linkedin.notification_service.clients.ConnectionsClient;
import com.taha.linkedin.notification_service.dto.PersonDto;
import com.taha.linkedin.notification_service.entity.Notification;
import com.taha.linkedin.notification_service.repository.NotificationRepository;
import com.taha.linkedin.posts_service.event.PostCreatedEvent;
import com.taha.linkedin.posts_service.event.PostLikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostsServiceConsumer {

    private final ConnectionsClient connectionsClient;
    private final NotificationRepository notificationRepository;


    @KafkaListener(topics = "post-created-topic")
    public void handlePostCreated(PostCreatedEvent postCreatedEvent) {

        log.info("Sending notifications: handlePostCreated");
        List<PersonDto> connections = connectionsClient.getFirstConnections(postCreatedEvent.getCreatorId());

        for (PersonDto connection: connections) {
            sendNotification(connection.getUserId(), "Your connection "+postCreatedEvent.getCreatorId()+"has created a post, Check it out");
        }
    }

    @KafkaListener(topics = "post-liked-event")
    public void handlePostLiked(PostLikeEvent postLikeEvent) {
        log.info("Sending notifications: handlePostLiked");
        String message = String.format("Your post, %d has been liked by %d",postLikeEvent.getPostId(),
                postLikeEvent.getLikedByUser());

        sendNotification(postLikeEvent.getCreatorId(), message);
    }

    public void sendNotification(Long userId, String message) {

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setUserId(userId);
        notificationRepository.save(notification);
    }

}
