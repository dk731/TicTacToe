package com.company;

import java.io.*;
import java.net.Socket;

public class InitializeClient implements Runnable {

    public Socket myClient;
    InputStream is;
    OutputStream os;


    public String ReadStream() throws IOException {

        byte[] lenBytes = new byte[4];
        is.read(lenBytes, 0, 4);
        int len = (((lenBytes[3] & 0xff) << 24) | ((lenBytes[2] & 0xff) << 16) |
                ((lenBytes[1] & 0xff) << 8) | (lenBytes[0] & 0xff));
        byte[] receivedBytes = new byte[len];
        is.read(receivedBytes, 0, len);
        return new String(receivedBytes, 0, len);
    }

    public void WriteStream(String str) throws IOException {
        byte[] toSendBytes = str.getBytes();
        int toSendLen = toSendBytes.length;
        byte[] toSendLenBytes = new byte[4];
        toSendLenBytes[0] = (byte)(toSendLen & 0xff);
        toSendLenBytes[1] = (byte)((toSendLen >> 8) & 0xff);
        toSendLenBytes[2] = (byte)((toSendLen >> 16) & 0xff);
        toSendLenBytes[3] = (byte)((toSendLen >> 24) & 0xff);
        os.write(toSendLenBytes);
        os.write(toSendBytes);

    }

    public void run() {

        try {
            is = myClient.getInputStream();
            os = myClient.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Lobby tmpLobby;
        breakOut:
        while(true)
        {
            try{
                String inps = ReadStream();
                String substring = inps.substring(inps.length() - 3);

                boolean isName = true;
                String substring1 = inps.substring(0, inps.length() - 3);
                System.out.println(substring+'\n');
                if (substring.equals("#LN"))
                {

                    for (Lobby l : Server.lobbyList)
                    {
                        if (substring1.equals(l.id))
                        {
                            if (l.AddPlayer(myClient))
                            {
                                System.out.println("cool");
                                tmpLobby = l;
                                WriteStream("#GG");
                                break breakOut;
                            }
                        }
                    }
                }
                else if (substring.equals("#CL"))
                {

                    for (Lobby l : Server.lobbyList)
                    {
                        if (substring1.equals(l.id))
                        {
                            WriteStream("#LE");
                            isName = false;
                        }
                    }
                    if (isName)
                    {

                        tmpLobby = new Lobby(substring1, myClient);
                        Server.lobbyList.add(tmpLobby);
                        WriteStream("#GG");
                        break;
                    }
                }
            }catch(Exception e)
            {
                System.out.println(e);
                Server.clients.remove(myClient);
                return;
            }
        }

        while(true)
        {
            try {
                String input = ReadStream();

                if (input.substring(input.length()-3).equals("#RG"))
                {
                    tmpLobby.ready++;
                    if(tmpLobby.ready >= 2)
                    {
                        WriteStream("#WP");
                        Thread tmpt = new Thread(tmpLobby);
                        tmpt.start();
                    }
                    WriteStream("#WP");
                    return;
                }

            } catch (IOException e)
            {
                e.printStackTrace();
                Server.clients.remove(myClient);
                return;
            }

        }

    }

    public InitializeClient(Socket q)
    {
        myClient = q;
    }

}
