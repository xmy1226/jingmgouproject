package com.ismartgo.beacon.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import java.io.File;
import java.io.FileInputStream;

public class DeviceInfoUtil
{
  protected static final String PREFS_DEVICE_ID = "gank_device_id";
  protected static final String PREFS_FILE = "gank_device_id.xml";
  protected static final String UUID_DIR = Environment.getExternalStorageDirectory() + File.separator + ".ismartgo";
  protected static final String UUID_FILE = "grand_device.log";
  static File gankFile;
  protected static Context s_instance;
  protected static String uuid;

  private static boolean checkRAMState()
  {
    return Environment.getExternalStorageState().equals("mounted");
  }

  public static String getPhoneRAM(Context paramContext)
  {
    StatFs localStatFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
    if (localStatFs == null)
      return "0GB";
    if (checkRAMState())
      try
      {
        paramContext = Formatter.formatFileSize(paramContext, localStatFs.getBlockCount() * localStatFs.getBlockSize()).replace(" ", "");
        return paramContext;
      }
      catch (Exception paramContext)
      {
        return "0GB";
      }
    return "0GB";
  }

  public static String getUDID(Context paramContext)
  {
    if (uuid == null)
      s_instance = paramContext;
    while (true)
    {
      try
      {
        Object localObject;
        if (uuid == null)
        {
          paramContext = s_instance.getSharedPreferences("gank_device_id.xml", 0);
          localObject = paramContext.getString("gank_device_id", null);
          if (localObject != null)
            uuid = (String)localObject;
        }
        else
        {
          return uuid;
        }
        gankFile = new File(UUID_DIR, "grand_device.log");
        boolean bool = gankFile.exists();
        if (bool)
        {
          try
          {
            localObject = new FileInputStream(gankFile.getAbsolutePath());
            byte[] arrayOfByte = new byte[((FileInputStream)localObject).available()];
            uuid = new String(arrayOfByte, 0, ((FileInputStream)localObject).read(arrayOfByte), "utf-8");
            ((FileInputStream)localObject).close();
          }
          catch (Exception localException)
          {
            localException.printStackTrace();
            getUUID(paramContext);
          }
          continue;
        }
      }
      finally
      {
      }
      getUUID(paramContext);
    }
  }

  // ERROR //
  private static void getUUID(SharedPreferences paramSharedPreferences)
  {
    // Byte code:
    //   0: getstatic 110	com/ismartgo/beacon/util/DeviceInfoUtil:s_instance	Landroid/content/Context;
    //   3: invokevirtual 166	android/content/Context:getContentResolver	()Landroid/content/ContentResolver;
    //   6: ldc 168
    //   8: invokestatic 173	android/provider/Settings$Secure:getString	(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;
    //   11: astore_1
    //   12: ldc 175
    //   14: aload_1
    //   15: invokevirtual 68	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   18: ifne +89 -> 107
    //   21: aload_1
    //   22: ldc 177
    //   24: invokevirtual 181	java/lang/String:getBytes	(Ljava/lang/String;)[B
    //   27: invokestatic 187	java/util/UUID:nameUUIDFromBytes	([B)Ljava/util/UUID;
    //   30: invokevirtual 188	java/util/UUID:toString	()Ljava/lang/String;
    //   33: putstatic 108	com/ismartgo/beacon/util/DeviceInfoUtil:uuid	Ljava/lang/String;
    //   36: aload_0
    //   37: invokeinterface 192 1 0
    //   42: ldc 8
    //   44: getstatic 108	com/ismartgo/beacon/util/DeviceInfoUtil:uuid	Ljava/lang/String;
    //   47: invokeinterface 198 3 0
    //   52: invokeinterface 201 1 0
    //   57: pop
    //   58: new 39	java/io/File
    //   61: dup
    //   62: getstatic 53	com/ismartgo/beacon/util/DeviceInfoUtil:UUID_DIR	Ljava/lang/String;
    //   65: invokespecial 202	java/io/File:<init>	(Ljava/lang/String;)V
    //   68: astore_0
    //   69: aload_0
    //   70: invokevirtual 130	java/io/File:exists	()Z
    //   73: ifne +8 -> 81
    //   76: aload_0
    //   77: invokevirtual 205	java/io/File:mkdirs	()Z
    //   80: pop
    //   81: new 207	java/io/FileOutputStream
    //   84: dup
    //   85: getstatic 127	com/ismartgo/beacon/util/DeviceInfoUtil:gankFile	Ljava/io/File;
    //   88: invokespecial 210	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
    //   91: astore_0
    //   92: aload_0
    //   93: getstatic 108	com/ismartgo/beacon/util/DeviceInfoUtil:uuid	Ljava/lang/String;
    //   96: invokevirtual 213	java/lang/String:getBytes	()[B
    //   99: invokevirtual 217	java/io/FileOutputStream:write	([B)V
    //   102: aload_0
    //   103: invokevirtual 218	java/io/FileOutputStream:close	()V
    //   106: return
    //   107: getstatic 110	com/ismartgo/beacon/util/DeviceInfoUtil:s_instance	Landroid/content/Context;
    //   110: ldc 220
    //   112: invokevirtual 224	android/content/Context:getSystemService	(Ljava/lang/String;)Ljava/lang/Object;
    //   115: checkcast 226	android/telephony/TelephonyManager
    //   118: invokevirtual 229	android/telephony/TelephonyManager:getDeviceId	()Ljava/lang/String;
    //   121: astore_1
    //   122: aload_1
    //   123: ifnull +33 -> 156
    //   126: aload_1
    //   127: ldc 177
    //   129: invokevirtual 181	java/lang/String:getBytes	(Ljava/lang/String;)[B
    //   132: invokestatic 187	java/util/UUID:nameUUIDFromBytes	([B)Ljava/util/UUID;
    //   135: invokevirtual 188	java/util/UUID:toString	()Ljava/lang/String;
    //   138: astore_1
    //   139: aload_1
    //   140: putstatic 108	com/ismartgo/beacon/util/DeviceInfoUtil:uuid	Ljava/lang/String;
    //   143: goto -107 -> 36
    //   146: astore_0
    //   147: new 231	java/lang/RuntimeException
    //   150: dup
    //   151: aload_0
    //   152: invokespecial 234	java/lang/RuntimeException:<init>	(Ljava/lang/Throwable;)V
    //   155: athrow
    //   156: invokestatic 238	java/util/UUID:randomUUID	()Ljava/util/UUID;
    //   159: invokevirtual 188	java/util/UUID:toString	()Ljava/lang/String;
    //   162: astore_1
    //   163: goto -24 -> 139
    //   166: astore_0
    //   167: aload_0
    //   168: invokevirtual 239	java/io/FileNotFoundException:printStackTrace	()V
    //   171: return
    //   172: astore_0
    //   173: aload_0
    //   174: invokevirtual 154	java/lang/Exception:printStackTrace	()V
    //   177: return
    //
    // Exception table:
    //   from	to	target	type
    //   12	36	146	java/io/UnsupportedEncodingException
    //   107	122	146	java/io/UnsupportedEncodingException
    //   126	139	146	java/io/UnsupportedEncodingException
    //   139	143	146	java/io/UnsupportedEncodingException
    //   156	163	146	java/io/UnsupportedEncodingException
    //   81	106	166	java/io/FileNotFoundException
    //   81	106	172	java/lang/Exception
  }
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     com.ismartgo.beacon.util.DeviceInfoUtil
 * JD-Core Version:    0.6.2
 */