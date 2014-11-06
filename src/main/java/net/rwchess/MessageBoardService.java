package net.rwchess;

import java.util.List;

/**
 * Created by bodia on 10/11/14.
 */
public interface MessageBoardService {

    List<Message> listMessages();
    void postMessage(Message message);
    void deleteMessage(Message message);
    Message findMessageById(Long messageId);
}
