package com.example.kuroyale.protocol;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        JOIN_REQUEST,
        JOIN_ACCEPT,
        JOIN_REJECT,
        PLAYER_READY,
        START_MATCH,
        CARD_PLAYED,
        TOWER_UPDATE, // For sync checks or damage
        ELIXIR_UPDATE,
        GAME_OVER,
        DISCONNECT,
        PING // For connection quality
    }

    private MessageType type;
    private int playerId; // 1 for Host, 2 for Client usually
    private Object data; // Flexible payload
    private long timestamp;

    public Message(MessageType type, int playerId, Object data) {
        this.type = type;
        this.playerId = playerId;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public MessageType getType() {
        return type;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Object getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", playerId=" + playerId +
                ", data=" + data +
                '}';
    }
}
