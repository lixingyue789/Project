package Test;

import Crawler.Crawler;

import java.io.IOException;

/**
 * Author:lxy1999
 * Created:2020/7/31
 */
public class Test {
    public static void main(String[] args) throws IOException {
        Crawler crawler = new Crawler();
        String html = crawler.getPage("https://github.com/doov-io/doov");
        System.out.println(html);
    }
}
