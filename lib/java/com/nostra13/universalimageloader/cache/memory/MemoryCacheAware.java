package com.nostra13.universalimageloader.cache.memory;

import java.util.Collection;

@Deprecated
public abstract interface MemoryCacheAware<K, V>
{
  public abstract void clear();

  public abstract V get(K paramK);

  public abstract Collection<K> keys();

  public abstract boolean put(K paramK, V paramV);

  public abstract V remove(K paramK);
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
 * JD-Core Version:    0.6.2
 */