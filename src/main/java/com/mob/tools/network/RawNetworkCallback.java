package com.mob.tools.network;

import java.io.InputStream;

public abstract interface RawNetworkCallback
{
  public abstract void onResponse(InputStream paramInputStream)
    throws Throwable;
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     com.mob.tools.network.RawNetworkCallback
 * JD-Core Version:    0.6.2
 */