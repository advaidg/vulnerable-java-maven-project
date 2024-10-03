package com.scalesec.vulnado;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LinkLister {

    public static List<String> getLinks(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a");
        return links.stream()
                .map(Element::absUrl)
                .collect(Collectors.toList());
    }

    public static List<String> getLinksV2(String url) throws BadRequest {
        try {
            URL aUrl = new URL(url);
            String host = aUrl.getHost();
            if (host.startsWith("172.") || host.startsWith("192.168") || host.startsWith("10.")) {
                throw new BadRequest("Use of Private IP");
            } else {
                return getLinks(url);
            }
        } catch (IOException e) {
            throw new BadRequest(e.getMessage());
        }
    }
}
