package com.lisa.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Main {

    private static class Page {

        public List<Page> pages = new ArrayList<>();

        public String url;

        public boolean broken;

        public int outerLinksCounter;

        public int innerLinksCounter;

        public Page(String url) {
            while (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            this.url = url;
        }

    }

    public static void main(String[] args) throws FileNotFoundException {
        PrintWriter pw;
        writeToFile(
                pw = new PrintWriter(new File(args[1])),
                crawl(args[0], Integer.valueOf(args[2])),
                ""
        );

        pw.close();
    }

    private static void writeToFile(PrintWriter pw, Page page, String shift) {
        if (page == null) {
            return;
        }

        pw.print(shift
                + page.url
                + (page.broken ? " (broken)" : "")
                + (page.innerLinksCounter > 0 ? " inner links count: " + String.valueOf(page.innerLinksCounter) : "")
                + (page.outerLinksCounter > 0 ? " outer links count: " + String.valueOf(page.outerLinksCounter) : "")
                + "\n");
        for (Page sub : page.pages) {
            writeToFile(pw, sub, shift + "\t");
        }
    }

    private static Page crawl(String url, int recursionLevel) {
        return crawl(new Page(url), url, new HashSet<>(), recursionLevel);
    }

    private static Page crawl(Page page, String homeUrl, Set<String> visited, int recursionLevel) {
        if (visited.contains(page.url)) {
            return page;
        }

        visited.add(page.url);

        try {

            Document doc = Jsoup.connect(page.url).get();
            if (recursionLevel > 0) {
                for (Element element : doc.select("a")) {

                    Page child = newPage(page, homeUrl, element.attr("href"));
                    if (child != null) {
                        crawl(child, homeUrl, visited, recursionLevel - 1);
                        page.pages.add(child);
                    }

                }
            }

        } catch (IOException e) {
            page.broken = true;
        }

        return page;
    }

    private static Page newPage(Page parent, String homeUrl, String uri) {
        if (uri.startsWith("/")) {
            parent.innerLinksCounter++;
            return new Page(parent.url + uri);
        }

        try {

            new URL(uri);   // check correct full URL
            parent.outerLinksCounter++;

            if (uri.startsWith(homeUrl)) {
                parent.innerLinksCounter++;
                return new Page(uri);
            }

        } catch (MalformedURLException e) { /* RETURN NULL AT THE END */ }

        return null;
    }

}
