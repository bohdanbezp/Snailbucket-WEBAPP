package net.rwchess.persistent;


import javax.persistence.*;

@Entity
@Table(name = "TOURN_PLAYERS")
public class TournamentPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long key;

    @ManyToOne
    @JoinColumn(name="MEMBER_ID")
    private Member assocMember;

    @Column(name = "FIXED_RATING")
    private int fixedRating;

    @ManyToOne
    @JoinColumn(name="TOURNEY_ID")
    private Tournament tournament;

    @Column(name = "EMAIL_FORUM")
    private boolean emailForum;

    @Column(name = "TOURNEY_GROUP")
    private String tourneyGroup;

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public Member getAssocMember() {
        return assocMember;
    }

    public void setAssocMember(Member assocMember) {
        this.assocMember = assocMember;
    }

    public int getFixedRating() {
        return fixedRating;
    }

    public void setFixedRating(int fixedRating) {
        this.fixedRating = fixedRating;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public boolean isEmailForum() {
        return emailForum;
    }

    public void setEmailForum(boolean emailForum) {
        this.emailForum = emailForum;
    }

    public String getTourneyGroup() {
        return tourneyGroup;
    }

    public void setTourneyGroup(String tourneyGroup) {
        this.tourneyGroup = tourneyGroup;
    }


    public boolean equals(Object o) {
        if (o == null && !(o instanceof TournamentPlayer))
            return false;

        TournamentPlayer player = (TournamentPlayer) o;

        return key.equals(player.key) && fixedRating == player.fixedRating;
    }

    public int hashCode() {
        return key.hashCode() + fixedRating;
    }
}
