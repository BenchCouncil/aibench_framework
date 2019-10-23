package org.benchcouncil.aibench;

import lombok.Data;

import java.io.Serializable;

@Data
public class QueryInfo implements Serializable {
    private int qid;
    private String uid;
    private int page;
    private String query;

    public QueryInfo(String uid, String query) {
        this.uid = uid;
        this.page = 1;
        this.query = query;
    }

    public QueryInfo(int qid, String uid, String query) {
        this.qid = qid;
        this.uid = uid;
        this.query = query;
    }

    public QueryInfo(String uid, int page, String query) {
        this.uid = uid;
        this.page = page;
        this.query = query;
    }

    public QueryInfo() {
    }

    public int getQid() {
        return qid;
    }

    public void setQid(int qid) {
        this.qid = qid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
