package net.rwchess.utils;

import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by bodia on 10/17/14.
 */
public class SendgridMailer implements Mailer {

    static Logger log = Logger.getLogger(SendgridMailer.class.getName());

    private ExecutorService taskExecutor = Executors.newFixedThreadPool(4);

    @Override
    public void sendEmail(String from, String subject, String contents, String to) {
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //        HttpClient httpclient = new DefaultHttpClient();
//        HttpPost get = new HttpPost("https://sendgrid.com/api/mail.send.json");
//        log.info("Preparing email to " + to);
//        try {
//            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
//            nameValuePairs.add(new BasicNameValuePair("to", to));
//            nameValuePairs.add(new BasicNameValuePair("toname", "Member"));
//            nameValuePairs.add(new BasicNameValuePair("subject", subject));
//            nameValuePairs.add(new BasicNameValuePair("text", contents));
//            nameValuePairs.add(new BasicNameValuePair("from", from));
//            nameValuePairs.add(new BasicNameValuePair("api_user", "bvk256"));
//            nameValuePairs.add(new BasicNameValuePair("api_key", "bodiaissendgrid"));
//            get.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//            HttpResponse r = httpclient.execute(get);
//            log.info("Response " + r.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
            }
        });

    }

}
