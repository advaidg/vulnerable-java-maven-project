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

class LinkLister {

    private static final Logger logger = LoggerFactory.getLogger(LinkLister.class);
    private static final Pattern PRIVATE_IP_PATTERN = Pattern.compile("^(172\\.16|192\\.168|10)\\.");
    private static final int MAX_LINKS = 1000; //Example limit. Adjust as needed

    private LinkLister() {} // Private constructor to prevent instantiation


    public static List<String> getLinks(String url) throws MalformedURLException, IOException, BadRequestException{
        validateUrl(url); //Input validation

        List<String> result = new ArrayList<>();
        Document doc = Jsoup.connect(url).maxBodySize(0).get(); //Handles large responses by setting maxBodySize to 0

        Elements links = doc.select("a");
        int count = 0;
        for (Element link : links) {
            if(count >= MAX_LINKS) {
                logger.warn("Maximum link limit reached. Truncating results.");
                break;
            }
            result.add(link.absUrl("href"));
            count++;
        }
        return result;
    }

    public static List<String> getLinksV2(String url) throws BadRequestException {
        validateUrl(url); // Input validation

        try {
            URL aUrl = new URL(url);
            String host = aUrl.getHost();
            Matcher matcher = PRIVATE_IP_PATTERN.matcher(host);
            if (matcher.find()) {
                throw new BadRequestException("Use of Private IP: " + host);
            } else {
                return getLinks(url);
            }
        } catch (MalformedURLException e) {
            logger.error("Invalid URL: {}", url, e);
            throw new BadRequestException("Invalid URL format", e);
        } catch (IOException e) {
            logger.error("Error fetching URL: {}", url, e);
            throw new BadRequestException("Error fetching URL", e);
        } catch (BadRequestException e) {
            throw e; // Re-throw BadRequestException
        }
    }

    private static void validateUrl(String url) throws MalformedURLException, BadRequestException{
        if(url == null || url.isEmpty()){
            throw new BadRequestException("URL cannot be null or empty");
        }
        new URL(url); //Throws MalformedURLException if invalid
    }
}

class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
