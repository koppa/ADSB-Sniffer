package de.graeb.adsbsniffer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.graeb.adsbsniffer.adbsreceiver.Packet;

/**
 * Class calculating statistics over the received packets
 *
 * Calculates flights, updates statistics of the database
 *
* @author markus
*/
public class RecordingStatistics {
    public static final int INTERVAL_STATUS = 1000;
    private final UpdateFlightsHandler updateFlightsHandler;

    private int packetsReceived;
    private int packetsReceivedInterval;
    private final ConcurrentHashMap<String, Entry> flights;
    private final DatabaseHelper recordingDb;

    private final int timeoutFlight;

    RecordingStatistics(int timeoutFlight, DatabaseHelper recordingDb) {
        this.recordingDb = recordingDb;
        this.timeoutFlight = timeoutFlight;

        packetsReceived = 0;

        flights = new ConcurrentHashMap<>();

        updateFlightsHandler = new UpdateFlightsHandler();
        updateFlightsHandler.sendEmptyMessageDelayed(0, INTERVAL_STATUS);
    }

    /**
     * @param packet received data packet
     * @return the id the flight
     */
    public long add(Packet packet) {
        Entry entry;
        if (!flights.containsKey(packet.icao24)) {
            // create new flight
            long id = recordingDb.storeFlight(new Date(), packet.icao24);
            entry = new Entry(id);

            flights.put(packet.icao24, entry);
        } else {
            entry = flights.get(packet.icao24);
        }
        entry.time = new Date();
        if (packet.isAdsb) {
            entry.packetsAdsb++;
        } else {
            entry.packetsModeS++;
        }

        packetsReceived++;
        return entry.id;
    }

    /**
     * Get the count of received Mode-S packets
     * @return count
     */
    public int getCountMessages() {
        return packetsReceived;
    }


    /**
     * Get the rate of all received packets (of the last second)
     * @return rate in packet/second
     */
    public double getRate() {
        return ((double) packetsReceivedInterval) / (((double) INTERVAL_STATUS) / 1000);
    }

    /**
     * Get current known icao24
     * @return list of icao24
     */
    public String[] getIcao24s() {
        Set<String> strings = flights.keySet();
        return strings.toArray(new String[strings.size()]);
    }

    public class Entry {
        final long id;

        Date time;
        int packetsAdsb = 0;
        int packetsModeS = 0;

        private Entry(long id) {
            this.id = id;
        }

        public int getPacketsModeS() {
            return packetsModeS;
        }
        public int getPacketsAdsb() {
            return packetsAdsb;
        }

        public Date getLastPacket() {
            return time;
        }
    }

    public Entry getEntry(String icao24) {
        return flights.get(icao24);
    }

    /**
     * Write the data to the database and stop handler
     */
    void writeBack () {
        // stop handler
        updateFlightsHandler.removeMessages(0);

        // write last received message to flights.last

        for (Entry entry : flights.values()) {
            recordingDb.updateFlight(entry.id, entry.time);
            recordingDb.storeFlightState(entry.id, new Date(), entry.packetsAdsb, entry.packetsModeS);
        }
    }

    public int getTimeoutFlight() {
        return timeoutFlight;
    }

    private int getPacketsReceivedLast = 0;
    /**
     * Periodically run to update the flights list
     * also updates the database
     */
    private class UpdateFlightsHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            // calculate packets in this interval
            packetsReceivedInterval = packetsReceived - getPacketsReceivedLast;
            getPacketsReceivedLast = packetsReceived;

            // store flight states
            for (Entry entry : flights.values()) {
                if (recordingDb != null) {
                    recordingDb.storeFlightState(entry.id, new Date(),
                            entry.packetsAdsb, entry.packetsModeS);
                }
            }


            // update flights
            for (Map.Entry<String, Entry> mapEntry : flights.entrySet()) {
                Date threshold = new Date();
                threshold.setTime(threshold.getTime() - timeoutFlight * 1000 * 60);
                Entry entry = mapEntry.getValue();
                if (entry.time.before(threshold)) {
                    // timeout of flight
                    if (recordingDb != null) {
                        recordingDb.updateFlight(entry.id, entry.time);
                    }

                    flights.remove(mapEntry.getKey());
                    Log.d("Recording.UpdateFlights", "removed flight " + mapEntry.getKey() + " seen " + entry.time);
                }
            }

            sendEmptyMessageDelayed(0, INTERVAL_STATUS);
        }
    }
}
