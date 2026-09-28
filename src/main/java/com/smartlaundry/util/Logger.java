package com.smartlaundry.util;

import com.smartlaundry.model.EventRecord;
import com.smartlaundry.service.SimulationState;

public final class Logger {
    private final SimulationState state;

    public Logger(SimulationState state) { this.state = state; }

    public void log(String threadName, String stage, String message) {
        EventRecord event = new EventRecord(formatTime(state.getElapsedSeconds()), threadName, stage, message);
        state.addEvent(event);
        System.out.println(event.toDisplayString());
    }

    private static String formatTime(double seconds) {
        int whole = (int) seconds;
        int millis = (int) Math.round((seconds - whole) * 1000.0);
        if (millis >= 1000) { whole++; millis = 0; }
        return String.format("%02d:%02d.%03d", whole / 60, whole % 60, millis);
    }
}
