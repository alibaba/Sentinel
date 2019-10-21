package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;

public interface SphResourceTypeSupport {
   Entry entryWithType(String var1, int var2, EntryType var3, int var4, Object[] var5) throws BlockException;

   Entry entryWithType(String var1, int var2, EntryType var3, int var4, boolean var5, Object[] var6) throws BlockException;

   AsyncEntry asyncEntryWithType(String var1, int var2, EntryType var3, int var4, boolean var5, Object[] var6) throws BlockException;
}
