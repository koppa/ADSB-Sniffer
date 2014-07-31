package de.graeb.adsbsniffer.adbsreceiver;

import java.util.Date;

public class Packet {
    public final String message;
    public final String icao24;
    public final Date internalTimestamp;
    public final int externalTimestamp;
    public final int format;
    public final boolean isAdsb;
    public final TriState checksumCorrect;

    protected Packet(String message, int format, String icao24, Date internalTimestamp, int externalTimestamp, boolean isAdsb, TriState checksumCorrect) {
        this.message = message;
        this.icao24 = icao24;
        this.internalTimestamp = internalTimestamp;
        this.externalTimestamp = externalTimestamp;
        this.format = format;
        this.isAdsb = isAdsb;
        this.checksumCorrect = checksumCorrect;
    }

    @Override
    public String toString() {
        return String.format("Packet{message='%s', icao24='%s', internalTimestamp=%s, externalTimestamp=%d}",
                message, icao24, internalTimestamp, externalTimestamp);
    }

}

class PacketBuilder {
    private String message;
    private String icao24;
    private Date internalTimestamp;
    private int externalTimestamp = -1;
    private int format;
    private boolean isAdsb;
    private TriState checksumCorrect = TriState.UNKNOWN;

    public PacketBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public PacketBuilder setIcao24(String icao24) {
        this.icao24 = icao24.toUpperCase();
        return this;
    }

    public PacketBuilder setInternalTimestamp(Date internalTimestamp) {
        this.internalTimestamp = internalTimestamp;
        return this;
    }

    public PacketBuilder setExternalTimestamp(int externalTimestamp) {
        this.externalTimestamp = externalTimestamp;
        return this;
    }

    public PacketBuilder setFormat(int format) {
        this.format = format;
        return this;
    }

    public PacketBuilder setChecksumCorrect(TriState checksumCorrect) {
        this.checksumCorrect = checksumCorrect;
        return this;
    }

    public PacketBuilder setAdsb() {
        this.isAdsb = true;
        return this;
    }

    public Packet createPacket() {
        return new Packet(message, format, icao24, internalTimestamp, externalTimestamp, isAdsb, checksumCorrect);
    }
}
