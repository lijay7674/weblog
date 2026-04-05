package com.forum.post.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 配置 Exchange、Queue 及消息转换器
 */
@Configuration
public class RabbitMQConfig {

    // ==================== Exchange 定义 ====================

    /**
     * 帖子模块交换机 - Topic 类型
     */
    public static final String EXCHANGE_FORUM_POST = "exchange.forum.post";
    
    /**
     * 死信交换机
     */
    public static final String EXCHANGE_DLX = "exchange.forum.dlx";

    // ==================== RoutingKey 定义 ====================
    
    public static final String ROUTING_KEY_LIKE_SYNC = "post.like.sync";  // 点赞消息
    public static final String ROUTING_KEY_COMMENT_NEW = "post.comment.new";  // 评论消息
    public static final String ROUTING_KEY_VIEW_SYNC = "post.view.sync";  //览量消息
    public static final String ROUTING_KEY_SEARCH_SYNC = "post.search.sync";  // 搜索同步消息
    public static final String ROUTING_KEY_DLX_LIKE = "dlx.post.like";

    // ==================== Queue 定义 ====================
    
    public static final String QUEUE_LIKE_SYNC = "queue.post.like.sync";  // 点赞同步队列
    public static final String QUEUE_COMMENT_NOTIFY = "queue.post.comment.notify";  // 评论通知队列
    public static final String QUEUE_VIEW_SYNC = "queue.post.view.sync";  //览量同步队列
    public static final String QUEUE_DLX_LIKE = "queue.dlx.post.like";  //赞死信队列
    // ==================== Exchange Bean ====================
    
    @Bean
    public TopicExchange postExchange() {
        return new TopicExchange(EXCHANGE_FORUM_POST, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    // ==================== Queue Bean ====================
    
    /**
     * 点赞同步队列
     * 用于异步同步点赞数据到数据库（作为Redis定时同步的补偿机制）
     */
    @Bean
    public Queue likeSyncQueue() {
        return QueueBuilder.durable(QUEUE_LIKE_SYNC)
                .withArgument("x-message-ttl", 86400000)  // 消息过期时间24小时
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLX_LIKE)
                .build();
    }

    /**
     * 评论通知队列
     * 用于新评论时异步发送通知
     */
    @Bean
    public Queue commentNotifyQueue() {
        return QueueBuilder.durable(QUEUE_COMMENT_NOTIFY)
                .withArgument("x-message-ttl", 604800000)  // 消息过期时间7天
                .build();
    }

    /**
     * 浏览量同步队列
     * 用于异步批量更新博客浏览量
     */
    @Bean
    public Queue viewSyncQueue() {
        return QueueBuilder.durable(QUEUE_VIEW_SYNC)
                .build();
    }

    /**
     * 点赞死信队列
     * 用于处理点赞同步失败的消息
     */
    @Bean
    public Queue likeDlxQueue() {
        return QueueBuilder.durable(QUEUE_DLX_LIKE).build();
    }

    // ==================== Binding ====================
    
    @Bean
    public Binding likeSyncBinding(Queue likeSyncQueue, TopicExchange postExchange) {
        return BindingBuilder.bind(likeSyncQueue).to(postExchange).with(ROUTING_KEY_LIKE_SYNC);
    }

    @Bean
    public Binding commentNotifyBinding(Queue commentNotifyQueue, TopicExchange postExchange) {
        return BindingBuilder.bind(commentNotifyQueue).to(postExchange).with(ROUTING_KEY_COMMENT_NEW);
    }

    @Bean
    public Binding viewSyncBinding(Queue viewSyncQueue, TopicExchange postExchange) {
        return BindingBuilder.bind(viewSyncQueue).to(postExchange).with(ROUTING_KEY_VIEW_SYNC);
    }

    @Bean
    public Binding likeDlxBinding(Queue likeDlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(likeDlxQueue).to(dlxExchange).with(ROUTING_KEY_DLX_LIKE);
    }

    // ==================== 消息转换器 ====================
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    /**
     * 批量消费监听器容器工厂
     * 用于批量处理浏览量同步消息
     */
    @Bean
    public SimpleRabbitListenerContainerFactory batchListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setBatchListener(true);        // 启用批量监听
        factory.setBatchSize(100);             // 每批最多 100 条
        factory.setConsumerBatchEnabled(true); // 开启批量消费
        return factory;
    }
}
