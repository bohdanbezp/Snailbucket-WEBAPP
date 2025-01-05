package net.rwchess.persistent;

import javax.persistence.*;
import java.util.Objects;

/**
 * Entity representing a BYE for a specific tournament, round, and player.
 */
@Entity
@Table(name = "TOURN_BYES")
public class TournBye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TOURNEY_ID", nullable = false)
    private Tournament tournament;

    @Column(name = "ROUND", nullable = false)
    private int round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLAYER_ID", nullable = false)
    private TournamentPlayer player;

    @Column(name = "BYE_TYPE", nullable = false, length = 50)
    private String byeType;

    public TournBye() {
        // Default constructor for JPA
    }

    public TournBye(Tournament tournament, int round, TournamentPlayer player, String byeType) {
        this.tournament = tournament;
        this.round = round;
        this.player = player;
        this.byeType = byeType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public TournamentPlayer getPlayer() {
        return player;
    }

    public void setPlayer(TournamentPlayer player) {
        this.player = player;
    }

    public String getByeType() {
        return byeType;
    }

    public void setByeType(String byeType) {
        this.byeType = byeType;
    }

    // toString method
    @Override
    public String toString() {
        String playerName = (player != null && player.getAssocMember() != null) ? player.getAssocMember().getUsername() : "Unknown Player";
        return playerName + " - " + byeType.toUpperCase() + " bye";
    }

    // equals method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TournBye tournBye = (TournBye) o;
        return round == tournBye.round &&
                Objects.equals(id, tournBye.id) &&
                Objects.equals(tournament, tournBye.tournament) &&
                Objects.equals(player, tournBye.player) &&
                Objects.equals(byeType, tournBye.byeType);
    }

    // hashCode method
    @Override
    public int hashCode() {
        return Objects.hash(id, tournament, round, player, byeType);
    }
}
