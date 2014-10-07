package de.graeb.adsbsniffer.adbsreceiver;

import android.util.Log;
import android.widget.Toast;

import com.google.common.primitives.Booleans;

import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import de.graeb.adsbsniffer.adbsreceiver.exceptions.ParseException;

public class ADSBParser {
    /**
     * Parses one line of a packet, should end with ';' and begin with '*' or '@'
     *
     * @param line      a received line
     * @param timestamp Timestamp when the packet was received
     * @return the translated packet
     *         null if not a valid packet
     */
    static Packet parse(byte[] line, Date timestamp) throws ParseException {

        // check if ends with ';'
        if (line[line.length - 1] != ';') {
            throw new ParseException("not ending with ';' string: " + new String(line));
        }
        // strip ';'
        String string = new String(line).substring(0, line.length - 1);

        PacketBuilder packetBuilder = new PacketBuilder()
                .setInternalTimestamp(timestamp);

        if (line[0] == '*') {
            // remove *, first character
            string = string.substring(1);

        } else if (line[0] == '@') {
            // the first 12 characters are the timestamp
            int[] externalTimestamp = hexStringToIntArray(string.substring(1, 13));

            int value = 0;
            for (int b : externalTimestamp) {
                value = 256 * value + b;
            }
            packetBuilder.setExternalTimestamp(value);

            string = string.substring(13);
        } else {
            // throw new ParseException("Illegal start character string=" + new String(line));
            Log.e("ADSBParser", "Line started with wrong character!!");
            return null;
        }

        // check if received message consists of '0's
        zeros: {
            for (int i = 0; i < string.length(); i++) {
                if (string.charAt(i) != '0') {
                    break zeros;
                }
            }
            Log.e("ADSBParser", "received 0 message");
            return null;
        }

        // parse message
        byte[] data = hexStringToByteArray(string);
        int format = (data[0] & 0xF8) >> 3;

        packetBuilder.setFormat(format)
                .setMessage(string);
        Log.d("ADSBParser", "message received: " + string);

        byte[] crc = calculateCRC(data);


        if (format == 17 || format == 18 || format == 11) {
            //Extended squitter (17, 18) or All-call reply (11)
            // contains PI field, the last 4bits are the interrogator code
            String icao24 = string.substring(2, 8);
            packetBuilder.setIcao24(icao24);

            // check crc
            if ((crc[0] == 0) && (crc[1] == 0) && ((crc[2] & 0xF0) == 0)) {
                packetBuilder.setChecksumCorrect(TriState.TRUE);
            } else {
                Log.e("ADSBParser", "Adsb message with invalid crc: " + Arrays.toString(crc));
                packetBuilder.setChecksumCorrect(TriState.FALSE);
            }

            if (format != 11) {
                packetBuilder.setAdsb();
            }
        } else {
            // PA Field used
            String icao24 = String.valueOf(Hex.encodeHex(crc));
            Log.d("ADSBParser", String.format("S-mode message format Nr: %d, message %s crc: %s",
                    format, new String(line), icao24));
            packetBuilder.setIcao24(icao24);
        }
        return packetBuilder.createPacket();
    }

    private static int[] hexStringToIntArray(String s) {
        if (s.length() % 2 > 0) {
            throw new IllegalArgumentException(String.format("length not multiple of 2, string: '%s'", s));
        }

        int len = s.length() / 2;
        int[] data = new int[len];

        for (int i = 0; i < s.length() - 1; i += 2) {
            data[i / 2] =  (Character.digit(s.charAt(i), 16) << 4) +
                    Character.digit(s.charAt(i + 1), 16);
        }

        return data;
    }

    private static byte[] hexStringToByteArray(String s) {
        int[] ints = hexStringToIntArray(s);
        byte[] bytes = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            bytes[i] = (byte) ints[i];

        }

        return bytes;
    }

    private static final boolean[] CRC_POLYNOM = {true, true, true, true, true, true, true, true,
            true, true, true, true, true, false, true, false,
            false, false, false, false, false, true, false, false, true};

    private static byte[] calculateCRC(byte[] input) {
        List<Boolean> datastream = Booleans.asList(byteToBitVector(input));

        for (int i = 0; i < datastream.size() - (CRC_POLYNOM.length - 1); i++) {
            if (datastream.get(i)) {
                for (int j = 0; j < CRC_POLYNOM.length; j++) {
                    datastream.set(i + j, CRC_POLYNOM[j] ^ datastream.get(i + j));
                }
            }
        }

        return bitVectorToByte(datastream.subList(datastream.size() - 24, datastream.size()));
    }


    private static boolean[] byteToBitVector(byte[] data) {
        boolean[] out = new boolean[data.length * 8];
        for (int i = 0; i < data.length * 8; i++) {
            out[i] = ((data[i / 8] >> (7 - i % 8)) & 1) > 0;
        }
        return out;
    }

    private static byte[] bitVectorToByte(List<Boolean> data) {
        byte[] out = new byte[data.size() / 8];
        Arrays.fill(out, (byte) 0);
        ListIterator<Boolean> iterator = data.listIterator();
        while (iterator.hasNext()) {
            Boolean value = iterator.next();
            if (value) {
                out[iterator.previousIndex() / 8] |= (1 << (7 - (iterator.previousIndex() % 8)));
            }
        }
        return out;
    }
}
