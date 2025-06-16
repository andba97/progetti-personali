package dev.andba.trismultiplayergame.module;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Client {
    private final SocketChannel socket;
    private final Selector selector;
    private final ByteBuffer bufferRead = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer;
    private MessageListener messageListener;
    private volatile boolean running = true;
    private final Thread selectorThread;

    private User connectedUser;

    public Client() {
        try {
            socket = SocketChannel.open();
            socket.configureBlocking(false);
            socket.connect(new InetSocketAddress("localhost", 50));

            selector = Selector.open();
            socket.register(selector, SelectionKey.OP_CONNECT );
            selectorThread = new Thread(this::runSelectorLoop);
            selectorThread.start();
        } catch (IOException e) {
            throw new RuntimeException("Errore di connessione al server", e);
        }
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void setConnectedUser(User user) {
        this.connectedUser = user;
    }
    public User getUser() {
        return this.connectedUser;
    }

    private void runSelectorLoop() {
        try {
            while (running) {
                selector.select();

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isConnectable()) {
                        handleConnect(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nel loop del client: " + e.getMessage());
        }
    }

    public interface MessageListener {
        void onMessage(String message);
    }

    private void handleConnect(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        if (sc.isConnectionPending()) {
            sc.finishConnect();
        }
        System.out.println("Connesso al server!");
        sc.register(selector, SelectionKey.OP_READ);
    }


    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        bufferRead.clear();
        int bytesRead = sc.read(bufferRead);
        if (bytesRead == -1) {
            sc.close();
            System.out.println("Server disconnesso.");
            return;
        }
        bufferRead.flip();
        String response = new String(bufferRead.array(), 0, bufferRead.limit());
        if (messageListener != null) {
            messageListener.onMessage(response);
        }
    }

    public void send(String message){
        try {
            writeBuffer = ByteBuffer.wrap(message.getBytes());
            socket.write(writeBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        running = false;
        try {
            selector.wakeup(); // sveglia il select() per uscire dal blocco
            selectorThread.join(); // aspetta che termini
            socket.close(); // chiudi socket
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}

