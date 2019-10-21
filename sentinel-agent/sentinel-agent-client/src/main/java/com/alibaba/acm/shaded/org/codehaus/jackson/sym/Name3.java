package com.alibaba.acm.shaded.org.codehaus.jackson.sym;

/**
 * Specialized implementation of PName: can be used for short Strings
 * that consists of 9 to 12 bytes. It's the longest special purpose
 * implementaion; longer ones are expressed using {@link NameN}.
 */
public final class Name3
    extends Name
{
    final int mQuad1;
    final int mQuad2;
    final int mQuad3;

    Name3(String name, int hash, int q1, int q2, int q3)
    {
        super(name, hash);
        mQuad1 = q1;
        mQuad2 = q2;
        mQuad3 = q3;
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
        return (qlen == 3)
            && (quads[0] == mQuad1)
            && (quads[1] == mQuad2)
            && (quads[2] == mQuad3);
    }
}
