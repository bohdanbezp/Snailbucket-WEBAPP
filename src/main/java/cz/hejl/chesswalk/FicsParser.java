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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FicsParser {
    public static int NULL = 0;
    public static int ACCEPT = 1;
    public static int DECLINE = 2;

    private Matcher chat;
    private Matcher creatingMatch;
    private Matcher drawAnswer;
    private Matcher drawOffer;
    private Matcher abortAnswer;
    private Matcher abortOffer;
    private Matcher matchEnd;
    private Matcher ratingChange;
    private Matcher ratingLine;
    private Matcher seekUnavailable;
    private Matcher resumeUnavailable;
    private Matcher tooManyAdjourned;
    private Matcher tooManyAdjourned2;
    private Matcher soughtLine;
    private Matcher soughtEnd;
    private Matcher style12;

    public FicsParser() {
        chat = Pattern.compile("(says:|tells you:) (.*)").matcher("");
        creatingMatch = Pattern.compile("^Creating:.*?([0-9+]+).*?([0-9+]+)")
                .matcher("");
        drawAnswer = Pattern.compile("(accepts|declines) the draw request")
                .matcher("");
        drawOffer = Pattern.compile("\\w+ offers you a draw").matcher("");
        abortAnswer = Pattern.compile("(accepts|declines) the abort request")
                .matcher("");
        abortOffer = Pattern.compile("\\w+ would like to abort").matcher("");
        ratingLine = Pattern.compile("(Blitz|Standard|Lightning)\\s+([0-9+]+)")
                .matcher("");
        matchEnd = Pattern
                .compile(
                        "^\\{Game.*?(\\w+) vs\\. (\\w+)\\) (.*)\\} (1-0|0-1|1/2-1/2|\\*)")
                .matcher("");
        ratingChange = Pattern.compile("rating adjustment.*?(\\d+).*?(\\d+)")
                .matcher("");
        seekUnavailable = Pattern.compile("That seek is not available")
                .matcher("");
        resumeUnavailable = Pattern.compile(
                "Your opponents are either not logged in or not available.")
                .matcher("");
        tooManyAdjourned = Pattern.compile(
                "You may not start a new match if you have more than [0-9]* adjourned games.")
                .matcher("");
        tooManyAdjourned2 = Pattern.compile(
                "You have too many stored games, and may not start a new match.")
                .matcher("");
        // 1 - id
        // 2 - rating
        // 3 - username
        // 4 - time
        // 5 - inc
        // 6 - rated/unrated
        // 7 - type
        // 8
        // 9 - white/black
        // 10 - manual start
        // 11 - use formula
        soughtLine = Pattern
                .compile(
                        "^\\s*(\\d+)\\s+([0-9+]+)\\s+([a-zA-Z()]+)\\s+(\\d+)\\s+(\\d+)\\s+(rated|unrated)\\s+(lightning|blitz|standard)\\s+((\\[black\\]|\\[white\\])\\s+)?[0-9-]+\\s*(m?)(f?)")
                .matcher("");
        // 1 - number of ads
        soughtEnd = Pattern.compile("^\\s*(\\d+)\\s+ads").matcher("");
        // 1 - 8
        // 2 - 7
        // 3 - 6
        // 4 - 5
        // 5 - 4
        // 6 - 3
        // 7 - 2
        // 8 - 1
        // 9 - B/W
        // 10 - double pawn push file
        // 11 - white castle short
        // 12 - white castle long
        // 13 - black castle short
        // 14 - black castle long
        // 15 - number of moves since (50 move rule)
        // 16 - game number
        // 17 - white's name
        // 18 - black's name
        // 19 - my relation to this game (who's playing)
        // 20 - initial time
        // 21 - increment
        // 22 - white's material
        // 23 - black's material
        // 24 - white's time
        // 25 - black's time
        // 26 - # of move to be made
        // 27 - prev move
        // 28 - time of prev move
        style12 = Pattern
                .compile(
                        "^<12> ([a-zA-Z-]+) ([a-zA-Z-]+) ([a-zA-Z-]+) ([a-zA-Z-]+) ([a-zA-Z-]+) ([a-zA-Z-]+) ([a-zA-Z-]+) ([a-zA-Z-]+) (B|W) ([0-9-]+) (0|1) (0|1) (0|1) (0|1) (\\d+) (\\d+) (\\w+) (\\w+) ([0-9-]+) (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) (\\w/\\w\\d-\\w\\d=?\\w?|o-o|o-o-o|none) (\\(\\d+:\\d+\\))")
                .matcher("");
    }

    // -------------------------------------------------------------------------------------------------------

    public String parseChat(String line) {
        chat.reset(line);
        if (chat.find()) {
            return chat.group(2);
        } else
            return null;
    }

    // -------------------------------------------------------------------------------------------------------

    public int[] parseCreatingMatch(String line) {
        creatingMatch.reset(line);
        if (creatingMatch.find()) {
            int[] result = new int[2];

            if (creatingMatch.group(1).startsWith("+"))
                result[0] = -1;
            else
                result[0] = Integer.parseInt(creatingMatch.group(1));

            if (creatingMatch.group(2).startsWith("+"))
                result[1] = -1;
            else
                result[1] = Integer.parseInt(creatingMatch.group(2));

            return result;
        } else
            return null;
    }

    // -------------------------------------------------------------------------------------------------------

    public int parseDrawAnswer(String line) {
        drawAnswer.reset(line);
        if (drawAnswer.find())
            if (drawAnswer.group(1).equals("accept"))
                return ACCEPT;
            else
                return DECLINE;
        else
            return NULL;
    }

    // -------------------------------------------------------------------------------------------------------

    public boolean parseDrawOffer(String line) {
        drawOffer.reset(line);
        return drawOffer.find();
    }

    // -------------------------------------------------------------------------------------------------------

    public int parseAbortAnswer(String line) {
        abortAnswer.reset(line);
        if (abortAnswer.find())
            if (abortAnswer.group(1).equals("accept"))
                return ACCEPT;
            else
                return DECLINE;
        else
            return NULL;
    }

    // -------------------------------------------------------------------------------------------------------

    public boolean parseAbortOffer(String line) {
        abortOffer.reset(line);
        return abortOffer.find();
    }


    // -------------------------------------------------------------------------------------------------------

    public int[] parseRatingChange(String line) {
        ratingChange.reset(line);
        if (ratingChange.find())
            return new int[]{Integer.parseInt(ratingChange.group(1)),
                    Integer.parseInt(ratingChange.group(2))};
        else
            return null;
    }

    // -------------------------------------------------------------------------------------------------------

    public Rating parseRatingLine(String line) {
        ratingLine.reset(line);
        if (ratingLine.find()) {
            Rating rating = new Rating();
            rating.rating = ratingLine.group(2);
            if (ratingLine.group(1).equals("Standard"))
                rating.type = Rating.STANDARD;
            else if (ratingLine.group(1).equals("Lightning"))
                rating.type = Rating.LIGHTNING;
            else if (ratingLine.group(1).equals("Blitz"))
                rating.type = Rating.BLITZ;
            return rating;
        } else
            return null;
    }

    // -------------------------------------------------------------------------------------------------------

    public boolean parseSeekUnavailable(String line) {
        seekUnavailable.reset(line);
        return seekUnavailable.find();
    }

    // -------------------------------------------------------------------------------------------------------

    public boolean parseResumeUnavailable(String line) {
        resumeUnavailable.reset(line);
        return resumeUnavailable.find();
    }

    public boolean parseTooManyAdjourned(String line) {
        tooManyAdjourned.reset(line);
        if (tooManyAdjourned.find())
            return true;
        tooManyAdjourned2.reset(line);
        return tooManyAdjourned2.find();
    }


    // -------------------------------------------------------------------------------------------------------

    public int parseSoughtEnd(String line) {
        soughtEnd.reset(line);
        if (soughtEnd.find()) {
            return Integer.parseInt(soughtEnd.group(1));
        } else
            return -1;
    }


    // -------------------------------------------------------------------------------------------------------

    public static class Rating {
        public static final int BLITZ = 0;
        public static final int LIGHTNING = 1;
        public static final int STANDARD = 2;

        public int type;
        public String rating;
    }
}
