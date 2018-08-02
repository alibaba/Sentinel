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

	public String getName() {
		return name;
	}

	public SentinelEntry setName(String name) {
		this.name = name;
		return this;
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
