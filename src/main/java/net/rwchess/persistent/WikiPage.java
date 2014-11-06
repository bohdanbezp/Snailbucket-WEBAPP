package net.rwchess.persistent;


import net.rwchess.utils.UsefulMethods;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Stack;

@Entity
@Table(name = "WIKI_PAGES", uniqueConstraints = @UniqueConstraint(columnNames = {"NAME"}))
public class WikiPage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long key;

    @Column(name = "NAME")
    private String name;

    @Lob
    @Column(name = "RAW_TEXT")
    private String rawText;

    @Lob
    @Column(name = "HTML_TEXT")
    private String htmlText;

    @Column(name = "HISTORY", length=10000)
    private Stack<String> history;

    @Column(name = "TD_PROTECT", nullable=false)
    private Boolean tdProtected;

    public WikiPage() {
    }

    public boolean isTdProtected() {
        return tdProtected;
    }

    public void setTdProtected(boolean tdProtected) {
        this.tdProtected = tdProtected;
    }

    public String getName() {
        return name;
    }

    public String getRawText() {
        return rawText;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRawText(String rawText) {
        if (rawText == null)
            return;

        this.rawText = rawText;
        this.htmlText = UsefulMethods.getHtml(rawText);
    }

    public void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
    }


    public WikiPage(String name, String rawText, Stack<String> history) {
        this.name = name;
        this.rawText = rawText;
        this.htmlText = UsefulMethods.getHtml(rawText);
        this.history = history;
        tdProtected = false;
    }

    public Stack<String> getHistory() {
        return history;
    }

    public void setHistory(Stack<String> history) {
        this.history = history;
    }
}