package com.company;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    static Server server;

    public static List<Socket> clients;
    public static ServerSocket mainSocket;
    public static List<Lobby> lobbyList;

    public static void main(String[] args) {

        server = new Server();
        clients = new ArrayList<Socket>();
        lobbyList = new ArrayList<Lobby>();

        try{
            mainSocket = new ServerSocket(11001);
        }catch(Exception e){System.out.println(e);}
        Thread addClients = new Thread(new AddClients());
        addClients.start();


    }

}
