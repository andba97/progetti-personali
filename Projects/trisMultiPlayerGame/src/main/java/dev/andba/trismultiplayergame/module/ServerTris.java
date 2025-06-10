package dev.andba.trismultiplayergame.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerTris implements Runnable {

    private ServerSocketChannel serverChannel;
    private int port;
    private EntityManager em;

    private Selector selector;
    private ExecutorService serverExecutor;

    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    private Map<String, SocketChannel> userChannels;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServerTris(int port, EntityManager em) {
        this.port = port;
        this.em = em;
        userChannels = new HashMap<>();
    }

    public void StartServer() {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));

            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            serverExecutor = Executors.newSingleThreadExecutor();
            serverExecutor.submit(this);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Server avviato e in ascolto sulla porta:" + ((InetSocketAddress) serverChannel.getLocalAddress()).getPort() );
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable())
                        handleAccept(key);
                    else if (key.isReadable())
                        handleRead(key);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleAccept(SelectionKey key) {
        try {
            SocketChannel client = serverChannel.accept();
            client.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            client.register(selector, SelectionKey.OP_READ, buffer);
            System.out.println("Ricevuta nuova richiesta di connessione da parte di un client");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleRead(SelectionKey key) {

        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        try {
            buffer.clear();
            int bytesRead = client.read(buffer);
            if (bytesRead == -1) {
                System.out.println("Connessione chiusa dal client");
                client.close();
                key.cancel();
                return;
            }

            buffer.flip();
            String message = new String(buffer.array(), 0, buffer.limit());
            ClientOperation operation = objectMapper.readValue(message, ClientOperation.class);

            System.out.println("Ricevuta nuova richiesta di: " + operation.getAction() );
            String response = "";
            if(operation.getAction().equals("Login"))
                response = handleLogin(operation.getUser(),client);
            else if (operation.getAction().equals("Register"))
                response = handleRegister(operation.getUser());
            else if (operation.getAction().equals("GetOnlinePlayer"))
                response = handleGetOnlinePlayer(operation.getUser().getUsername());
            else if (operation.getAction().equals("Logout")) {
                handleLogout(operation.getUser().getUsername());
                return;
            }
            else if (operation.getAction().equals("RequestGame")){
                handleRequestGame(operation.getUser(),operation.getCompetitor());
                return;
            }

            writeBuffer.clear(); // resetta buffer
            writeBuffer.put(response.getBytes());
            writeBuffer.flip();
            client.write(writeBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private String handleLogin(User user, SocketChannel  client) {
        String response;
        User temp = em.find(User.class,user.getUsername());
        if(temp==null)
            response = "ERROR1";
        else if (!temp.getPassword().equals(user.getPassword()))
            response = "ERROR2";
        else if(userChannels.containsKey(user.getUsername()))
            response = "ERROR3";
        else {
            response = "OK";
            userChannels.put(user.getUsername(), client);
            updateOnlinePlayer(user.getUsername());
        }
        return response;
    }

    private String handleRegister(User user) {
        String response;
        User temp = em.find(User.class,user.getUsername());
        if(temp==null){
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(user);
            tx.commit();
            response = "OK";
        }
        else
            response = "ERROR4";
        return response;
    }

    private String handleGetOnlinePlayer(String user) {
        ServerOperation serverOperation = new ServerOperation();
        serverOperation.setOperation("GetOnlinePlayer");
        ArrayList<String> playerList = new ArrayList<>();
        for(String temp : userChannels.keySet()) {
            if (!temp.equals(user))
                playerList.add(temp);
        }
        serverOperation.setPlayerList(playerList);
        try {
            return objectMapper.writeValueAsString(serverOperation);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateOnlinePlayer(String user) {
        for (Map.Entry<String, SocketChannel> entry : userChannels.entrySet()) {

            String username = entry.getKey();
            SocketChannel channel = entry.getValue();

            if(!username.equals(user)) {
                String message = handleGetOnlinePlayer(username);
                try {
                    writeBuffer.clear();
                    writeBuffer.put(message.getBytes());
                    writeBuffer.flip();
                    channel.write(writeBuffer);
                    System.out.println("Invio aggiornamento a " + username + ": " + message);
                } catch (IOException e) {
                    System.out.println("Errore nell'invio aggiornamento online player a " + username);
                    try {
                        channel.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    userChannels.remove(username);
                }
            }
        }
    }

    private void handleLogout(String username) {
        SocketChannel channel = userChannels.remove(username);
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Utente disconnesso: " + username);
        updateOnlinePlayer(username);
    }

    private void handleRequestGame(User user, String competitor) {

        SocketChannel channel = userChannels.get(competitor);
        ServerOperation serverOperation = new ServerOperation();
        serverOperation.setOperation("RequestGame");
        serverOperation.setChallenger(user.getUsername());
        try {
            String message = objectMapper.writeValueAsString(serverOperation);
            writeBuffer.clear(); // resetta buffer
            writeBuffer.put(message.getBytes());
            writeBuffer.flip();
            channel.write(writeBuffer);
        } catch (IOException e) {
            try {
                channel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            userChannels.remove(competitor);
        }
    }
}



