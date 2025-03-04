package com.scalesec.vulnado;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LinkLister {

    private static final Logger logger = LoggerFactory.getLogger(LinkLister.class);
    private static final Pattern URL_PATTERN = Pattern.compile("^(?:(?:https?|ftp)://)?(?:\\S+(?::\\S*)?@)?(?:(?:[a-z\\u00a1-\\uffff0-9](?:[a-z\\u00a1-\\uffff0-9-]{0,61}[a-z\\u00a1-\\uffff0-9])?\\.)+(?:[a-z\\u00a1-\\uffff]{2,})|localhost|(?:\\d{1,3}\\.){3}\\d{1,3})(?::\\d+)?(?:/[^\\s]*)?$");
    private static final List<String> PRIVATE_IP_PREFIXES = List.of("172.", "192.168", "10.");

    private LinkLister() {}

    public static List<String> getLinks(String url) throws IOException {
        List<String> result = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a");
        for (Element link : links) {
            result.add(link.absUrl("href"));
        }
        return result;
    }

    public static List<String> getLinksV2(String url) throws BadRequest, MalformedURLException {
        //Input validation
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        Matcher matcher = URL_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        URL aUrl = new URL(url);
        String host = aUrl.getHost();

        logger.info("Processing URL: {}", url);


        if (isPrivateIp(host)) {
            throw new BadRequest("Use of Private IP: " + host);
        } else {
            return getLinks(url);
        }
    }

    private static boolean isPrivateIp(String host) {
        for (String prefix : PRIVATE_IP_PREFIXES) {
            if (host.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }


    public static class BadRequest extends RuntimeException {
        public BadRequest(String message) {
            super(message);
        }
    }
}
