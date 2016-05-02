/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ofrimann.behave;

/**
 *
 * @author ofri
 */
public class LogRecord
{
    public final static String[] LOG_HEADER = {"startTime","endTime","action","type"};
    private String action;
    private String type;
    private long startTime;
    private long endTime;

    public LogRecord(LogRecord other)
    {
        this.action = other.action;
        this.type = other.type;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
    }

    public LogRecord(String action, String type, long startTime) {
        this.action = action;
        this.type = type;
        this.startTime = startTime;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
