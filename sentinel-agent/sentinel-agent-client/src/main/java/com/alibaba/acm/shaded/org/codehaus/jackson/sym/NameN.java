package com.alibaba.acm.shaded.org.codehaus.jackson.sym;

/**
 * Generic implementation of PName used for "long" names, where long
 * means that its byte (UTF-8) representation is 13 bytes or more.
 */
public final class NameN
    extends Name
{
    final int[] mQuads;
    final int mQuadLen;

    NameN(String name, int hash, int[] quads, int quadLen)
    {
        super(name, hash);
        /* We have specialized implementations for shorter
         * names, so let's not allow runt instances here
         */
        if (quadLen < 3) {
            throw new IllegalArgumentException("Qlen must >= 3");
        }
        mQuads = quads;
        mQuadLen = quadLen;
    }

    // Implies quad length == 1, never matches
    @Override
	public boolean equals(int quad) { return false; }

    // Implies quad length == 2, never matches
    @Override
	public boolean equals(int quad1, int quad2) { return false; }

    @Override
	public boolean equals(int[] quads, int qlen)
    {
        if (qlen != mQuadLen) {
            return false;
        }

        /* 26-Nov-2008, tatus: Strange, but it does look like
         *   unrolling here is counter-productive, reducing
         *   speed. Perhaps it prevents inlining by HotSpot or
         *   something...
         */
        // Will always have >= 3 quads, can unroll
        /*
        if (quads[0] == mQuads[0]
            && quads[1] == mQuads[1]
            && quads[2] == mQuads[2]) {
            for (int i = 3; i < qlen; ++i) {
                if (quads[i] != mQuads[i]) {
                    return false;
                }
            }
            return true;
        }
        */

        // or simpler way without unrolling:
        for (int i = 0; i < qlen; ++i) {
            if (quads[i] != mQuads[i]) {
                return false;
            }
        }
        return true;
    }
}
