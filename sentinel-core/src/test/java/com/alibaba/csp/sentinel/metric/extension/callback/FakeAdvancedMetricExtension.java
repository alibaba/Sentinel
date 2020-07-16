package com.alibaba.csp.sentinel.metric.extension.callback;

import com.alibaba.csp.sentinel.metric.extension.AdvancedMetricExtension;
import com.alibaba.csp.sentinel.slots.block.BlockException;

class FakeAdvancedMetricExtension implements AdvancedMetricExtension {
    long pass = 0;
    long block = 0;
    long success = 0;
    long exception = 0;
    long rt = 0;
    long thread = 0;

	@Override
	public void addPass(String resource, int n, Object... args) {
		// Do nothing because of using the enhanced one		
	}

	@Override
	public void addBlock(String resource, int n, String origin, BlockException blockException, Object... args) {
		// Do nothing because of using the enhanced one		
	}

	@Override
	public void addSuccess(String resource, int n, Object... args) {
		// Do nothing because of using the enhanced one		
	}

	@Override
	public void addException(String resource, int n, Throwable throwable) {
		// Do nothing because of using the enhanced one		
	}

	@Override
	public void addRt(String resource, long rt, Object... args) {
		// Do nothing because of using the enhanced one		
	}

	@Override
	public void increaseThreadNum(String resource, Object... args) {
		// Do nothing because of using the enhanced one		
	}

	@Override
	public void decreaseThreadNum(String resource, Object... args) {
		// Do nothing because of using the enhanced one		
	}

	@Override
	public void addPass(String resource, String entryType, int n, Object... args) {
		pass += n;
	}

	@Override
	public void addBlock(String resource, String entryType, int n, String origin, BlockException blockException,
			Object... args) {
		block += n;
	}

	@Override
	public void addSuccess(String resource, String entryType, int n, Object... args) {
		success += n;
	}

	@Override
	public void addException(String resource, String entryType, int n, Throwable throwable) {
		exception += n;
	}

	@Override
	public void addRt(String resource, String entryTypeTag, long rt, Object... args) {
		this.rt += rt;
	}

	@Override
	public void increaseThreadNum(String resource, String entryType, Object... args) {
		thread ++;
	}

	@Override
	public void decreaseThreadNum(String resource, String entryType, Object... args) {
		thread --;
	}

}
