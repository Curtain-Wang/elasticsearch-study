package com.curtain.utils;

import com.curtain.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：Curtain
 * @date ：Created in 2021/3/12 10:21
 * @description：TODO
 */
public class HTMLParseUtil {

    public static void main(String[] args) throws IOException {
       parseJD("java");
    }
    
    public static List<Content> parseJD(String keywords) throws IOException {
        List<Content> list = new ArrayList<>();
        //获取请求 https://search.jd.com/Search?keyword=java
        //前提需要联网！
        String url = "https://search.jd.com/Search?keyword=" + keywords;
        //解析网页 (Jsoup返回Document就是浏览器的Document对象)
        Document document = Jsoup.parse(new URL(url), 3000);
        //所有在js中的可以使用的方法，这里都能用！
        Element jGoodsList = document.getElementById("J_goodsList");
//        System.out.println(jGoodsList.html());
        //获取所有的li元素
        Elements lis = jGoodsList.getElementsByTag("li");
        //获取元素中的内容
        for (Element li: lis) {
            //关于图片特别多的网站，所有的图片都是延迟加载的
            String img = li.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = li.getElementsByClass("p-price").eq(0).text();
            String title = li.getElementsByClass("p-name").eq(0).text();
            Content content = new Content(img, title, price);
            list.add(content);
        } 
        return list;
    }
}
