package frc.robot;

import java.net.InetSocketAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.IOException;
import java.net.SocketException;

public class DS {
    private Thread thread;

    public void start() {
        thread = new Thread(() -> {
            DatagramSocket socket;
            try {
                socket = new DatagramSocket();
            } catch(SocketException exception) {
                exception.printStackTrace();
                return;
            }
            
            InetSocketAddress address = new InetSocketAddress("127.0.0.1", 1110); // UDP port 1110: DS -> RoboRIO 
            byte[] data = new byte[6];
            DatagramPacket packet = new DatagramPacket(data, 0, 6, address);
            
            // counters b/c for some reason ~60 packets are required for the robot to actually enable
            short packetCount = 0;
            int numPackets = 0;

            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(20);

                    packetCount++;

                    data[0] = (byte) (packetCount >> 8);
                    data[1] = (byte) packetCount;
                    data[2] = 0x01; // general tag
                    data[3] = 0; // disabled
                    data[4] = 0x10; // request
                    data[5] = 0; // sets station

                    if(numPackets >= 60) {
                        data[3] = 0x04; // enabled
                    } else {
                        numPackets++;
                    }

                    packet.setData(data);
                    socket.send(packet);
                } catch(InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } catch(IOException exception) {
                    exception.printStackTrace();
                }
            }
            socket.close();
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        if(thread == null) {
            return;
        }
        thread.interrupt();
        try {
            thread.join(1000);
        } catch(InterruptedException exception) {
            exception.printStackTrace();
        }
    }
}