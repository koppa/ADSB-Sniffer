package de.graeb.adsbsniffer.adbsreceiver;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.google.common.primitives.Bytes;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import de.graeb.adsbsniffer.adbsreceiver.exceptions.NoUsbDeviceFound;
import de.graeb.adsbsniffer.adbsreceiver.exceptions.ParseException;

/**
 * Interface for communicating with the GNS58900 Adsb receiver
 */
public class GNSReceiver {
    private final LinkedList<Byte> byteBuffer = new LinkedList<>();
    private final CDCDevice cdcDevice;

    /**
     * Connect to an gns5890 receiver over usb
     *
     * @param handler callback
     * @throws NoUsbDeviceFound no receiver was found
     */
    public GNSReceiver(Context context, final PacketReceivedHandler handler) throws NoUsbDeviceFound {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        //just get the first device
        UsbDevice usbDevice = null;
        if (deviceList != null) {
            for (UsbDevice device : deviceList.values()) {
                if (device.getProductId() == 63720 && device.getVendorId() == 1240) {
                    usbDevice = device;
                    break;
                }
            }
        }

        if (usbDevice == null) {
            throw new NoUsbDeviceFound();
        }

        cdcDevice = new CDCDevice(usbManager, usbDevice, new ReceiveHandler(handler));

        cdcDevice.configure(115200, 0, 8);
    }

    /**
     * Setup of the receiver
     *
     * @param mode      0, 2, 3, 4
     * @param heartbeat whether enable heartbeat
     * @param timestamp whether enable heartbeat
     */
    public void setUp(int mode, boolean heartbeat, boolean timestamp) {
        if (mode < 0 | mode > 4 | mode == 1) {
            throw new IllegalArgumentException("Illegal mode number: " + mode);
        }

        byte CMD_SET_MODE = 0x43;
        byte FLAG_TIMESTAMP = 0x10;
        byte FLAG_HEARTBEAT = 0x40;

        byte[] message = {CMD_SET_MODE, 0};

        message[1] = (byte) ((heartbeat ? FLAG_HEARTBEAT : 0)
                | (timestamp ? FLAG_TIMESTAMP : 0)
                | (byte) mode);
        String text = formatForTransfer(message);
        Log.d("USB", text);
        cdcDevice.send(text);

    }

    private String formatForTransfer(byte[] values) {
        StringBuilder sb = new StringBuilder("#");

        ListIterator<Byte> listIterator = Bytes.asList(values).listIterator();

        while (listIterator.hasNext()) {
            int value = listIterator.next().intValue();
            sb.append(value / 16);
            sb.append(value % 16);

            if (listIterator.hasNext()) {
                sb.append('-');
            }
        }

        sb.append("\n\r");

        return sb.toString();
    }

    /**
     * Reads the incoming data
     *
     * Splits it into messages and parses it via ADSBParser
     */
    private class ReceiveHandler implements CDCDevice.ReceivedHandler {
        private final PacketReceivedHandler handler;

        public ReceiveHandler(PacketReceivedHandler handler) {
            this.handler = handler;
        }

        @Override
        public void incoming(byte[] data) {
            for (byte b : data) {
                // strip newline and linefeed
                if (b != '\n' && b != '\r') {
                    byteBuffer.add(b);
                }
            }

            //read until ;
            notfound:
            while (true) {
                // parse lines until no more found
                ListIterator<Byte> iterator = byteBuffer.listIterator();
                do {
                    if (!iterator.hasNext()) {
                        break notfound;
                    }
                }
                while (iterator.next() != ';');

                List<Byte> sublist = byteBuffer.subList(0, iterator.previousIndex() + 1);
                byte[] line = Bytes.toArray(sublist);
                sublist.clear();

                // get timestamp
                Date timestamp = new Date();

                if (line[0] == '#') {
                    // ignore status messages
                    continue;
                }

                // parse message
                try {
                    Packet packet = ADSBParser.parse(line, timestamp);
                    if (packet != null) {
                        handler.incoming(packet);
                    }
                } catch (ParseException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    /**
     * Callback interface for receiving packets
     */
    public interface PacketReceivedHandler {
        public void incoming(Packet packet);
    }
}
