package net.rwchess.persistent;

import javax.persistence.*;

@Entity
@Table(name = "FILES", uniqueConstraints = @UniqueConstraint(columnNames = {"NAME"}))
public class DownloadFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long key;

    @Column(name = "NAME", length = 100, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "CREATOR_ID")
    private Member creator;

    @Column(name = "DESCRIPTION")
    private String description;

    public Long getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Member getCreator() {
        return creator;
    }

    public void setCreator(Member creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
