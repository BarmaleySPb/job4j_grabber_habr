package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private String retrieveDescription(String link) throws IOException {
        AtomicReference<String> result = new AtomicReference<>("");
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".style-ugc");
        rows.forEach(row -> result.set(row.text()));

        return result.get();
    }

    public static void main(String[] args) throws IOException {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        for (int i = 1; i < 6; i++) {
            Connection connection = Jsoup.connect(PAGE_LINK + i);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element dateElement = row.select(".vacancy-card__date").first();
                Element dateVacancy = dateElement.child(0);
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String vacancyDate = String.format("%s", dateVacancy.attr("datetime"));
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", vacancyName, link, parser.parse(vacancyDate));
            });
        }
    }
}