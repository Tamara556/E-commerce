package com.micro.ecommerce.repository;

import com.micro.ecommerce.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, String> {
}
