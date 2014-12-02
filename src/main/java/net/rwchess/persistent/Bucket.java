package net.rwchess.persistent;


import java.util.List;

public class Bucket {
    private List<TournamentPlayer> playerList;
    private String name;
    private String td;

    public List<TournamentPlayer> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<TournamentPlayer> playerList) {
        this.playerList = playerList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTd() {
        return td;
    }

    public void setTd(String td) {
        this.td = td;
    }
}
