package ru.nop;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class Weather {


    private static String result;
    private static final Logger logger = LoggerFactory.getLogger(Weather.class);


    public static void Update() {

        parseWeather();

    }

    public static String getWeather() {

        System.out.println(result);

        Update();
        if (!result.isEmpty())
            return result;

        else {

            Update();
            return result;

        }
    }


    private static void parseWeather() {


        try {

            Document page = getPage("https://yandex.ru/pogoda/tyumen");
            String tapmerature = page.select("span[class=temp__value]").first().text();
            String condition = page.select("div[class=link__condition day-anchor i-bem]").first().text();
            String feel = page.select("div[class=link__feelings fact__feelings").select("span[class=temp__value]").text();

            String windSpeed = "\uD83D\uDCA8 Ветер " + page.select("dl[class=term term_orient_v fact__wind-speed]").text();
            String davlenie = "\n\uD83D\uDCA7 Влажность " + page.select("dl[class=term term_orient_v fact__humidity]").text();
            String vlajnost = "\n\uD83D\uDDDC Давление " + page.select("dl[class=term term_orient_v fact__pressure]").text();


            result = "❄ Погода в Тюмени \n\uD83C\uDF2A " + tapmerature + "° " + condition + ". \n\uD83C\uDF84 Ощущается как " + feel + "°.\n" + windSpeed + davlenie + vlajnost;


        } catch (Exception e) {

            e.printStackTrace();
            result = null;

            logger.error("From Weather", e);

        }


    }


    public static Document getPage(String url) throws IOException {

        Document page = Jsoup.parse(new URL(url), 3000);
        return page;

    }


}
