package com.backend.karyanestApplication.Repositry;

import com.backend.karyanestApplication.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByConversationId(Long conversationId);
}
