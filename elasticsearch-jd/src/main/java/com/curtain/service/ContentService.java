package com.curtain.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author ：Curtain
 * @date ：Created in 2021/3/12 11:45
 * @description：TODO
 */
public interface ContentService {
    
    boolean parseContent(String keywords);
    
    List<Map<String,Object>> searchPage(String keywords, int pageNo, int pageSize) throws IOException;
}
