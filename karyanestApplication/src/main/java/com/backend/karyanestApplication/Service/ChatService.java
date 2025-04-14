package com.backend.karyanestApplication.Service;

import com.backend.karyanestApplication.DTO.ChatRequest;
import com.backend.karyanestApplication.DTO.MessageRequest;
import com.backend.karyanestApplication.Model.Conversation;
import com.backend.karyanestApplication.Model.Message;
import com.backend.karyanestApplication.Model.User;
import com.backend.karyanestApplication.Repositry.ConversationRepository;
import com.backend.karyanestApplication.Repositry.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    public boolean isChatAllowed(Long Id, String senderRole) {
        // Block User ↔ Agent communication for property-related chat
        return !senderRole.equals("ROLE_USER") || !isAgent(Id);
    }
    public boolean canSendMessage(Long senderId, Long conversationId, String senderRole) {
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);

        if (conversation.isPresent()) {
            Conversation chat = conversation.get();

            // Allow if sender is the initiator or receiver
            boolean isParticipant = chat.getInitiatorId().equals(senderId) || chat.getReceiverId().equals(senderId);

            // Allow if sender is an admin
            boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(senderRole);

            // Message can be sent if the conversation is open and the sender is a participant or an admin
            return (isParticipant || isAdmin) && chat.getStatus() == Conversation.ConversationStatus.OPEN;
        }
        return false;
    }
   @Transactional
    public Conversation createChat(ChatRequest request, Long InitiatorId, Long ReceiverId) {
        Conversation conversation = new Conversation();
        conversation.setInitiatorId(InitiatorId);
        conversation.setReceiverId(ReceiverId);
        conversation.setType(Conversation.ConversationType.valueOf(request.getType()));
        conversation.setStatus(Conversation.ConversationStatus.OPEN);
        if (request.getType().equals("PROPERTY_INQUIRY")) {
            conversation.setPropertyId(request.getPropertyId());  // 🔥 Property ID Set Karo
        }
        return conversationRepository.save(conversation);
    }


    public ResponseEntity<List<String>> getMessages(Long conversationId) {
        List<Message> messages = messageRepository.findByConversationId(conversationId);

        if (messages == null || messages.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonList("No messages found for this conversation."));
        }

        // ✅ Extract unique participants
        Set<Long> participants = messages.stream()
                .map(Message::getSenderId)
                .collect(Collectors.toSet());

        // ✅ Check if the conversation has at least two participants
        if (participants.size() < 2) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonList("Conversation must have at least two participants."));
        }

        // ✅ Format messages properly with timestamp
        List<String> formattedMessages = messages.stream()
                .map(msg -> "User " + msg.getSenderId() + " [" + msg.getTimestamp() + "]: " + msg.getMessage())
                .collect(Collectors.toList());

        return ResponseEntity.ok(formattedMessages);
    }



    public Long getReceiverId(Long conversationId, Long senderId) {
        List<Message> messages = messageRepository.findByConversationId(conversationId);
        Set<Long> participants = messages.stream().map(Message::getSenderId).collect(Collectors.toSet());

        return participants.stream()
                .filter(id -> !id.equals(senderId))
                .findFirst()
                .orElse(null);
    }


    private boolean isAgent(Long userId) {
        User user = userService.findById(userId);
        return user.getUserRole().getName().equals("ROLE_AGENT");
    }

    public boolean isUserPartOfConversation(Long userId, Long conversationId) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        return conversationOpt.map(conversation ->
                        conversation.getInitiatorId().equals(userId) || conversation.getReceiverId().equals(userId))
                .orElse(false);
    }

    public void closeConversation(Long conversationId) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);

        if (conversationOpt.isPresent()) {
            Conversation conversation = conversationOpt.get();
            conversation.setStatus(Conversation.ConversationStatus.CLOSED);
            conversationRepository.save(conversation);
        } else {
            throw new RuntimeException("Conversation not found.");
        }
    }
    @Transactional
    public Message sendMessage(MessageRequest messageRequest,Long userId,String role) {
        // Find the conversation by ID
        Conversation conversation = conversationRepository.findById(messageRequest.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Create a new message entity
        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(userId);
        message.setMessage(messageRequest.getMessage());
        message.setTimestamp(LocalDateTime.now());
        if(role.equals("ROLE_ADMIN")) {
            updateReceiverIdIfAdminJoins(userId, messageRequest.getConversationId());
        }
        // Save the message to the database
        return messageRepository.save(message);
    }
    // Later, update receiverId if another admin joins
    @Transactional
    public void updateReceiverIdIfAdminJoins(Long newAdminId, Long conversationId) {
        // Find the conversation by ID
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found, we could not update it"));

        // Update the receiver ID
        conversation.setReceiverId(newAdminId);

        // Save the updated conversation
        conversationRepository.save(conversation);
    }

}
