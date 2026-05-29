package com.matcha.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

/**
 * MatchaLog — Data model representing a single daily matcha intake record.
 * Uses JavaFX property wrappers for seamless TableView data binding.
 * Maps directly to the 'matcha_logs' table in the MySQL database.
 */
public class MatchaLog {

    private final IntegerProperty id;
    private final IntegerProperty userId;
    private final IntegerProperty drinkCount;
    private final ObjectProperty<LocalDate> logDate;

    public MatchaLog(int id, int userId, int drinkCount, LocalDate logDate) {
        this.id         = new SimpleIntegerProperty(id);
        this.userId     = new SimpleIntegerProperty(userId);
        this.drinkCount = new SimpleIntegerProperty(drinkCount);
        this.logDate    = new SimpleObjectProperty<>(logDate);
    }

    // --- id ---
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    // --- userId ---
    public int getUserId() { return userId.get(); }
    public IntegerProperty userIdProperty() { return userId; }

    // --- drinkCount ---
    public int getDrinkCount() { return drinkCount.get(); }
    public void setDrinkCount(int value) { drinkCount.set(value); }
    public IntegerProperty drinkCountProperty() { return drinkCount; }

    // --- logDate ---
    public LocalDate getLogDate() { return logDate.get(); }
    public void setLogDate(LocalDate value) { logDate.set(value); }
    public ObjectProperty<LocalDate> logDateProperty() { return logDate; }

    @Override
    public String toString() {
        return "MatchaLog{id=" + getId() + ", userId=" + getUserId() +
               ", drinkCount=" + getDrinkCount() + ", logDate=" + getLogDate() + "}";
    }
}
