package de.graeb.adsbsniffer.adbsreceiver;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * USB Driver implementation of the CDC-ADM protocol
 */
public class CDCDevice {
    private static final boolean FORCE_CLAIM = true;

    private final UsbDeviceConnection usbConnection;

    private final UsbEndpoint endpointDataIn;
    private final UsbEndpoint endpointDataOut;
    private boolean cont = true;

    /**
     * Instantiate a connection to a cdc device
     * @param usbManager a valid instance of UsbManager
     * @param device the usb dev
     * @param receivedHandler callback for receiving
     */
    public CDCDevice(UsbManager usbManager, UsbDevice device, final ReceivedHandler receivedHandler) {
        usbConnection = usbManager.openDevice(device);
        usbConnection.claimInterface(device.getInterface(1), FORCE_CLAIM);
        usbConnection.claimInterface(device.getInterface(0), FORCE_CLAIM);

        UsbEndpoint in = null, out = null;

        for (int i = 0; i < 2; i++) {
            UsbEndpoint endpoint = device.getInterface(1).getEndpoint(i);

            if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                in = endpoint;
            } else {
                out = endpoint;
            }
        }

        endpointDataIn = in;
        endpointDataOut = out;


        Thread thread = new Thread() {
            @Override
            public void run() {
                byte[] buf = new byte[512];
                while (cont) {
                    int length = usbConnection.bulkTransfer(endpointDataIn, buf, 512, 100);
                    if (length > 0) {
                        byte[] received = new byte[length];
                        System.arraycopy(buf, 0, received, 0, length);
                        receivedHandler.incoming(received);
                        Log.d("usb", new String(buf).substring(0, length));
                    }
                }
            }
        };
        thread.setName("usb read loop");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Set the configuration on the serial line
     * @param baudrate baud in bps
     * @param bitsParity 1 or 2
     */
    public void configure(int baudrate, int bitsParity, int bitsPerSymbol) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(baudrate);
        buffer.putChar((char) bitsParity);
        buffer.putChar((char) bitsPerSymbol);


        boolean successful;

        //                                 requestType, request, value, index, buffer, length, timeout
        successful = usbConnection.controlTransfer(33, 0x20, 0, 1, buffer.array(), 8, 1000) == 8;

        //                                SET_CONTROL_LINE_STATE
        //                   requestType, request, value, index, buffer, length, timeout
        successful &= usbConnection.controlTransfer(33, 0x22, 1, 1, new byte[0], 0, 1000) == 0;

        if (!successful) {
            throw new IllegalStateException("Configure not successful");
        }
    }


    /**
     * Sends a string
     *
     * @param text will be converted via String.getBytes()
     * @return send was successful
     */
    public boolean send(String text) {
        byte[] bytes = text.getBytes();
        return send(bytes);
    }

    /**
     * Sends bytes
     *
     * @param bytes data
     * @return send was successful
     */
    public boolean send(byte[] bytes) {
        int length = usbConnection.bulkTransfer(endpointDataOut, bytes, bytes.length, 1000);
        return length == bytes.length;
    }

    public interface ReceivedHandler {
        public void incoming(byte[] data);
    }

    public void stop() {
        cont = false;
        usbConnection.close();
    }
}
