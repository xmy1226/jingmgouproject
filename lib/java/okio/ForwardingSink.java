package okio;

import java.io.IOException;

public abstract class ForwardingSink
  implements Sink
{
  private final Sink delegate;

  public ForwardingSink(Sink paramSink)
  {
    if (paramSink == null)
      throw new IllegalArgumentException("delegate == null");
    this.delegate = paramSink;
  }

  public void close()
    throws IOException
  {
    this.delegate.close();
  }

  public final Sink delegate()
  {
    return this.delegate;
  }

  public void flush()
    throws IOException
  {
    this.delegate.flush();
  }

  public Timeout timeout()
  {
    return this.delegate.timeout();
  }

  public String toString()
  {
    return getClass().getSimpleName() + "(" + this.delegate.toString() + ")";
  }

  public void write(Buffer paramBuffer, long paramLong)
    throws IOException
  {
    this.delegate.write(paramBuffer, paramLong);
  }
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     okio.ForwardingSink
 * JD-Core Version:    0.6.2
 */