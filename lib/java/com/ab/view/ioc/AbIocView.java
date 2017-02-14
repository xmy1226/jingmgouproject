package com.ab.view.ioc;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.FIELD})
public @interface AbIocView
{
  public abstract String click();

  public abstract int id();

  public abstract String itemClick();

  public abstract String itemLongClick();

  public abstract String longClick();

  public abstract AbIocSelect select();
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     com.ab.view.ioc.AbIocView
 * JD-Core Version:    0.6.2
 */