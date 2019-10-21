package com.taobao.diamond.notify.utils.task;

public interface TaskProcessor {
	boolean process(String taskType, Task task);
}
