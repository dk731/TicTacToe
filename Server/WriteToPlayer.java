package com.company;

import java.net.Socket;

public class WriteToPlayer implements Runnable {


    Socket myPlayer;
    Lobby myLobby;

    public void run() {
        while(true)
        {
            try{

                Server.clients.add(Server.mainSocket.accept());

            }catch(Exception e){System.out.println(e);}
        }
    }

    public WriteToPlayer(Lobby l, Socket s)
    {
        myLobby = l;
        myPlayer = s;
    }

}