package com.alibaba.acm.shaded.org.codehaus.jackson.sym;

/**
 * Specialized implementation of PName: can be used for short Strings
 * that consists of 5 to 8 bytes. Usually this means relatively short
 * ascii-only names.
 *<p>
 * The reason for such specialized classes is mostly space efficiency;
 * and to a lesser degree performance. Both are achieved for short
 * Strings by avoiding another level of indirection (via quad arrays)
 */
public final class Name2
    extends Name
{
    final int mQuad1;

    final int mQuad2;

    Name2(String name, int hash, int quad1, int quad2)
    {
        super(name, hash);
        mQuad1 = quad1;
        mQuad2 = quad2;
    }

    @Override
    public boolean equals(int quad) { return false; }

    @Override
    public boolean equals(int quad1, int quad2)
    {
        return (quad1 == mQuad1) && (quad2 == mQuad2);
    }

    @Override
    public boolean equals(int[] quads, int qlen)
    {
        return (qlen == 2 && quads[0] == mQuad1 && quads[1] == mQuad2);
    }
}
