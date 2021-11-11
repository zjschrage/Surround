package net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class Users implements Runnable {

    public DataOutputStream serverOut;
    public DataInputStream serverIn;
    public ArrayList<Users> users;
    public int id;
    public char direction = 'w';

    public Users(DataOutputStream outIn, DataInputStream inIn, ArrayList<Users> usersIn, int idIn) {
        this.serverOut = outIn;
        this.serverIn = inIn;
        this.users = usersIn;
        this.id = idIn;
    }

    @Override
    public void run() {
        assignClientId();
        testForStart();
        try {
            setup();
        } catch (IOException e) {

        }
        try {
            Server.gameTickThread.start();
        } catch (IllegalThreadStateException e) {

        }
        updateStatus();
    }

    public void assignClientId() {
        try {
            users.get(id).serverOut.writeUTF("" + id);
        } catch (Exception e) {

        }
    }

    public void testForStart() {
        while (true) {
            try {
                String json = serverIn.readUTF();
                String jsonid = json.substring(0, 1);
                String hostOrClient = json.substring(1, 2);
                String wantsToStart = json.substring(4);
                if (wantsToStart.equals("start")) {
                    boolean first = true;
                    for (int i = 0; i < users.size(); i++) {
                        if (Server.wantsToStart[i] == true) {
                            first = false;
                            break;
                        }
                    }
                    if (first && hostOrClient.equals("h")) {
                        System.out.println("Host ready");
                        Server.wantsToStart[Integer.parseInt(jsonid)] = true;
                        break;
                    }
                    else if (!first) {
                        System.out.println("Client ready");
                        users.get(id).serverOut.writeUTF("hostready");
                        Server.wantsToStart[Integer.parseInt(jsonid)] = true;
                        break;
                    }
                }
            } catch (Exception e) {

            }
        }
        boolean everyoneReady = false;
        while (!everyoneReady) {
            System.out.print("");
            if (users.size() > 1) {
                for (int i = 0; i < users.size(); i++) {
                    if (Server.wantsToStart[i] == true) {
                        everyoneReady = true;
                    } else if (Server.wantsToStart[i] == false) {
                        everyoneReady = false;
                        break;
                    }
                }
            }
        }
        try {
            users.get(id).serverOut.writeUTF("start");
            //System.out.println("User " + id + " starting with " + users.size() + " people." );
        } 
        catch (IOException e) {

        }
    }

    public void setup() throws IOException {
        users.get(this.id).serverOut.writeInt(users.size());
    }

    public void updateStatus() {
        while (true) {
            if (users.isEmpty()) break;
            try {
                String json = serverIn.readUTF();
                char dir = json.charAt(3);
                direction = dir;
            } catch (Exception e) {

            }
        }
    }
    
}