package au.com.codeka.warworlds.server.ctrl;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.joda.time.DateTime;

import au.com.codeka.common.Log;
import au.com.codeka.common.protobuf.Messages;
import au.com.codeka.warworlds.server.BackgroundRunner;
import au.com.codeka.warworlds.server.Configuration;
import au.com.codeka.warworlds.server.RequestException;
import au.com.codeka.warworlds.server.data.DB;
import au.com.codeka.warworlds.server.data.SqlResult;
import au.com.codeka.warworlds.server.data.SqlStmt;
import au.com.codeka.warworlds.server.data.Transaction;
import au.com.codeka.warworlds.server.model.Alliance;
import au.com.codeka.warworlds.server.model.ChatBlock;
import au.com.codeka.warworlds.server.model.ChatConversation;
import au.com.codeka.warworlds.server.model.ChatMessage;
import au.com.codeka.warworlds.server.model.Empire;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

public class ChatController {
  private final Log log = new Log("ChatController");
  private DataBase db;

  // If true, we'll log all chat messages to the servers log as well.
  private static final boolean LOG_CHAT_MSGS = true;

  public ChatController() {
    db = new DataBase();
  }

  public ChatController(Transaction trans) {
    db = new DataBase(trans);
  }

  public ArrayList<ChatConversation> getConversationsForEmpire(int empireID) throws RequestException {
    try {
      return db.getConversationsForEmpire(empireID);
    } catch (Exception e) {
      throw new RequestException(e);
    }
  }

  public ChatConversation getConversation(int id) throws RequestException {
    try {
      return db.getConversation(id);
    } catch (Exception e) {
      throw new RequestException(e);
    }
  }

  public ArrayList<ChatConversation> getAllConversations() throws RequestException {
    try {
      return db.getConversations("1 = 1");
    } catch (Exception e) {
      throw new RequestException(e);
    }
  }

  public void addParticipant(ChatConversation conversation, int empireID) throws RequestException {
    try {
      db.addParticipant(conversation.getID(), empireID);
      conversation.addParticipant(empireID, false);
    } catch (Exception e) {
      throw new RequestException(e);
    }
  }

  public void removeParticipant(ChatConversation conversation, int empireID) throws RequestException {
    try {
      db.removeParticipant(conversation.getID(), empireID);
      int index = -1;
      for (int i = 0; i < conversation.getParticipants().size(); i++) {
        if (conversation.getParticipants().get(i).getEmpireID() == empireID) {
          index = i;
        }
      }
      if (index >= 0) {
        conversation.getParticipants().remove(index);
      }
    } catch (Exception e) {
      throw new RequestException(e);
    }
  }

  /**
   * Gets a list of all the empires the given empire has blocked. Not to be confused with
   * {@link #getBlockingEmpires(int)}.
   */
  public List<ChatBlock> getBlocksForEmpire(int empireID) throws RequestException {
    try {
      return db.getBlocksForEmpire(empireID);
    } catch (Exception e) {
      throw new RequestException(e);
    }
  }

  /**
   * Gets a list of all empires that have the given empire blocked. Not to be confused with
   * {@link #getBlocksForEmpire(int)}.
   */
  public List<ChatBlock> getBlockingEmpires(int empireID) throws RequestException {
    try {
      return db.getBlockingEmpires(empireID);
    } catch (Exception e) {
      throw new RequestException(e);
    }
  }

  public void addBlock(ChatBlock block) throws RequestException {
    try {
      db.addBlock(block);
    } catch (Exception e) {
      throw new RequestException(e, block);
    }
  }

  public void removeBlock(ChatBlock block) throws RequestException {
    try {
      db.removeBlock(block);
    } catch (Exception e) {
      throw new RequestException(e, block);
    }
  }

  public void postMessage(ChatMessage msg) throws RequestException {
    if (msg.getMessage().length() > 8) {
      double emojiFraction =
          (double) countEmojis(msg.getMessage().toCharArray()) / msg.getMessage().length();
      if (emojiFraction > Configuration.i.getLimits().getMaxEmojiRatio()) {
        throw new RequestException(400, "Too many emojis (" + emojiFraction + ")");
      }
    }

    msg.setDatePosted(DateTime.now());
    String msg_native = msg.getMessage();
    String msg_en = new TranslateController().translate(msg_native);
    if (msg_en != null) {
      msg.setEnglishMessage(msg_en);
    }

    int profanityLevel = ProfanityFilter.filter(msg_en == null ? msg_native : msg_en);

    if (LOG_CHAT_MSGS) {
      String empireName;
      String convName = "";
      if (msg.getEmpireID() != null) {
        Empire empire = new EmpireController().getEmpire(msg.getEmpireID());
        if (empire != null) {
          empireName = String.format(Locale.ENGLISH, "[%d] %s", empire.getID(), empire.getDisplayName());
        } else {
          empireName = "[??] ???";
        }

        if (msg.getAllianceKey() != null) {
          Alliance alliance = new AllianceController().getAlliance(msg.getAllianceID());
          convName = String.format("{%s} ", alliance.getName());
        }
      } else {
        empireName = "[SERVER]";
      }

      if (msg.getConversationID() != null && msg.getConversationID() > 0) {
        convName = String.format(Locale.ENGLISH, "[private %d] ", msg.getConversationID());
      }

      log.info("%s > %s%s", empireName, convName, msg.getMessage());
      if (!Strings.isNullOrEmpty(msg.getEnglishMessage())) {
        log.info("%s > %s<%s>", empireName, convName, msg.getEnglishMessage());
      }
    }

    String sql = "INSERT INTO chat_messages (empire_id, alliance_id, message, message_en,"
        + " profanity_level, posted_date, conversation_id, action) VALUES"
        + " (?, ?, ?, ?, ?, ?, ?, ?)";
    try (SqlStmt stmt = DB.prepare(sql, Statement.RETURN_GENERATED_KEYS)) {
      if (msg.getEmpireKey() != null) {
        stmt.setInt(1, msg.getEmpireID());
        if (msg.getAllianceKey() != null) {
          stmt.setInt(2, msg.getAllianceID());
        } else {
          stmt.setNull(2);
        }
      } else {
        stmt.setNull(1);
        stmt.setNull(2);
      }

      stmt.setString(3, msg.getMessage());
      stmt.setString(4, msg.getEnglishMessage());
      stmt.setInt(5, profanityLevel);
      stmt.setDateTime(6, msg.getDatePosted());

      if (msg.getConversationID() != null && msg.getConversationID() > 0) {
        stmt.setInt(7, msg.getConversationID());
      } else {
        stmt.setNull(7);
      }

      if (msg.getAction() == null || msg.getAction() == ChatMessage.MessageAction.Normal) {
        stmt.setNull(8);
      } else {
        stmt.setInt(8, msg.getAction().getValue());
      }

      stmt.update();
      msg.setID(stmt.getAutoGeneratedID());
    } catch (Exception e) {
      throw new RequestException(e);
    }

    // send notifications on a background thread, it can take a while...
    final ChatMessage chatmsg = msg;
    new BackgroundRunner() {
      @Override
      protected void doInBackground() {
        // escape the HTML before sending the notification out
        Messages.ChatMessage.Builder chat_msg_pb = Messages.ChatMessage.newBuilder();
        chatmsg.toProtocolBuffer(chat_msg_pb, true);

        String encoded = getEncodedMessage(chatmsg);
        try {
          Set<Integer> exclusions = new HashSet<>();
          if (msg.getEmpireID() != null) {
            List<ChatBlock> blocking = getBlockingEmpires(msg.getEmpireID());
            for (ChatBlock block : blocking) {
              exclusions.add(block.getEmpireID());
            }
          }

          if (chat_msg_pb.hasConversationId()) {
            new NotificationController().sendNotificationToConversation(
                chat_msg_pb.getConversationId(), "chat", encoded);
          } else if (chat_msg_pb.hasAllianceKey()) {
            new NotificationController().sendNotificationToOnlineAlliance(
                Integer.parseInt(chat_msg_pb.getAllianceKey()), "chat", encoded, exclusions);
          } else {
            new NotificationController().sendNotificationToAllOnline(
                "chat", encoded, exclusions);
          }
        } catch (RequestException e) {
          log.error("Error sending notification.");
        }
      }
    }.execute();
  }

  /**
   * Counts the number of emojis in the given char array.
   */
  private int countEmojis(char[] chars) {
    int count = 0;
    int index = 0;
    while (index < chars.length - 1) {
      if (chars[index] == 0xD83C) {
        if (chars[index + 1] >= 0xDF00 && chars[index + 1] <= 0xDFFF) {
          count++;
        }
      } else if (chars[index] == 0xD83D) {
        if (chars[index + 1] >= 0xDC00 && chars[index + 1] <= 0xDDFF) {
          count++;
        }
      }
      ++index;
    }
    return count;
  }

  /**
   * Encodes the given message, ready to be used in a notification.
   */
  private String getEncodedMessage(ChatMessage msg) {
    Messages.ChatMessage.Builder chat_msg_pb = Messages.ChatMessage.newBuilder();
    msg.toProtocolBuffer(chat_msg_pb, true);

    return BaseEncoding.base64().encode(chat_msg_pb.build().toByteArray());
  }

  /**
   * Search for an existing conversation between the two given empires. An existing conversation is one
   * where the last message was sent within the last week and only the given two empires are participants.
   */
  public ChatConversation findExistingConversation(int empireID1, int empireID2) throws RequestException {
    // make sure empireID1 is the larger, since that's what our SQL query assumes
    if (empireID1 < empireID2) {
      int tmp = empireID1;
      empireID1 = empireID2;
      empireID2 = tmp;
    }

    try {
      return db.findExistingConversation(empireID1, empireID2);
    } catch (Exception e) {
      throw new RequestException(e);
    }
  }

  public ChatConversation createConversation(int empireID1, int empireID2) throws RequestException {
    ChatConversation conversation = findExistingConversation(empireID1, empireID2);
    if (conversation == null) {
      try {
        log.info(String.format("Creating new conversation between %1d and %2d", empireID1, empireID2));
        conversation = db.createConversation(empireID1, empireID2);
      } catch (Exception e) {
        throw new RequestException(e);
      }
    }
    return conversation;
  }

  private static class DataBase extends BaseDataBase {
    DataBase() {
      super();
    }

    DataBase(Transaction trans) {
      super(trans);
    }

    ChatConversation findExistingConversation(int empireID1, int empireID2) throws Exception {
      Integer chatID = null;
      String sql = "SELECT chat_conversations.id, MAX(empire_id) AS empire_id_1, MIN(empire_id) AS empire_id_2, COUNT(*) AS num_empires" +
          " FROM chat_conversations" +
          " INNER JOIN chat_conversation_participants ON conversation_id = chat_conversations.id" +
          " GROUP BY chat_conversations.id" +
          " HAVING COUNT(*) = 2" +
          " AND MAX(empire_id) = ?" +
          " AND MIN(empire_id) = ?";
      try (SqlStmt stmt = prepare(sql)) {
        stmt.setInt(1, empireID1);
        stmt.setInt(2, empireID2);
        SqlResult res = stmt.select();
        if (res.next()) {
          chatID = res.getInt(1);
        }
      }

      if (chatID == null) {
        return null;
      }

      ArrayList<ChatConversation> conversations = getConversations("chat_conversations.id = " + chatID);
      return conversations.get(0);
    }

    ArrayList<ChatConversation> getConversationsForEmpire(int empireID) throws Exception {
      String whereClause = "chat_conversations.id IN (" +
          "SELECT conversation_id FROM chat_conversation_participants WHERE empire_id = " + empireID + ")";
      return getConversations(whereClause);
    }

    ArrayList<ChatConversation> getConversations(String whereClause) throws Exception {
      Map<Integer, ChatConversation> conversations = new HashMap<Integer, ChatConversation>();
      String sql = "" +
          "SELECT chat_conversations.id, chat_conversation_participants.empire_id," +
          "       chat_conversation_participants.is_muted " +
          "FROM chat_conversations " +
          "INNER JOIN chat_conversation_participants ON conversation_id = chat_conversations.id " +
          "WHERE " + whereClause;
      try (SqlStmt stmt = prepare(sql)) {
        SqlResult res = stmt.select();
        while (res.next()) {
          int conversationID = res.getInt(1);
          ChatConversation conversation = conversations.get(conversationID);
          if (conversation == null) {
            conversation = new ChatConversation(conversationID);
            conversations.put(conversationID, conversation);
          }
          conversation.addParticipant(res.getInt(2), res.getInt(3) != 0);
        }
      }
      return new ArrayList<>(conversations.values());
    }

    ChatConversation getConversation(int id) throws Exception {
      ArrayList<ChatConversation> conversations = getConversations("chat_conversations.id = " + id);
      if (conversations.size() == 1) {
        return conversations.get(0);
      }
      return null;
    }

    void addParticipant(int conversationID, int empireID) throws Exception {
      String sql = "INSERT INTO chat_conversation_participants (conversation_id, empire_id, is_muted) VALUES" +
          " (?, ?, 0)";
      try (SqlStmt stmt = prepare(sql)) {
        stmt.setInt(1, conversationID);
        stmt.setInt(2, empireID);
        stmt.update();
      }
    }

    void removeParticipant(int conversationID, int empireID) throws Exception {
      String sql = "DELETE FROM chat_conversation_participants WHERE conversation_id = ? AND empire_id = ?";
      try (SqlStmt stmt = prepare(sql)) {
        stmt.setInt(1, conversationID);
        stmt.setInt(2, empireID);
        stmt.update();
      }
    }

    ChatConversation createConversation(int empireID1, int empireID2) throws Exception {
      ChatConversation conversation;

      String sql = "INSERT INTO chat_conversations DEFAULT VALUES";
      try (SqlStmt stmt = prepare(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.update();
        conversation = new ChatConversation(stmt.getAutoGeneratedID());
      }

      sql = "INSERT INTO chat_conversation_participants (conversation_id, empire_id, is_muted) VALUES" +
          " (?, ?, 0)";
      if (empireID1 != empireID2) {
        sql += ", (?, ?, 0)";
      }
      try (SqlStmt stmt = prepare(sql)) {
        stmt.setInt(1, conversation.getID());
        stmt.setInt(2, empireID1);
        if (empireID1 != empireID2) {
          stmt.setInt(3, conversation.getID());
          stmt.setInt(4, empireID2);
        }
        stmt.update();
      }

      conversation.addParticipant(empireID1, false);
      if (empireID1 != empireID2) {
        conversation.addParticipant(empireID2, false);
      }
      return conversation;
    }

    List<ChatBlock> getBlocksForEmpire(int empireID) throws Exception {
      String sql = "SELECT * FROM chat_blocked WHERE empire_id = ?";
      try (SqlStmt stmt = prepare(sql)) {
        stmt.setInt(1, empireID);
        SqlResult result = stmt.select();

        ArrayList<ChatBlock> chatBlocks = new ArrayList<>();
        while (result.next()) {
          chatBlocks.add(new ChatBlock(result));
        }
        return chatBlocks;
      }
    }

    List<ChatBlock> getBlockingEmpires(int empireID) throws Exception {
      String sql = "SELECT * FROM chat_blocked WHERE blocked_empire_id = ?";
      try (SqlStmt stmt = prepare(sql)) {
        stmt.setInt(1, empireID);
        SqlResult result = stmt.select();

        ArrayList<ChatBlock> chatBlocks = new ArrayList<>();
        while (result.next()) {
          chatBlocks.add(new ChatBlock(result));
        }
        return chatBlocks;
      }
    }

    void addBlock(ChatBlock block) throws Exception {
      String sql = "INSERT INTO chat_blocked (empire_id, blocked_empire_id, created_date)" +
          " VALUES (?, ?, ?)";
      try (SqlStmt stmt = prepare(sql)) {
        stmt.setInt(1, block.getEmpireID());
        stmt.setInt(2, block.getBlockedEmpireID());
        stmt.setDateTime(3, block.getBlockTime());
        stmt.update();
      }
    }

    void removeBlock(ChatBlock block) throws Exception {
      String sql = "DELETE FROM chat_blocked WHERE empire_id = ? AND blocked_empire_id = ?";
      try (SqlStmt stmt = prepare(sql)) {
        stmt.setInt(1, block.getEmpireID());
        stmt.setInt(2, block.getBlockedEmpireID());
        stmt.update();
      }
    }
  }
}
