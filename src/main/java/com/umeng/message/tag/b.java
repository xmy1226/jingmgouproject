package com.umeng.message.tag;

import com.umeng.common.message.Log;

public class b
  implements a
{
  private static final String a = b.class.getName();
  private static int b = 256;

  public boolean a(String paramString)
  {
    if ((paramString == null) || ("".equals(paramString.trim())))
      return false;
    if ((paramString != null) && (paramString.length() > b))
    {
      Log.b(a, String.format("The length of %s exceeds allowed max length %i", new Object[] { paramString, Integer.valueOf(b) }));
      return false;
    }
    return true;
  }
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     com.umeng.message.tag.b
 * JD-Core Version:    0.6.2
 */