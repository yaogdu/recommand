package com.demai.service;


import java.util.List;
import java.util.Set;


public interface SolrService {

    Set<Long> queryMeetUids(List<Long> uids, Long meetId);
}
