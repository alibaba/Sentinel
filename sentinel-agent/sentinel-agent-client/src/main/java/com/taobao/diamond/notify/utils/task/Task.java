package com.taobao.diamond.notify.utils.task;

/**
 * @author huali
 *
 */
public abstract class Task {
	/**
	 * 一个任务两次处理的间隔，单位是毫秒
	 */
	private long taskInterval;
	
	/**
	 * 任务上次被处理的时间，用毫秒表示
	 */
	private long lastProcessTime;
	
	public abstract void merge(Task task);
	
	public void setTaskInterval(long interval){
		this.taskInterval = interval;
	}
	
	public long getTaskInterval(){
		return this.taskInterval;
	}
	
	public void setLastProcessTime(long lastProcessTime){
		this.lastProcessTime = lastProcessTime;
	}
	
	public long getLastProcessTime(){
		return this.lastProcessTime;
	}
	
	/**
	 * TaskManager 判断当前是否需要处理这个Task，子类可以Override这个函数实现自己的逻辑
	 * @return
	 */
	public boolean shouldProcess(){
		return (System.currentTimeMillis() - this.lastProcessTime >= this.taskInterval);
	}
	
}
