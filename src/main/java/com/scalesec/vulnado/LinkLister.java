package com.scalesec.vulnado;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LinkLister {

    private static final Logger logger = Logger.getLogger(LinkLister.class.getName());

    private LinkLister() {
        // Private constructor to prevent instantiation
    }

    public static List<String> getLinks(String url) throws IOException {
        List<String> result = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a");
        for (Element link : links) {
            result.add(link.absUrl("href"));
        }
        return result;
    }

    public static List<String> getLinksV2(String url) throws BadRequest {
        try {
            URL aUrl = new URL(url);
            String host = aUrl.getHost();
            logger.info("Host name: " + host); // Use logger instead of System.out.println
            if (host.startsWith("172.") || host.startsWith("192.168") || host.startsWith("10.")) {
                throw new BadRequest("Use of Private IP");
            } else {
                return getLinks(url);
            }
        } catch (IOException e) { // Catch specific exception
            throw new BadRequest(e.getMessage(), e); // Include original exception
        }
    }
}
