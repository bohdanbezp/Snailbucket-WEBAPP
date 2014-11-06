package net.rwchess.persistent;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TOURN_GAMES")
public class TournamentGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long key;

    @ManyToOne
    @JoinColumn(name="WHITEPL_ID")
    private TournamentPlayer whitePlayer;

    @ManyToOne
    @JoinColumn(name="BLACKPL_ID")
    private TournamentPlayer blackPlayer;

    @Lob
    @Column(name = "PNG")
    private String png;

    @ManyToOne
    @JoinColumn(name="TOURNEY_ID")
    private Tournament tournament;

    @Column(name = "RESULT")
    private String result;

    @Column(name = "SHEDULED_DATE")
    private Date secheduled;

    @Column(name = "PLAYED_DATE")
    private Date played;

    @Column(name = "ROUND")
    private int round;

    @Column(name = "W_LAST_POST")
    private Date whiteLastPost;

    @Column(name = "B_LAST_POST")
    private Date blackLastPost;

    @Lob
    @Column(name = "GAMEFORUM_HTML")
    private String gameforumHtml;

    @Column(name = "INIT_REM_SENT")
    private boolean initContReminderSent;

    @Column(name = "FIRST_REM_SENT")
    private boolean firstReminder;

    @Column(name = "PREGAME_REM_SENT")
    private boolean preGameReminderSent;

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public TournamentPlayer getWhitePlayer() {
        return whitePlayer;
    }

    public void setWhitePlayer(TournamentPlayer whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public TournamentPlayer getBlackPlayer() {
        return blackPlayer;
    }

    public void setBlackPlayer(TournamentPlayer blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public String getPng() {
        return png;
    }

    public void setPng(String png) {
        this.png = png;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Date getSecheduled() {
        return secheduled;
    }

    public void setSecheduled(Date secheduled) {
        this.secheduled = secheduled;
    }

    public Date getPlayed() {
        return played;
    }

    public void setPlayed(Date played) {
        this.played = played;
    }

    public String getGameforumHtml() {
        return gameforumHtml;
    }

    public void setGameforumHtml(String gameforumHtml) {
        this.gameforumHtml = gameforumHtml;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public Date getWhiteLastPost() {
        return whiteLastPost;
    }

    public void setWhiteLastPost(Date whiteLastPost) {
        this.whiteLastPost = whiteLastPost;
    }

    public Date getBlackLastPost() {
        return blackLastPost;
    }

    public void setBlackLastPost(Date blackLastPost) {
        this.blackLastPost = blackLastPost;
    }

    public boolean isInitContReminderSent() {       return initContReminderSent;
    }

    public void setInitContReminderSent(boolean initContReminderSent) {
        this.initContReminderSent = initContReminderSent;
    }

    public boolean isFirstReminder() {
        return firstReminder;
    }

    public void setFirstReminder(boolean firstReminder) {
        this.firstReminder = firstReminder;
    }

    public boolean isPreGameReminderSent() {
        return preGameReminderSent;
    }

    public void setPreGameReminderSent(boolean preGameReminderSent) {
        this.preGameReminderSent = preGameReminderSent;
    }

    public boolean equals(Object o) {
        if (o == null && !(o instanceof TournamentGame))
            return false;

        TournamentGame game = (TournamentGame) o;

        return key.equals(game.key) && (result != null ? result.equals(game.result) : result == game.result);
    }

    public int hashCode() {
        return key.hashCode() + (result != null ? result.hashCode() : 0) +
                (secheduled != null ? secheduled.hashCode() : 0);
    }

    public String toString() {
        return tournament.getShortName()+":R"+ round + '_' + whitePlayer.getAssocMember().getUsername()+ '-' + blackPlayer.getAssocMember().getUsername();
    }
}
