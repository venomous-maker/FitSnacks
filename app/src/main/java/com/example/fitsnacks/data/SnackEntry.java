// Name : pooja bandari
// Course: open - source intelligent device (ITMD- 555)
// Project : FitSnacks
package com.example.fitsnacks.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "snacks")
public class SnackEntry {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public int calories;
    public String portion;
    public String date; // ISO8601

    public SnackEntry() { }

    public SnackEntry(String name, int calories, String portion, String date) {
        this.name = name;
        this.calories = calories;
        this.portion = portion;
        this.date = date;
    }
}