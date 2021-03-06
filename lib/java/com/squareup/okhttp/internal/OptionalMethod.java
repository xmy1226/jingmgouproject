package com.squareup.okhttp.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class OptionalMethod<T>
{
  private final String methodName;
  private final Class[] methodParams;
  private final Class<?> returnType;

  public OptionalMethod(Class<?> paramClass, String paramString, Class[] paramArrayOfClass)
  {
    this.returnType = paramClass;
    this.methodName = paramString;
    this.methodParams = paramArrayOfClass;
  }

  private Method getMethod(Class<?> paramClass)
  {
    Class<?> localClass = null;
    if (this.methodName != null)
    {
      paramClass = getPublicMethod(paramClass, this.methodName, this.methodParams);
      localClass = paramClass;
      if (paramClass != null)
      {
        localClass = paramClass;
        if (this.returnType != null)
        {
          localClass = paramClass;
          if (!this.returnType.isAssignableFrom(paramClass.getReturnType()))
            localClass = null;
        }
      }
    }
    return localClass;
  }

  private static Method getPublicMethod(Class<?> paramClass, String paramString, Class[] paramArrayOfClass)
  {
    Class<?> localClass = null;
    try
    {
      paramClass = paramClass.getMethod(paramString, paramArrayOfClass);
      localClass = paramClass;
      int i = paramClass.getModifiers();
      if ((i & 0x1) == 0)
        paramClass = null;
      return paramClass;
    }
    catch (NoSuchMethodException paramClass)
    {
    }
    return localClass;
  }

  public Object invoke(T paramT, Object[] paramArrayOfObject)
    throws InvocationTargetException
  {
    Method localMethod = getMethod(paramT.getClass());
    if (localMethod == null)
      throw new AssertionError("Method " + this.methodName + " not supported for object " + paramT);
    try
    {
      paramT = localMethod.invoke(paramT, paramArrayOfObject);
      return paramT;
    }
    catch (IllegalAccessException paramT)
    {
      paramArrayOfObject = new AssertionError("Unexpectedly could not call: " + localMethod);
      paramArrayOfObject.initCause(paramT);
    }
    throw paramArrayOfObject;
  }

  public Object invokeOptional(T paramT, Object[] paramArrayOfObject)
    throws InvocationTargetException
  {
    Method localMethod = getMethod(paramT.getClass());
    if (localMethod == null)
      return null;
    try
    {
      paramT = localMethod.invoke(paramT, paramArrayOfObject);
      return paramT;
    }
    catch (IllegalAccessException paramT)
    {
    }
    return null;
  }

  public Object invokeOptionalWithoutCheckedException(T paramT, Object[] paramArrayOfObject)
  {
    try
    {
      paramT = invokeOptional(paramT, paramArrayOfObject);
      return paramT;
    }
    catch (InvocationTargetException paramT)
    {
      paramT = paramT.getTargetException();
      if ((paramT instanceof RuntimeException))
        throw ((RuntimeException)paramT);
      paramArrayOfObject = new AssertionError("Unexpected exception");
      paramArrayOfObject.initCause(paramT);
    }
    throw paramArrayOfObject;
  }

  public Object invokeWithoutCheckedException(T paramT, Object[] paramArrayOfObject)
  {
    try
    {
      paramT = invoke(paramT, paramArrayOfObject);
      return paramT;
    }
    catch (InvocationTargetException paramT)
    {
      paramT = paramT.getTargetException();
      if ((paramT instanceof RuntimeException))
        throw ((RuntimeException)paramT);
      paramArrayOfObject = new AssertionError("Unexpected exception");
      paramArrayOfObject.initCause(paramT);
    }
    throw paramArrayOfObject;
  }

  public boolean isSupported(T paramT)
  {
    return getMethod(paramT.getClass()) != null;
  }
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     com.squareup.okhttp.internal.OptionalMethod
 * JD-Core Version:    0.6.2
 */