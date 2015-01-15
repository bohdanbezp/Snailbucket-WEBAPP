/*
 * Copyright (C) 2012 Ciaran Gultnieks, ciaran@ciarang.com
 * Copyright (C) 2010 František Hejl
 *
 * This file is part of Chesswalk.
 *
 * Chesswalk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chesswalk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cz.hejl.chesswalk;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Timer;

public class ChessClient {
    public static int blackRating;
    public static int whiteRating;
    private final List<String> players;
    private int playerId = 0;

    public boolean guest;
    public String username;

    private static final int EXCEPTION = 0;
    private static final int MATCH_STARTED = 1;
    private static final int UPDATE_SOUGHT = 2;
    private static final int ONLINE_MOVE = 3;
    private static final int DRAW_OFFER = 4;
    private static final int DRAW_ANSWER = 5;
    private static final int CHAT_MESSAGE = 6;
    private static final int MATCH_END = 7;
    private static final int SEEK_UNAVAILABLE = 8;
    private static final int RESUME_UNAVAILABLE = 9;
    private static final int RATING_CHANGE = 10;
    private static final int CREATING_MATCH = 11;
    private static final int RATING = 12;
    private static final int TOO_MANY_ADJOURNED = 13;
    private static final int ABORT_OFFER = 14;
    private static final int ABORT_ANSWER = 15;
    private static ChessClient instance;

    private FicsParser ficsParser = new FicsParser();
    private InputStream in;
    private OutputStream out;
    private Socket socket;
    private Timer timer;


    private RatingListener ratingListener;

    public static interface RatingListener {
        public void ratingChecked(String player, int rating);
    }


    private Runnable listenTask = new Runnable() {
        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in));

                while (true) {
                    String line = "";

                    line = br.readLine();
                    Object o = null;
                    if ((o = (Object) ficsParser.parseRatingLine(line)) != null) {
                        FicsParser.Rating rating = (FicsParser.Rating) o;
                        if (rating.type == FicsParser.Rating.STANDARD && Integer.parseInt(rating.rating) > 1000)
                            ratingListener.ratingChecked(players.get(playerId++), Integer.parseInt(rating.rating));
                    }
                }
            } catch (IOException e) {
            }

        }
    };

    // -------------------------------------------------------------------------------------------------------

    /**
     * Releases all resources - threads, connection...
     */
    public void cancel() {
        try {
            if (timer != null)
                timer.cancel();
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
            instance = null;
        } catch (IOException e) {

        }
    }

    // -------------------------------------------------------------------------------------------------------

    public void cancelSeek() {
        write("unseek\n");
    }

    // -------------------------------------------------------------------------------------------------------

    private boolean endsWith(StringBuffer sb, String pattern) {
        if (sb.length() < pattern.length())
            return false;
        else {
            int pos1 = sb.length() - 1;
            int pos2 = pattern.length() - 1;
            boolean found = true;
            while (pos2 >= 0) {
                if (sb.charAt(pos1) != pattern.charAt(pos2)) {
                    found = false;
                    break;
                }
                pos1--;
                pos2--;
            }
            return found;
        }
    }

    // -------------------------------------------------------------------------------------------------------

    public void finger(String player) {
        write("finger " + player + '\n');
    }

    // -------------------------------------------------------------------------------------------------------

    public ChessClient(RatingListener ratingListener, List<String> players) {
        this.ratingListener = ratingListener;
        this.players = players;
    }

    // -------------------------------------------------------------------------------------------------------

    public void login(String username, String password)
            throws LoginException, IOException {

        this.username = username;
        guest = username.equals("guest");

        // try to establish connection
        socket = new Socket("freechess.org", 23);
        in = socket.getInputStream();
        out = socket.getOutputStream();

        // do the log in
        readUntil("login:");
        write(username + '\n');
        StringBuffer sb = readUntil(":");

        if (endsWith(sb, "password:")) {
            // username exists
            write(password + '\n');
            StringBuffer sb1 = readUntil(new String[]{"fics%", "login:"});
            // wrong password
            if (endsWith(sb1, "login:")) {
                cancel();
                throw new LoginException();
            } else {
                postLoginCommands();
            }

        } else if (endsWith(sb, "login:")) {
            // username too short or blank
            cancel();
            throw new LoginException();
        } else if (endsWith(sb, "\":")) {
            // username is guest or doesn't exist
            if (username.equals("guest")) {
                this.username = sb.substring(sb.length() - 11, sb.length() - 2);
                postLoginCommands();
            } else {
                cancel();
                throw new LoginException();
            }
        }

        new Thread(listenTask).start();
    }

    // -------------------------------------------------------------------------------------------------------

    public void play(String id) {
        write("play " + id + '\n');
    }

    // -------------------------------------------------------------------------------------------------------

    private void postLoginCommands() throws IOException {
        write("set shout off\n");
        write("set seek off\n");
        write("set pin off\n");
        write("set style 12\n");
        write("set autoflag 1\n");
    }

    // -------------------------------------------------------------------------------------------------------

    private StringBuffer readUntil(String[] patterns) throws IOException {
        StringBuffer sb = new StringBuffer();
        char ch = (char) in.read();
        while (true) {
            sb.append(ch);
            boolean found = false;
            for (int i = 0; i < patterns.length; i++) {
                if (endsWith(sb, patterns[i]))
                    found = true;
            }
            if (found)
                break;
            ch = (char) in.read();
        }

        return sb;
    }

    // -------------------------------------------------------------------------------------------------------


    // -------------------------------------------------------------------------------------------------------

    public void resign() {
        write("resign\n");
    }

    // -------------------------------------------------------------------------------------------------------

    public void abort() {
        write("abort\n");
    }

    // -------------------------------------------------------------------------------------------------------

    public void say(String message) {
        write("say " + message + '\n');
    }

    // -------------------------------------------------------------------------------------------------------


    // -------------------------------------------------------------------------------------------------------

    private StringBuffer readUntil(String pattern) throws IOException {
        return readUntil(new String[]{pattern});
    }

    // -------------------------------------------------------------------------------------------------------

    public void seek(int minutes, int seconds, String colorSymbol,
                     String ratedSymbol) {
        write("seek " + minutes + ' ' + seconds + ' ' + colorSymbol + ' '
                + ratedSymbol + " formula\n");
    }

    // -------------------------------------------------------------------------------------------------------

    public void resumeGame() {
        write("resume\n");
    }

    // -------------------------------------------------------------------------------------------------------


    // -------------------------------------------------------------------------------------------------------

    public void withdraw() {
        write("withdraw\n");
    }

    // -------------------------------------------------------------------------------------------------------

    public void write(String s) {
        try {
            out.write(s.getBytes());
        } catch (IOException e) {

        }
    }

    // -------------------------------------------------------------------------------------------------------


}
