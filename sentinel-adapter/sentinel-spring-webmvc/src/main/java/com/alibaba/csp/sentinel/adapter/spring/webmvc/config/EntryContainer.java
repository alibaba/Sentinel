package com.alibaba.csp.sentinel.adapter.spring.webmvc.config;

import com.alibaba.csp.sentinel.Entry;

/**
 * @author zhangkai
 * @description
 * @date 2019-10-17
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
