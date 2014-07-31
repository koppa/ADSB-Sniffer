package de.graeb.adsbsniffer.adbsreceiver;

public enum TriState {
    TRUE(1),
    FALSE(0),
    UNKNOWN(-1);

    public final int value;

    private TriState (int value) {
        this.value = value;
    }
}
