package net.rwchess.persistent.dao;


import net.rwchess.persistent.DownloadFile;

import java.util.List;

public interface DownloadFileDAO {
    public void store(DownloadFile downloadFile);

    public List<DownloadFile> getAllFiles();

}
