package com.forum.post.mq;

import com.forum.post.config.RabbitMQConfig;
import com.forum.post.service.BlogService;
import com.forum.post.service.CommentService;
import com.forum.post.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 帖子模块消息监听器
 * 处理点赞同步、评论通知、浏览量同步等消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventListener {

    private final LikeService likeService;
    private final BlogService blogService;
    private final CommentService commentService;

    /**
     * 处理点赞同步消息
     * 作为Redis批量同步的补偿机制，处理单条紧急同步场景
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_LIKE_SYNC)
    public void handleLikeSync(LikeEvent event) {
        log.info("接收到点赞同步消息: {}", event);
        try {
            likeService.syncToDatabase(event.getUserId(), event.getTargetId(), 
                    event.getTargetType(), event.getLiked());
        } catch (Exception e) {
            log.error("点赞同步失败: {}", event, e);
        }
    }

    /**
     * 处理评论通知消息
     * 发送系统通知给博客作者或被回复者
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_COMMENT_NOTIFY)
    public void handleCommentNotify(CommentEvent event) {
        log.info("接收到评论通知消息: {}", event);
        try {
            if (event.getParentId() > 0 && event.getToUserId() != null) {
                // 回复评论，通知被回复者
                sendReplyNotification(event);
            } else {
                // 一级评论，通知博客作者
                sendCommentNotification(event);
            }
        } catch (Exception e) {
            log.error("评论通知处理失败: {}", event, e);
        }
    }

    /**
     * 处理浏览量同步消息
     * 批量更新博客浏览量
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_VIEW_SYNC, containerFactory = "batchListenerContainerFactory")
    public void handleViewSync(List<ViewEvent> events) {
        log.info("批量处理浏览量同步，数量: {}", events.size());
        try {
            // 按博客ID分组统计浏览量
            java.util.Map<Long, Long> viewCountMap = new java.util.HashMap<>();
            for (ViewEvent event : events) {
                viewCountMap.merge(event.getBlogId(), 1L, Long::sum);
            }
            
            // 批量更新浏览量
            for (java.util.Map.Entry<Long, Long> entry : viewCountMap.entrySet()) {
                blogService.incrementViewCount(entry.getKey());
            }
        } catch (Exception e) {
            log.error("浏览量同步失败", e);
        }
    }

    /**
     * 处理点赞死信消息
     * 记录失败消息，后续人工处理
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_DLX_LIKE)
    public void handleLikeDlx(LikeEvent event) {
        log.warn("点赞消息进入死信队列: {}", event);
        // 可以记录到数据库或发送告警
    }

    // ==================== 私有方法 ====================

    /**
     * 发送回复通知
     */
    private void sendReplyNotification(CommentEvent event) {
        // TODO: 调用消息服务发送通知
        log.info("发送回复通知给用户: {}, 评论ID: {}", event.getToUserId(), event.getCommentId());
    }

    /**
     * 发送评论通知
     */
    private void sendCommentNotification(CommentEvent event) {
        // TODO: 调用消息服务发送通知
        // 需要先查询博客作者ID
        log.info("发送评论通知，博客ID: {}, 评论ID: {}", event.getBlogId(), event.getCommentId());
    }
}
