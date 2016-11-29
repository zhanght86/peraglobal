package com.peraglobal.km.crawler.web.webmagic.processor.example;

import java.util.List;

import com.peraglobal.km.crawler.web.webmagic.Page;
import com.peraglobal.km.crawler.web.webmagic.Site;
import com.peraglobal.km.crawler.web.webmagic.Spider;
import com.peraglobal.km.crawler.web.webmagic.processor.PageProcessor;

/**
 * @author code4crafter@gmail.com <br>
 */
public class OschinaBlogPageProcessor implements PageProcessor {

    private Site site = Site.me().setDomain("my.oschina.net");

    @Override
    public void process(Page page) {
        List<String> links = page.getHtml().links().regex("http://my\\.oschina\\.net/flashsword/blog/\\d+").all();
        page.addTargetRequests(links);
        page.putField("title", page.getHtml().xpath("//div[@class='BlogEntity']/div[@class='BlogTitle']/h1/text()").toString());
        if (page.getResultItems().get("title") == null) {
            //skip this page
            page.setSkip(true);
        }
        page.putField("content", page.getHtml().smartContent().toString());
        page.putField("tags", page.getHtml().xpath("//div[@class='BlogTags']/a/text()").all());
    }

    @Override
    public Site getSite() {
        return site;

    }

    public static void main(String[] args) {
        Spider.create(new OschinaBlogPageProcessor()).addUrl("http://my.oschina.net/flashsword/blog").run();
    }
}
