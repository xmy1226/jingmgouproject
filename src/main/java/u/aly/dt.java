package u.aly;

public final class dt extends du
{
  private byte[] a;
  private int b;
  private int c;

  public dt()
  {
  }

  public dt(byte[] paramArrayOfByte)
  {
    a(paramArrayOfByte);
  }

  public dt(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    c(paramArrayOfByte, paramInt1, paramInt2);
  }

  public int a(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws dv
  {
    int j = h();
    int i = paramInt2;
    if (paramInt2 > j)
      i = j;
    if (i > 0)
    {
      System.arraycopy(this.a, this.b, paramArrayOfByte, paramInt1, i);
      a(i);
    }
    return i;
  }

  public void a(int paramInt)
  {
    this.b += paramInt;
  }

  public void a(byte[] paramArrayOfByte)
  {
    c(paramArrayOfByte, 0, paramArrayOfByte.length);
  }

  public boolean a()
  {
    return true;
  }

  public void b()
    throws dv
  {
  }

  public void b(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws dv
  {
    throw new UnsupportedOperationException("No writing allowed!");
  }

  public void c()
  {
  }

  public void c(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
  {
    this.a = paramArrayOfByte;
    this.b = paramInt1;
    this.c = (paramInt1 + paramInt2);
  }

  public void e()
  {
    this.a = null;
  }

  public byte[] f()
  {
    return this.a;
  }

  public int g()
  {
    return this.b;
  }

  public int h()
  {
    return this.c - this.b;
  }
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     u.aly.dt
 * JD-Core Version:    0.6.2
 */