package com.example.kuroyale.model.persistence;

import java.io.Serializable;
import java.time.LocalDateTime;

public class QuestData implements Serializable {
    private static final long serialVersionUID = 1L;

    // Placeholder for Quest logic
    private LocalDateTime lastResetTime;

    public QuestData() {
        this.lastResetTime = LocalDateTime.now();
    }

    public LocalDateTime getLastResetTime() {
        return lastResetTime;
    }

    public void setLastResetTime(LocalDateTime time) {
        this.lastResetTime = time;
    }
}
