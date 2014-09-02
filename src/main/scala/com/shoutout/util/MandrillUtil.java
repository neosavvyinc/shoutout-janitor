package com.shoutout.util;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by aparrish on 2/22/14.
 */
public class MandrillUtil {


    public static void sendMailViaMandrill( MandrillConfiguration configuration, String[] recipientEmail, Stats stats, String updateString ) throws IOException, MandrillApiError {

        MandrillApi mandrillApi  = new MandrillApi(configuration.getApiKey());

        MandrillMessage message = new MandrillMessage();
        message.setSubject("Daily Shoutout Janitorial Report - we be cleanin...");
        message.setHtml(updateString);

        message.setAutoText(true);
        message.setFromEmail(configuration.getUsername());
        message.setFromName("Team Shoutout");


        ArrayList<MandrillMessage.Recipient> recipients = new ArrayList<MandrillMessage.Recipient>();

        for (int i = 0; i < recipientEmail.length; i++) {
            String recipientEmailAddress = recipientEmail[i];
            MandrillMessage.Recipient recipient = new MandrillMessage.Recipient();
            recipient.setEmail(recipientEmailAddress);
            recipients.add(recipient);
        }

        message.setPreserveRecipients(true);
        message.setTo(recipients);

        ArrayList<String> tags = new ArrayList<String>();
        message.setTags(tags);

        try {
            MandrillMessageStatus[] messageStatusReports = mandrillApi.messages().send(message, false);
        } catch(final MandrillApiError e) {

            System.out.print(e.toString());

        }
    }

}
