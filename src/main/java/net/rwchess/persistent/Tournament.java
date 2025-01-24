package net.rwchess.persistent;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TOURNAMENTS", uniqueConstraints = @UniqueConstraint(columnNames = {"SHORT_PAGE"}))
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long key;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "SHORT_PAGE")
    private String shortName;

    @Column(name = "MAX_COUNT")
    private int maxCount;

    @Column(name = "SIGNUP_FROM")
    @Temporal(TemporalType.TIMESTAMP)
    private Date signupFrom;

    @Column(name = "SIGNUP_TO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date signupTo;

    @Column(name = "START_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Column(name = "END_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Column(name = "TIME_CONTROL", length = 50, nullable = true)
    private String timeControl;

    // Getters and Setters

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public Date getSignupFrom() {
        return signupFrom;
    }

    public void setSignupFrom(Date signupFrom) {
        this.signupFrom = signupFrom;
    }

    public Date getSignupTo() {
        return signupTo;
    }

    public void setSignupTo(Date signupTo) {
        this.signupTo = signupTo;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getTimeControl() {
        return timeControl;
    }

    public void setTimeControl(String timeControl) {
        this.timeControl = timeControl;
    }

    @Override
    public String toString() {
        return "Tournament{" +
                "key=" + key +
                ", fullName='" + fullName + '\'' +
                ", shortName='" + shortName + '\'' +
                ", maxCount=" + maxCount +
                ", signupFrom=" + signupFrom +
                ", signupTo=" + signupTo +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", timeControl='" + timeControl + '\'' +
                '}';
    }
}
