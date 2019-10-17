package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import com.alibaba.csp.sentinel.Entry;

/**
 * @author zhangkai
 */
public class EntryContainer {
  private Entry urlEntry;
  private Entry httpMethodUrlEntry;

  public Entry getUrlEntry() {
    return urlEntry;
  }

  public EntryContainer setUrlEntry(Entry urlEntry) {
    this.urlEntry = urlEntry;
    return this;
  }

  public Entry getHttpMethodUrlEntry() {
    return httpMethodUrlEntry;
  }

  public EntryContainer setHttpMethodUrlEntry(Entry httpMethodUrlEntry) {
    this.httpMethodUrlEntry = httpMethodUrlEntry;
    return this;
  }
}
