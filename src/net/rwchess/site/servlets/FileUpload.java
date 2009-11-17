package net.rwchess.site.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.rwchess.site.ErrorsManager;
import net.rwchess.site.data.DAO;
import net.rwchess.site.data.File;
import net.rwchess.site.data.UploadedFile;
import net.rwchess.site.utils.UsefulMethods;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.google.appengine.api.datastore.Blob;

public class FileUpload extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(500000);
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(500000);
		try {
			String description = null;
			File fileBlob = null;
			
			FileItemIterator iterator = upload.getItemIterator(req);
			while (iterator.hasNext()) {
				FileItemStream item = iterator.next();
				InputStream in = item.openStream();
				
				try {
					if (item.isFormField()
							&& item.getFieldName().equals("description")) {
						description = IOUtils.toString(in);
					} 
					else if (!item.isFormField()) {
						fileBlob = new File(item.getName(),
								new Blob(IOUtils.toByteArray(in)));
					}
				} 
				finally {
					IOUtils.closeQuietly(in);
				}
			}

			if (description != null && fileBlob != null) {
				UploadedFile file = new UploadedFile(UsefulMethods
						.getUsername(req.getSession()), new Date(),
						description, fileBlob.getFileName());
				DAO.get().getPersistenceManager().makePersistent(file);
				DAO.get().getPersistenceManager().makePersistent(fileBlob);
			}
			else
				throw new FileUploadException("Something went wrong");
			
			res.sendRedirect("/");
		} 
		catch (FileUploadException e) {
			ErrorsManager.display(e.getMessage(), res, req);
			e.printStackTrace();
		}
	}
}
