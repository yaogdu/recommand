package com.demai.service.impl;


import com.demai.common.Config;
import com.demai.service.SolrService;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class SolrServiceImpl implements SolrService {


    private static final Logger logger = LoggerFactory.getLogger(SolrServiceImpl.class);

    private static HttpSolrServer solrServer;

    static {
        solrServer = new HttpSolrServer(Config.get("solr_url"));
        solrServer.setConnectionTimeout(5000);
    }

    @Override
    public Set<Long> queryMeetUids(List<Long> uids, Long meetId) {

        logger.info("start to query meetuids");

        Set<Long> meetUids = new HashSet<Long>();
        SolrQuery query = new SolrQuery();
        query.setRows(Integer.MAX_VALUE);
        String queryStr = "logType:browse AND beBrowsedContentID:" + meetId;
        query.setQuery(queryStr);

        QueryResponse rsp;
        try {
            rsp = solrServer.query(query);
            SolrDocumentList docs = rsp.getResults();
            Iterator<SolrDocument> iter = docs.iterator();

            while (iter.hasNext()) {
                SolrDocument doc = iter.next();
                Long uid = Long.parseLong(doc.getFirstValue("uid").toString());
                if (uids.contains(uid)) {
                    meetUids.add(uid);
                }
            }
        } catch (SolrServerException e) {
            logger.error("queryMeetUids error", e);
        } catch (IOException e) {
            logger.error("queryMeetUids error", e);
        }

        logger.info("queryMeetUids result is {}", meetUids);

        return meetUids;
    }

}
