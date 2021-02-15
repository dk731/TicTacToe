package com.company;

import java.net.ServerSocket;
import java.net.Socket;

public class AddClients implements Runnable {


    public void run() {
        while(true)
        {
            try{

                Socket tmp = Server.mainSocket.accept();
                Thread thread = new Thread(new InitializeClient(tmp));
                thread.start();
                Server.clients.add(tmp);

            }catch(Exception e){System.out.println(e);}
        }
    }

}
