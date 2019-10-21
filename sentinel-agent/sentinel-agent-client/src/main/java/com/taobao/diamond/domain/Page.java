package com.taobao.diamond.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 分页对象
 * 
 * @author boyan
 * @date 2010-5-6
 * @param <E>
 */
public class Page<E> implements Serializable {
    static final long serialVersionUID = -1L;

    private int totalCount; // 总记录数
    private int pageNumber; // 页数
    private int pagesAvailable; // 总页数
    private List<E> pageItems = new ArrayList<E>(); // 该页内容


    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }


    public void setPagesAvailable(int pagesAvailable) {
        this.pagesAvailable = pagesAvailable;
    }


    public void setPageItems(List<E> pageItems) {
        this.pageItems = pageItems;
    }


    public int getTotalCount() {
        return totalCount;
    }


    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }


    public int getPageNumber() {
        return pageNumber;
    }


    public int getPagesAvailable() {
        return pagesAvailable;
    }


    public List<E> getPageItems() {
        return pageItems;
    }
}
