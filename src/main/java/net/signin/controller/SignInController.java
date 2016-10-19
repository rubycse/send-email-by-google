package net.signin.controller;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import net.signin.service.EmailSender;
import net.signin.utils.PropertyReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author lutfun
 * @since 3/28/16
 */

@Controller
public class SignInController {

    private static HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    GoogleClientSecrets clientSecrets;
    GoogleAuthorizationCodeFlow flow;
    Credential credential;
    Gmail service;

    private String clientId = "146785461871-rk9tjoglfknju4nl1dqlmruriuat5346.apps.googleusercontent.com";
    private String clientSecret = "hhZwD-wVpdoHli-ehcEWZg7G";
    private String redirectURI = "http://gmail-dphe.rhcloud.com/sign-in-by-google/signin/sendMail";
    private List<String> scopes = Arrays.asList("https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/gmail.send");

    @Autowired
    private EmailSender emailSender;

    @RequestMapping(value = "/connect", method = RequestMethod.GET)
    public RedirectView googleConnectionStatus(HttpServletRequest request) throws Exception {
        return new RedirectView(authorize());
    }

    private String authorize() throws Exception {
        AuthorizationCodeRequestUrl authorizationUrl;
        if (flow == null) {
            GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
            web.setClientId(clientId);
            web.setClientSecret(clientSecret);
            clientSecrets = new GoogleClientSecrets().setWeb(web);
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
                    scopes).build();
        }
        authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectURI);
        return authorizationUrl.build();
    }

    @RequestMapping(path = "/signin", method = RequestMethod.GET)
    public String signin(ModelMap model) {
        model.put("gapiClientId", PropertyReader.getProperty("gapi.clientId"));
        model.put("gapiClientSecret", PropertyReader.getProperty("gapi.clientSecret"));
        return "signin/signin";
    }

    @RequestMapping(path = "/sendMail", method = RequestMethod.GET, params = "code")
    public String validateSignIn(@RequestParam String code) throws IOException {
        if (code == null || code.trim().equals("")) {
            return "ERROR";
        }

        TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();
        credential = flow.createAndStoreCredential(response, "me");

        service = new Gmail.Builder(new NetHttpTransport(),
                new JacksonFactory(), credential)
                .setApplicationName("Gmail Send Sample").build();

        return "signin/compose";
    }

    @RequestMapping(path = "/sendMail", method = RequestMethod.POST)
    public String sendEmail(@RequestParam String to, @RequestParam String subject,
                            @RequestParam String body) throws IOException {
        try {
            emailSender.sendMessage(service, "me", createEmail(to, "me", subject, body));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return "signin/mailSentSuccess";
    }

    public static MimeMessage createEmail(String to, String from, String subject,
                                          String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }
}
