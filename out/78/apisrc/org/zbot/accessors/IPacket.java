package org.zbot.accessors;

public interface IPacket {

    dt getCipher();

    int getBitCaret();

    void initCipher(int[] aint, int i);

    void finishBitAccess(byte b0);

    int readBits(int i);

    void initBitAccess(byte b0);

    int readableBytes(int i);
}
