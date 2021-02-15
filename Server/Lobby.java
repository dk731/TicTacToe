package com.company;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class Lobby implements Runnable {

    public String id;
    public Socket player1, player2;
    public boolean playerTurn = true; // true - first
    public int[] field = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    public boolean firtsplayer;
    public int ready = 0;


    public boolean AddPlayer(Socket q)
    {
        if (player1 == null)
        {
            player1 = q;
        }
        else if (player2 == null)
        {
            player2 = q;
        }
        else
        {
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        StartGame();
    }

    public void StartGame()
    {
        Random rand = new Random();


        OutputStream os1 = null;
        OutputStream os2 = null;
        InputStream is1 = null;
        InputStream is2 = null;
        try {
            is2 = player2.getInputStream();
            is1 = player1.getInputStream();
            os2 = player2.getOutputStream();
            os1 = player1.getOutputStream();


            WriteStream("#PG", os1, player1);
            WriteStream("#PG", os2, player2);

        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true)
        {
            field = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
            try {



                if (rand.nextDouble() < 0.5)
                {
                    System.out.println("1 - X, 2 - O");
                    WriteStream("1#YT", os1, player1);
                    WriteStream("2#YT", os2, player2);
                    firtsplayer = true;
                    playerTurn = true;
                }
                else {
                    System.out.println("1 - O, 2 - X");
                    WriteStream("2#YT", os1, player1);
                    WriteStream("1#YT", os2, player2);
                    firtsplayer = false;
                    playerTurn = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String input;
            while(true)
            {

                try{
                    while(true)
                    {

                        if (playerTurn)
                        {
                            WriteStream("#WR", os1, player1);
                            input = ReadStream(is1);
                        }
                        else
                        {
                            WriteStream("#WR", os2, player2);
                            input = ReadStream(is2);
                        }
                        if (input.substring(input.length()-3).equals("#MM"))
                        {
                            int i = Character.getNumericValue(input.charAt(0));
                            if (field[i] == 0)
                            {

                                if (playerTurn)
                                {
                                    WriteStream("#GM", os1, player1);
                                    WriteStream(i + "#UF", os2, player2);
                                    if (firtsplayer)
                                    {
                                        field[i] = 1;
                                    }
                                    else
                                    {
                                        field[i] = 2;
                                    }
                                }
                                else {
                                    WriteStream("#GM", os2, player2);
                                    WriteStream(i + "#UF", os1, player1);
                                    if (firtsplayer)
                                    {
                                        field[i] = 2;
                                    }
                                    else
                                    {
                                        field[i] = 1;
                                    }
                                }
                                playerTurn = !playerTurn;
                                break;
                            }
                        }
                    }
                    if (CheckForWin())
                    {
                        Thread.sleep(3000);
                        System.out.println("Game Ended");
                        if (playerTurn)
                        {
                            WriteStream("0#GF", os1, player1);
                            WriteStream("1#GF", os2, player2);
                        }
                        else
                        {
                            WriteStream("1#GF", os1, player1);
                            WriteStream("0#GF", os2, player2);
                        }
                        Thread.sleep(3000);
                        while (true)
                        {
                            String input1 = ReadStream(is1);
                            if (input1.contains("#FF"))
                            {
                                break;
                            }
                        }
                        while (true)
                        {
                            String input2 = ReadStream(is2);
                            if (input2.contains("#FF"))
                            {
                                break;
                            }
                        }

                        break;
                    }
                    else if (CheckTie())
                    {
                        Thread.sleep(3000);
                        WriteStream("#TG", os1, player1);
                        WriteStream("#TG", os2, player2);
                        Thread.sleep(1000);
                        while (true)
                        {
                            String input1 = ReadStream(is1);
                            if (input1.contains("#FF"))
                            {
                                break;
                            }
                        }
                        while (true)
                        {
                            String input2 = ReadStream(is2);
                            if (input2.contains("#FF"))
                            {
                                break;
                            }
                        }

                        break;


                    }




                }catch(Exception e){
                    System.out.println(e);
                    Server.lobbyList.remove(this);
                    return;
                }

            }
        }
    }

    private boolean CheckTie()
    {

        for(int a : field)
        {
            if (a == 0)
            {
                return false;
            }
        }
        return true;

    }

    private boolean CheckForWin()
    {
        for (int i = 0; i < 3; i++)
        {
            if(field[i*3] == field[i*3 + 1] && field[i*3] == field[i*3 + 2] && field[i*3] != 0)
            {
                return true;
            }
            if(field[i] == field[i + 3] && field[i] == field[i + 6] && field[i] != 0) {
                return true;
            }
        }
        if (field[0] == field[4] && field[0] == field[8] && field[0] != 0)
        {
            return true;
        }
        if (field[2] == field[4] && field[2] == field[6] && field[2] != 0)
        {
            return true;
        }
        return false;
    }

    public Lobby(String ids, Socket p)
    {
        id = ids;
        player1 = p;
    }

    public String ReadStream(InputStream is) throws IOException {

        byte[] lenBytes = new byte[4];
        is.read(lenBytes, 0, 4);
        int len = (((lenBytes[3] & 0xff) << 24) | ((lenBytes[2] & 0xff) << 16) |
                ((lenBytes[1] & 0xff) << 8) | (lenBytes[0] & 0xff));
        byte[] receivedBytes = new byte[len];
        is.read(receivedBytes, 0, len);
        return new String(receivedBytes, 0, len);
    }

    public void WriteStream(String str, OutputStream os, Socket myClient) throws IOException {
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


}
