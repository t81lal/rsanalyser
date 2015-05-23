package org.zbot.accessors.collections;

public interface IHashTable {

    INode[] getBuckets();

    INode getHead();

    INode getTail();
}
