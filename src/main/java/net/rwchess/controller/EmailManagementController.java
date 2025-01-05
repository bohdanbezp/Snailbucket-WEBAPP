package net.rwchess.controller;

import info.bliki.commons.validator.routines.EmailValidator;
import net.rwchess.utils.Mailer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/email")
public class EmailManagementController {

    private final Mailer mailService;

    public EmailManagementController(Mailer mailService) {
        this.mailService = mailService;
    }

    @RequestMapping(value = "/send", method = RequestMethod.GET)
    public String sendEmailForm(ModelMap model) {
        model.addAttribute("title", "Send Email from tds@snailbucket.org");
        return "send-email";
    }

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public String sendEmail(@RequestParam("recipients") String recipients,
                            @RequestParam("subject") String subject,
                            @RequestParam("message") String message,
                            ModelMap model) {
        String fromEmail = "tds@snailbucket.org";
        String[] recipientList = recipients.split(",");
        List<String> invalidEmails = new ArrayList<>();

        EmailValidator validator = EmailValidator.getInstance();

        for (String recipient : recipientList) {
            recipient = recipient.trim();
            if (!validator.isValid(recipient)) {
                invalidEmails.add(recipient);
            }
        }

        if (!invalidEmails.isEmpty()) {
            model.addAttribute("title", "Error");
            model.addAttribute("error", "The following email addresses are invalid: " + String.join(", ", invalidEmails));
            return "error";
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            for (String recipient : recipientList) {
                try {
                    mailService.sendEmail(fromEmail, subject, message, recipient.trim());
                    Thread.sleep(1000); // Pause for 1 second between emails
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Handle thread interruption
                    // Log or handle the interruption as needed
                }
            }
        });

        executorService.shutdown();

        model.addAttribute("title", "Emails Sent");
        model.addAttribute("error", "Emails have been successfully sent to all recipients.");
        return "error";
    }

}
