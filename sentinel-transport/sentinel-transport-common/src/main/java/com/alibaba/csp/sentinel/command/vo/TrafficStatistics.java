/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.command.vo;
public class TrafficStatistics{
    private String id;

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id=id;
    }

    private String parentId;

    public String getParentId(){
        return parentId;
    }

    public void setParentId(String parentId){
        this.parentId=parentId;
    }

    private String resource;

    public String getResource(){
        return resource;
    }

    public void setResource(String resource){
        this.resource=resource;
    }

    private Integer threadNum;

    public Integer getThreadNum(){
        return threadNum;
    }

    public void setThreadNum(Integer threadNum){
        this.threadNum=threadNum;
    }

    private Long passQps;

    public Long getPassQps(){
        return passQps;
    }

    public void setPassQps(Long passQps){
        this.passQps=passQps;
    }

    private Long blockQps;

    public Long getBlockQps(){
        return blockQps;
    }

    public void setBlockQps(Long blockQps){
        this.blockQps=blockQps;
    }

    private Long totalQps;

    public Long getTotalQps(){
        return totalQps;
    }

    public void setTotalQps(Long totalQps){
        this.totalQps=totalQps;
    }

    private Long averageRt;

    public Long getAverageRt(){
        return averageRt;
    }

    public void setAverageRt(Long averageRt){
        this.averageRt=averageRt;
    }

    private Long successQps;

    public Long getSuccessQps(){
        return successQps;
    }

    public void setSuccessQps(Long successQps){
        this.successQps=successQps;
    }

    private Long exceptionQps;

    public Long getExceptionQps(){
        return exceptionQps;
    }

    public void setExceptionQps(Long exceptionQps){
        this.exceptionQps=exceptionQps;
    }

    private Long oneMinutePass;

    public Long getOneMinutePass(){
        return oneMinutePass;
    }

    public void setOneMinutePass(Long oneMinutePass){
        this.oneMinutePass=oneMinutePass;
    }

    private Long oneMinuteBlock;

    public Long getOneMinuteBlock(){
        return oneMinuteBlock;
    }

    public void setOneMinuteBlock(Long oneMinuteBlock){
        this.oneMinuteBlock=oneMinuteBlock;
    }

    private Long oneMinuteException;

    public Long getOneMinuteException(){
        return oneMinuteException;
    }

    public void setOneMinuteException(Long oneMinuteException){
        this.oneMinuteException=oneMinuteException;
    }

    private Long oneMinuteTotal;

    public Long getOneMinuteTotal(){
        return oneMinuteTotal;
    }

    public void setOneMinuteTotal(Long oneMinuteTotal){
        this.oneMinuteTotal=oneMinuteTotal;
    }

    public TrafficStatistics(String id,String parentId,String resource,Integer threadNum,Long passQps,Long blockQps,Long totalQps,Long averageRt,Long successQps,Long exceptionQps,Long oneMinutePass,Long oneMinuteBlock,Long oneMinuteException,Long oneMinuteTotal){
        this.id=id;
        this.parentId=parentId;
        this.resource=resource;
        this.threadNum=threadNum;
        this.passQps=passQps;
        this.blockQps=blockQps;
        this.totalQps=totalQps;
        this.averageRt=averageRt;
        this.successQps=successQps;
        this.exceptionQps=exceptionQps;
        this.oneMinutePass=oneMinutePass;
        this.oneMinuteBlock=oneMinuteBlock;
        this.oneMinuteException=oneMinuteException;
        this.oneMinuteTotal=oneMinuteTotal;
    }
}

