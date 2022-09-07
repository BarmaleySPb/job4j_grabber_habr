package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.model.Post;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final int PAGE = 5;

    private static final String PAGE_LINK = "/vacancies/java_developer?page=";

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private static String retrieveDescription(String link) {
        String description;
        try {
            Connection connection = Jsoup.connect(String.format(link));
            Document document = connection.get();
            Elements rows = document.select(".style-ugc");
            description = rows.get(0).text();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error collect description!");
        }
        return description;
    }

    public List<Post> list(String link) {
        List<Post> listOfLinks = new LinkedList<>();
        try {
            for (int i = 1; i <= PAGE; i++) {
                Connection connection = Jsoup.connect(link + PAGE_LINK + i);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element dateElement = row.select(".vacancy-card__date").first();
                    Element dateVacancy = dateElement.child(0);
                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    String vacancyDate = String.format("%s", dateVacancy.attr("datetime"));
                    String linka = String.format("%s%s", link, linkElement.attr("href"));
                    System.out.printf("%s %s %s%n", vacancyName, linka, vacancyDate);
                    String description = retrieveDescription(linka);
                    listOfLinks.add(
                            new Post(vacancyName,
                                    description,
                                    linka,
                                    dateTimeParser.parse(vacancyDate))
                    );
                });
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error parse data");
        }
        return listOfLinks;
    }
}
