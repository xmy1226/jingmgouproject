package com.google.zxing.common;

import com.google.zxing.Binarizer;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;

public class GlobalHistogramBinarizer extends Binarizer
{
  private static final int LUMINANCE_BITS = 5;
  private static final int LUMINANCE_BUCKETS = 32;
  private static final int LUMINANCE_SHIFT = 3;
  private int[] buckets = null;
  private byte[] luminances = null;

  public GlobalHistogramBinarizer(LuminanceSource paramLuminanceSource)
  {
    super(paramLuminanceSource);
  }

  private static int estimateBlackPoint(int[] paramArrayOfInt)
    throws NotFoundException
  {
    int i2 = 0;
    int i3 = paramArrayOfInt.length;
    int i = 0;
    int m = 0;
    int j = 0;
    int i1;
    for (int n = 0; i < i3; n = i1)
    {
      k = m;
      if (paramArrayOfInt[i] > m)
      {
        k = paramArrayOfInt[i];
        j = i;
      }
      i1 = n;
      if (paramArrayOfInt[i] > n)
        i1 = paramArrayOfInt[i];
      i += 1;
      m = k;
    }
    m = 0;
    i = 0;
    int k = i2;
    if (k < i3)
    {
      i1 = k - j;
      i1 *= paramArrayOfInt[k] * i1;
      if (i1 <= m)
        break label251;
      m = k;
      i = i1;
    }
    while (true)
    {
      k += 1;
      i1 = m;
      m = i;
      i = i1;
      break;
      if (j > i)
      {
        i1 = i;
        m = j;
      }
      while (true)
      {
        if (m - i1 <= i3 >> 4)
          throw NotFoundException.getNotFoundInstance();
        j = m - 1;
        k = -1;
        i = m - 1;
        if (i > i1)
        {
          i2 = i - i1;
          i2 = i2 * i2 * (m - i) * (n - paramArrayOfInt[i]);
          if (i2 <= k)
            break label231;
          k = i;
          j = i2;
        }
        while (true)
        {
          i -= 1;
          i2 = k;
          k = j;
          j = i2;
          break;
          return j << 3;
          label231: i2 = j;
          j = k;
          k = i2;
        }
        m = i;
        i1 = j;
      }
      label251: i1 = i;
      i = m;
      m = i1;
    }
  }

  private void initArrays(int paramInt)
  {
    if ((this.luminances == null) || (this.luminances.length < paramInt))
      this.luminances = new byte[paramInt];
    if (this.buckets == null)
      this.buckets = new int[32];
    while (true)
    {
      return;
      paramInt = 0;
      while (paramInt < 32)
      {
        this.buckets[paramInt] = 0;
        paramInt += 1;
      }
    }
  }

  public Binarizer createBinarizer(LuminanceSource paramLuminanceSource)
  {
    return new GlobalHistogramBinarizer(paramLuminanceSource);
  }

  public BitMatrix getBlackMatrix()
    throws NotFoundException
  {
    Object localObject = getLuminanceSource();
    int k = ((LuminanceSource)localObject).getWidth();
    int m = ((LuminanceSource)localObject).getHeight();
    BitMatrix localBitMatrix = new BitMatrix(k, m);
    initArrays(k);
    int[] arrayOfInt = this.buckets;
    int i = 1;
    int j;
    while (i < 5)
    {
      byte[] arrayOfByte = ((LuminanceSource)localObject).getRow(m * i / 5, this.luminances);
      n = (k << 2) / 5;
      j = k / 5;
      while (j < n)
      {
        int i1 = (arrayOfByte[j] & 0xFF) >> 3;
        arrayOfInt[i1] += 1;
        j += 1;
      }
      i += 1;
    }
    int n = estimateBlackPoint(arrayOfInt);
    localObject = ((LuminanceSource)localObject).getMatrix();
    i = 0;
    while (i < m)
    {
      j = 0;
      while (j < k)
      {
        if ((localObject[(i * k + j)] & 0xFF) < n)
          localBitMatrix.set(j, i);
        j += 1;
      }
      i += 1;
    }
    return localBitMatrix;
  }

  public BitArray getBlackRow(int paramInt, BitArray paramBitArray)
    throws NotFoundException
  {
    int i = 1;
    Object localObject = getLuminanceSource();
    int m = ((LuminanceSource)localObject).getWidth();
    if ((paramBitArray == null) || (paramBitArray.getSize() < m))
      paramBitArray = new BitArray(m);
    int[] arrayOfInt;
    while (true)
    {
      initArrays(m);
      localObject = ((LuminanceSource)localObject).getRow(paramInt, this.luminances);
      arrayOfInt = this.buckets;
      paramInt = 0;
      while (paramInt < m)
      {
        j = (localObject[paramInt] & 0xFF) >> 3;
        arrayOfInt[j] += 1;
        paramInt += 1;
      }
      paramBitArray.clear();
    }
    int n = estimateBlackPoint(arrayOfInt);
    int j = localObject[0];
    paramInt = localObject[1] & 0xFF;
    j &= 255;
    while (i < m - 1)
    {
      int k = localObject[(i + 1)] & 0xFF;
      if ((paramInt << 2) - j - k >> 1 < n)
        paramBitArray.set(i);
      i += 1;
      j = paramInt;
      paramInt = k;
    }
    return paramBitArray;
  }
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     com.google.zxing.common.GlobalHistogramBinarizer
 * JD-Core Version:    0.6.2
 */