package u.aly;

import android.content.Context;
import android.provider.Settings.Secure;

public class b extends a
{
  private static final String a = "android_id";
  private Context b;

  public b(Context paramContext)
  {
    super("android_id");
    this.b = paramContext;
  }

  public String f()
  {
    try
    {
      String str = Settings.Secure.getString(this.b.getContentResolver(), "android_id");
      return str;
    }
    catch (Exception localException)
    {
    }
    return null;
  }
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     u.aly.b
 * JD-Core Version:    0.6.2
 */