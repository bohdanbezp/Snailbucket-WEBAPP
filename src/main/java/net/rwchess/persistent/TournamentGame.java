package net.rwchess.persistent;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "TOURN_GAMES")
public class TournamentGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long key;

    @ManyToOne
    @JoinColumn(name = "WHITEPL_ID")
    private TournamentPlayer whitePlayer;

    @ManyToOne
    @JoinColumn(name = "BLACKPL_ID")
    private TournamentPlayer blackPlayer;

    @Lob
    @Column(name = "PNG")
    private String png;

    @ManyToOne
    @JoinColumn(name = "TOURNEY_ID")
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

    public boolean isInitContReminderSent() {
        return initContReminderSent;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TournamentGame that = (TournamentGame) o;

        if (round != that.round) return false;
        if (initContReminderSent != that.initContReminderSent) return false;
        if (firstReminder != that.firstReminder) return false;
        if (preGameReminderSent != that.preGameReminderSent) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (whitePlayer != null ? !whitePlayer.equals(that.whitePlayer) : that.whitePlayer != null) return false;
        if (blackPlayer != null ? !blackPlayer.equals(that.blackPlayer) : that.blackPlayer != null) return false;
        if (png != null ? !png.equals(that.png) : that.png != null) return false;
        if (tournament != null ? !tournament.equals(that.tournament) : that.tournament != null) return false;
        if (result != null ? !result.equals(that.result) : that.result != null) return false;
        if (secheduled != null ? !secheduled.equals(that.secheduled) : that.secheduled != null) return false;
        if (played != null ? !played.equals(that.played) : that.played != null) return false;
        if (whiteLastPost != null ? !whiteLastPost.equals(that.whiteLastPost) : that.whiteLastPost != null) return false;
        if (blackLastPost != null ? !blackLastPost.equals(that.blackLastPost) : that.blackLastPost != null) return false;
        return gameforumHtml != null ? gameforumHtml.equals(that.gameforumHtml) : that.gameforumHtml == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (whitePlayer != null ? whitePlayer.hashCode() : 0);
        result = 31 * result + (blackPlayer != null ? blackPlayer.hashCode() : 0);
        result = 31 * result + (png != null ? png.hashCode() : 0);
        result = 31 * result + (tournament != null ? tournament.hashCode() : 0);
        result = 31 * result + (this.result != null ? this.result.hashCode() : 0);
        result = 31 * result + (secheduled != null ? secheduled.hashCode() : 0);
        result = 31 * result + (played != null ? played.hashCode() : 0);
        result = 31 * result + round;
        result = 31 * result + (whiteLastPost != null ? whiteLastPost.hashCode() : 0);
        result = 31 * result + (blackLastPost != null ? blackLastPost.hashCode() : 0);
        result = 31 * result + (gameforumHtml != null ? gameforumHtml.hashCode() : 0);
        result = 31 * result + (initContReminderSent ? 1 : 0);
        result = 31 * result + (firstReminder ? 1 : 0);
        result = 31 * result + (preGameReminderSent ? 1 : 0);
        return result;
    }


    @Override
    public String toString() {
        String whitePlayerName = whitePlayer != null ? whitePlayer.getAssocMember().getUsername() : "";
        String blackPlayerName = blackPlayer != null ? blackPlayer.getAssocMember().getUsername() : "";
        String tournamentName = tournament != null ? tournament.getShortName() : "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String status;

        if (result != null && !result.isEmpty()) {
            status = "Result: " + result;
        } else if (played != null) {
            status = "Played on: " + dateFormat.format(played);
        } else if (secheduled != null) {
            status = "Scheduled for: " + dateFormat.format(secheduled);
        } else {
            status = "Status: Unscheduled";
        }

        return String.format("%s | Round %d: %s vs %s (%s)",
                tournament.getFullName(), round, whitePlayerName, blackPlayerName, status);
    }

    public String getGameForumString() {
        return tournament.getShortName() + ":R" + round + '_' + whitePlayer.getAssocMember().getUsername() + '-' + blackPlayer.getAssocMember().getUsername();
    }

}
