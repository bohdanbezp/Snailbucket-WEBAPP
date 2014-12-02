package net.rwchess.persistent.dao;

import net.rwchess.persistent.WikiPage;

import java.util.Stack;

/**
 * Created by bodia on 10/13/14.
 */
public interface WikiPageDAO {
    public WikiPage getWikiPageByName(String name);

    public void store(WikiPage wikiPage);

    public void toggleProtectTd(String pageName);

    public void updatePageWithText(String pageName, String rawText, Stack<String> newHistory);
}
