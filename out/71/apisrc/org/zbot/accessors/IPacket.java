package org.zbot.accessors;

public interface IPacket {

    dl getCipher();

    void initCipher(int[] aint, int i);

    void finishBitAccess(byte b0);

    int readableBytes(int i);

    int readBits(int i);
}
