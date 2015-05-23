package org.zbot.accessors;

public interface IPacket {

    db getCipher();

    void initCipher(int[] aint, short short0);

    void finishBitAccess(int i);

    int readBits(int i);
}
