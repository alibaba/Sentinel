package com.alibaba.csp.sentinel.annotation.entry;

import java.util.HashSet;
import java.util.Set;

public class SentinelEntry {

	public static final SentinelEntry NULL_SENTINEL = new SentinelEntry().setName("%NULL_SENTINEL");

	public static final SentinelEntry EXCEPTION_SENTINEL = new SentinelEntry().setName("%EXCEPTION_SENTINEL");

	public static final SentinelEntry NAME_NULL_SENTINEL = new SentinelEntry().setName("%NAME_NULL_SENTINEL");

	public static final Set<SentinelEntry> NOT_SENTINEL = new HashSet<SentinelEntry>();

	static {
		NOT_SENTINEL.add(NULL_SENTINEL);
		NOT_SENTINEL.add(EXCEPTION_SENTINEL);
		NOT_SENTINEL.add(NAME_NULL_SENTINEL);
	}

	private String name;
	
	private int count;
	
	
	private Set<Class<? extends Throwable>> throwableSet;

	public String getName() {
		return name;
	}

	public SentinelEntry setName(String name) {
		this.name = name;
		return this;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Set<Class<? extends Throwable>> getThrowableSet(){
		return throwableSet;
	}

	public void addThrowableSet(Class<? extends Throwable>[] throwableArray ) {
		if(throwableArray == null || throwableArray.length == 0) {
			return;
		}
		if(throwableSet == null) {
			throwableSet = new HashSet<Class<? extends Throwable>>();
		}
		for( Class<? extends Throwable> throwable :throwableArray) {
			throwableSet.add(throwable);
		}
	}
	
	public boolean isThisThrowable(Class<? extends Throwable> class1) {
		return throwableSet == null ? false :throwableSet.contains(class1);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (name == null) {
			return false;
		}

		if (!(obj instanceof SentinelEntry)) {
			return false;
		}
		SentinelEntry sentinelEntry = (SentinelEntry) obj;
		String name = sentinelEntry.getName();

		if (!this.name.equals(name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SentinelEntry [name=" + name + "]";
	}
}
