package com.scalesec.vulnado;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkLister {

    private static final Logger logger = LoggerFactory.getLogger(LinkLister.class);

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
            logger.info("Host: {}", host); // Use logger instead of System.out.println

            if (isPrivateIP(host)) {
                throw new BadRequest("Use of Private IP");
            } else {
                return getLinks(url);
            }
        } catch (MalformedURLException e) {
            throw new BadRequest("Invalid URL provided", e); // Specific exception and rethrow with original exception
        } catch (IOException e) {
            throw new BadRequest("Error fetching links", e); // Specific exception and rethrow with original exception
        }
    }

    private static boolean isPrivateIP(String host) {
        // Use a more robust approach to identify private IP addresses
        // Example: Check against CIDR ranges for private IP address blocks
        // You can use a dedicated library for IP address validation or refer to RFC1918
        // This implementation is a simplified example
        return host.startsWith("172.") || host.startsWith("192.168") || host.startsWith("10.");
    }
}
