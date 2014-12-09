package net.rwchess.persistent;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "MEMBERS", uniqueConstraints = @UniqueConstraint(columnNames = {"USERNAME"}))
public class Member implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long key;

    @Column(name = "USERNAME", length = 100, nullable = false)
    private String username;

    @Column(name = "PASSWORD_HASH")
    private String passwordHash;

    @Column(name = "GRUP")
    private int group;

    @Column(name = "COUNTRY", length = 100)
    private String country;

    @Column(name = "CONFIRMED")
    private Boolean confirmed;

    @Column(name = "PREFERENCE")
    private String preference;

    @Column(name = "INSIST")
    private String insist;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "RR")
    private int rr;

    public static final int ADMIN = 3;
    public static final int TD = 2;
    public static final int USER = 1;
    public static final int BAN = 0;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public String getInsist() {
        return insist;
    }

    public void setInsist(String insist) {
        this.insist = insist;
    }

    public Long getKey() {
        return key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public int getRr() {
        return rr;
    }

    public void setRr(int rr) {
        this.rr = rr;
    }
}
