package org.zbot.accessors.collections;

public interface IHashTable {

    INode[] getBuckets();

    INode getHead();

    INode getFirst();

    int getSize();

    int getIndex();

    void put(INode inode, long i);

    INode first();

    INode next();

    INode get(long i);

    void clear();
}
