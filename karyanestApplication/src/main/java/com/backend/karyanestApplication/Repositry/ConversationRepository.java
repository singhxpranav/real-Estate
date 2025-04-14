package com.backend.karyanestApplication.Repositry;

import com.backend.karyanestApplication.Model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    List<Conversation> findByInitiatorIdOrReceiverId(Long initiatorId, Long receiverId);
}
