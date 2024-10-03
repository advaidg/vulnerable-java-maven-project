package com.scalesec.vulnado;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.assertThrows;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LinkListerTest {

    @Test
    void testGetLinks() throws IOException {
        String url = "https://www.example.com";
        String link1 = "https://www.example.com/page1";
        String link2 = "https://www.example.com/page2";

        Document mockDoc = Mockito.mock(Document.class);
        Elements mockLinks = Mockito.mock(Elements.class);
        when(mockDoc.select("a")).thenReturn(mockLinks);
        when(mockLinks.stream()).thenReturn(List.of(
                Mockito.mock(Element.class),
                Mockito.mock(Element.class)
        ).stream());
        when(mockLinks.get(0).absUrl()).thenReturn(link1);
        when(mockLinks.get(1).absUrl()).thenReturn(link2);

        when(Jsoup.connect(url)).thenReturn(Mockito.mock(org.jsoup.Connection.class));
        when(Jsoup.connect(url).get()).thenReturn(mockDoc);

        List<String> expectedLinks = new ArrayList<>(List.of(link1, link2));
        List<String> actualLinks = LinkLister.getLinks(url);

        assertEquals(expectedLinks, actualLinks);
        verify(Jsoup.connect(url)).get();
    }

    @Test
    void testGetLinksV2_ValidUrl() throws IOException {
        String url = "https://www.example.com";
        String link1 = "https://www.example.com/page1";
        String link2 = "https://www.example.com/page2";

        Document mockDoc = Mockito.mock(Document.class);
        Elements mockLinks = Mockito.mock(Elements.class);
        when(mockDoc.select("a")).thenReturn(mockLinks);
        when(mockLinks.stream()).thenReturn(List.of(
                Mockito.mock(Element.class),
                Mockito.mock(Element.class)
        ).stream());
        when(mockLinks.get(0).absUrl()).thenReturn(link1);
        when(mockLinks.get(1).absUrl()).thenReturn(link2);

        when(Jsoup.connect(url)).thenReturn(Mockito.mock(org.jsoup.Connection.class));
        when(Jsoup.connect(url).get()).thenReturn(mockDoc);

        List<String> expectedLinks = new ArrayList<>(List.of(link1, link2));
        List<String> actualLinks = LinkLister.getLinksV2(url);

        assertEquals(expectedLinks, actualLinks);
        verify(Jsoup.connect(url)).get();
    }

    @Test
    void testGetLinksV2_PrivateIp_ThrowsBadRequest() {
        String url = "http://192.168.1.1";
        assertThrows(BadRequest.class, () -> LinkLister.getLinksV2(url));
    }

    @Test
    void testGetLinksV2_InvalidUrl_ThrowsBadRequest() {
        String url = "http://invalid.url";
        assertThrows(BadRequest.class, () -> LinkLister.getLinksV2(url));
    }
}
