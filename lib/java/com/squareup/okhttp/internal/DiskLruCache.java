package com.squareup.okhttp.internal;

import com.squareup.okhttp.internal.io.FileSystem;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import okio.Source;
import okio.Timeout;

public final class DiskLruCache
  implements Closeable
{
  static final long ANY_SEQUENCE_NUMBER = -1L;
  private static final String CLEAN = "CLEAN";
  private static final String DIRTY = "DIRTY";
  static final String JOURNAL_FILE = "journal";
  static final String JOURNAL_FILE_BACKUP = "journal.bkp";
  static final String JOURNAL_FILE_TEMP = "journal.tmp";
  static final Pattern LEGAL_KEY_PATTERN;
  static final String MAGIC = "libcore.io.DiskLruCache";
  private static final Sink NULL_SINK;
  private static final String READ = "READ";
  private static final String REMOVE = "REMOVE";
  static final String VERSION_1 = "1";
  private final int appVersion;
  private final Runnable cleanupRunnable = new Runnable()
  {
    public void run()
    {
      int i = 0;
      synchronized (DiskLruCache.this)
      {
        if (!DiskLruCache.this.initialized)
          i = 1;
        if ((i | DiskLruCache.this.closed) != 0)
          return;
      }
      try
      {
        DiskLruCache.this.trimToSize();
        if (DiskLruCache.this.journalRebuildRequired())
        {
          DiskLruCache.this.rebuildJournal();
          DiskLruCache.access$502(DiskLruCache.this, 0);
        }
        return;
        localObject = finally;
        throw localObject;
      }
      catch (IOException localIOException)
      {
        throw new RuntimeException(localIOException);
      }
    }
  };
  private boolean closed;
  private final File directory;
  private final Executor executor;
  private final FileSystem fileSystem;
  private boolean hasJournalErrors;
  private boolean initialized;
  private final File journalFile;
  private final File journalFileBackup;
  private final File journalFileTmp;
  private BufferedSink journalWriter;
  private final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap(0, 0.75F, true);
  private long maxSize;
  private long nextSequenceNumber = 0L;
  private int redundantOpCount;
  private long size = 0L;
  private final int valueCount;

  static
  {
    if (!DiskLruCache.class.desiredAssertionStatus());
    for (boolean bool = true; ; bool = false)
    {
      $assertionsDisabled = bool;
      LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,120}");
      NULL_SINK = new Sink()
      {
        public void close()
          throws IOException
        {
        }

        public void flush()
          throws IOException
        {
        }

        public Timeout timeout()
        {
          return Timeout.NONE;
        }

        public void write(Buffer paramAnonymousBuffer, long paramAnonymousLong)
          throws IOException
        {
          paramAnonymousBuffer.skip(paramAnonymousLong);
        }
      };
      return;
    }
  }

  DiskLruCache(FileSystem paramFileSystem, File paramFile, int paramInt1, int paramInt2, long paramLong, Executor paramExecutor)
  {
    this.fileSystem = paramFileSystem;
    this.directory = paramFile;
    this.appVersion = paramInt1;
    this.journalFile = new File(paramFile, "journal");
    this.journalFileTmp = new File(paramFile, "journal.tmp");
    this.journalFileBackup = new File(paramFile, "journal.bkp");
    this.valueCount = paramInt2;
    this.maxSize = paramLong;
    this.executor = paramExecutor;
  }

  private void checkNotClosed()
  {
    try
    {
      if (isClosed())
        throw new IllegalStateException("cache is closed");
    }
    finally
    {
    }
  }

  private void completeEdit(Editor paramEditor, boolean paramBoolean)
    throws IOException
  {
    Entry localEntry;
    try
    {
      localEntry = paramEditor.entry;
      if (localEntry.currentEditor != paramEditor)
        throw new IllegalStateException();
    }
    finally
    {
    }
    if ((paramBoolean) && (!localEntry.readable))
    {
      i = 0;
      while (i < this.valueCount)
      {
        if (paramEditor.written[i] == 0)
        {
          paramEditor.abort();
          throw new IllegalStateException("Newly created entry didn't create value for index " + i);
        }
        if (!this.fileSystem.exists(localEntry.dirtyFiles[i]))
        {
          paramEditor.abort();
          return;
        }
        i += 1;
      }
    }
    int i = 0;
    while (true)
    {
      long l1;
      if (i < this.valueCount)
      {
        paramEditor = localEntry.dirtyFiles[i];
        if (paramBoolean)
        {
          if (this.fileSystem.exists(paramEditor))
          {
            File localFile = localEntry.cleanFiles[i];
            this.fileSystem.rename(paramEditor, localFile);
            l1 = localEntry.lengths[i];
            long l2 = this.fileSystem.size(localFile);
            localEntry.lengths[i] = l2;
            this.size = (this.size - l1 + l2);
          }
        }
        else
          this.fileSystem.delete(paramEditor);
      }
      else
      {
        this.redundantOpCount += 1;
        Entry.access$902(localEntry, null);
        if ((localEntry.readable | paramBoolean))
        {
          Entry.access$802(localEntry, true);
          this.journalWriter.writeUtf8("CLEAN").writeByte(32);
          this.journalWriter.writeUtf8(localEntry.key);
          localEntry.writeLengths(this.journalWriter);
          this.journalWriter.writeByte(10);
          if (paramBoolean)
          {
            l1 = this.nextSequenceNumber;
            this.nextSequenceNumber = (1L + l1);
            Entry.access$1602(localEntry, l1);
          }
        }
        while (true)
        {
          this.journalWriter.flush();
          if ((this.size <= this.maxSize) && (!journalRebuildRequired()))
            break;
          this.executor.execute(this.cleanupRunnable);
          break;
          this.lruEntries.remove(localEntry.key);
          this.journalWriter.writeUtf8("REMOVE").writeByte(32);
          this.journalWriter.writeUtf8(localEntry.key);
          this.journalWriter.writeByte(10);
        }
      }
      i += 1;
    }
  }

  public static DiskLruCache create(FileSystem paramFileSystem, File paramFile, int paramInt1, int paramInt2, long paramLong)
  {
    if (paramLong <= 0L)
      throw new IllegalArgumentException("maxSize <= 0");
    if (paramInt2 <= 0)
      throw new IllegalArgumentException("valueCount <= 0");
    return new DiskLruCache(paramFileSystem, paramFile, paramInt1, paramInt2, paramLong, new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue(), Util.threadFactory("OkHttp DiskLruCache", true)));
  }

  private Editor edit(String paramString, long paramLong)
    throws IOException
  {
    Object localObject2 = null;
    try
    {
      initialize();
      checkNotClosed();
      validateKey(paramString);
      Entry localEntry = (Entry)this.lruEntries.get(paramString);
      Object localObject1;
      if (paramLong != -1L)
      {
        localObject1 = localObject2;
        if (localEntry != null)
        {
          long l = localEntry.sequenceNumber;
          if (l == paramLong)
            break label71;
          localObject1 = localObject2;
        }
      }
      while (true)
      {
        return localObject1;
        label71: if (localEntry != null)
        {
          localObject1 = localObject2;
          if (localEntry.currentEditor != null);
        }
        else
        {
          this.journalWriter.writeUtf8("DIRTY").writeByte(32).writeUtf8(paramString).writeByte(10);
          this.journalWriter.flush();
          localObject1 = localObject2;
          if (!this.hasJournalErrors)
          {
            localObject1 = localEntry;
            if (localEntry == null)
            {
              localObject1 = new Entry(paramString, null);
              this.lruEntries.put(paramString, localObject1);
            }
            paramString = new Editor((Entry)localObject1, null);
            Entry.access$902((Entry)localObject1, paramString);
            localObject1 = paramString;
          }
        }
      }
    }
    finally
    {
    }
    throw paramString;
  }

  private boolean journalRebuildRequired()
  {
    return (this.redundantOpCount >= 2000) && (this.redundantOpCount >= this.lruEntries.size());
  }

  private BufferedSink newJournalWriter()
    throws FileNotFoundException
  {
    return Okio.buffer(new FaultHidingSink(this.fileSystem.appendingSink(this.journalFile))
    {
      static
      {
        if (!DiskLruCache.class.desiredAssertionStatus());
        for (boolean bool = true; ; bool = false)
        {
          $assertionsDisabled = bool;
          return;
        }
      }

      protected void onException(IOException paramAnonymousIOException)
      {
        assert (Thread.holdsLock(DiskLruCache.this));
        DiskLruCache.access$602(DiskLruCache.this, true);
      }
    });
  }

  private void processJournal()
    throws IOException
  {
    this.fileSystem.delete(this.journalFileTmp);
    Iterator localIterator = this.lruEntries.values().iterator();
    while (localIterator.hasNext())
    {
      Entry localEntry = (Entry)localIterator.next();
      int i;
      if (localEntry.currentEditor == null)
      {
        i = 0;
        while (i < this.valueCount)
        {
          this.size += localEntry.lengths[i];
          i += 1;
        }
      }
      else
      {
        Entry.access$902(localEntry, null);
        i = 0;
        while (i < this.valueCount)
        {
          this.fileSystem.delete(localEntry.cleanFiles[i]);
          this.fileSystem.delete(localEntry.dirtyFiles[i]);
          i += 1;
        }
        localIterator.remove();
      }
    }
  }

  // ERROR //
  private void readJournal()
    throws IOException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 132	com/squareup/okhttp/internal/DiskLruCache:fileSystem	Lcom/squareup/okhttp/internal/io/FileSystem;
    //   4: aload_0
    //   5: getfield 143	com/squareup/okhttp/internal/DiskLruCache:journalFile	Ljava/io/File;
    //   8: invokeinterface 439 2 0
    //   13: invokestatic 442	okio/Okio:buffer	(Lokio/Source;)Lokio/BufferedSource;
    //   16: astore_2
    //   17: aload_2
    //   18: invokeinterface 447 1 0
    //   23: astore_3
    //   24: aload_2
    //   25: invokeinterface 447 1 0
    //   30: astore 4
    //   32: aload_2
    //   33: invokeinterface 447 1 0
    //   38: astore 5
    //   40: aload_2
    //   41: invokeinterface 447 1 0
    //   46: astore 6
    //   48: aload_2
    //   49: invokeinterface 447 1 0
    //   54: astore 7
    //   56: ldc 50
    //   58: aload_3
    //   59: invokevirtual 453	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   62: ifeq +54 -> 116
    //   65: ldc 59
    //   67: aload 4
    //   69: invokevirtual 453	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   72: ifeq +44 -> 116
    //   75: aload_0
    //   76: getfield 136	com/squareup/okhttp/internal/DiskLruCache:appVersion	I
    //   79: invokestatic 458	java/lang/Integer:toString	(I)Ljava/lang/String;
    //   82: aload 5
    //   84: invokevirtual 453	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   87: ifeq +29 -> 116
    //   90: aload_0
    //   91: getfield 149	com/squareup/okhttp/internal/DiskLruCache:valueCount	I
    //   94: invokestatic 458	java/lang/Integer:toString	(I)Ljava/lang/String;
    //   97: aload 6
    //   99: invokevirtual 453	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   102: ifeq +14 -> 116
    //   105: ldc_w 460
    //   108: aload 7
    //   110: invokevirtual 453	java/lang/String:equals	(Ljava/lang/Object;)Z
    //   113: ifne +77 -> 190
    //   116: new 163	java/io/IOException
    //   119: dup
    //   120: new 243	java/lang/StringBuilder
    //   123: dup
    //   124: invokespecial 244	java/lang/StringBuilder:<init>	()V
    //   127: ldc_w 462
    //   130: invokevirtual 250	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   133: aload_3
    //   134: invokevirtual 250	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   137: ldc_w 464
    //   140: invokevirtual 250	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   143: aload 4
    //   145: invokevirtual 250	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   148: ldc_w 464
    //   151: invokevirtual 250	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   154: aload 6
    //   156: invokevirtual 250	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   159: ldc_w 464
    //   162: invokevirtual 250	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   165: aload 7
    //   167: invokevirtual 250	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   170: ldc_w 466
    //   173: invokevirtual 250	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   176: invokevirtual 257	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   179: invokespecial 467	java/io/IOException:<init>	(Ljava/lang/String;)V
    //   182: athrow
    //   183: astore_3
    //   184: aload_2
    //   185: invokestatic 471	com/squareup/okhttp/internal/Util:closeQuietly	(Ljava/io/Closeable;)V
    //   188: aload_3
    //   189: athrow
    //   190: iconst_0
    //   191: istore_1
    //   192: aload_0
    //   193: aload_2
    //   194: invokeinterface 447 1 0
    //   199: invokespecial 474	com/squareup/okhttp/internal/DiskLruCache:readJournalLine	(Ljava/lang/String;)V
    //   202: iload_1
    //   203: iconst_1
    //   204: iadd
    //   205: istore_1
    //   206: goto -14 -> 192
    //   209: astore_3
    //   210: aload_0
    //   211: iload_1
    //   212: aload_0
    //   213: getfield 123	com/squareup/okhttp/internal/DiskLruCache:lruEntries	Ljava/util/LinkedHashMap;
    //   216: invokevirtual 393	java/util/LinkedHashMap:size	()I
    //   219: isub
    //   220: putfield 207	com/squareup/okhttp/internal/DiskLruCache:redundantOpCount	I
    //   223: aload_2
    //   224: invokeinterface 477 1 0
    //   229: ifne +12 -> 241
    //   232: aload_0
    //   233: invokespecial 203	com/squareup/okhttp/internal/DiskLruCache:rebuildJournal	()V
    //   236: aload_2
    //   237: invokestatic 471	com/squareup/okhttp/internal/Util:closeQuietly	(Ljava/io/Closeable;)V
    //   240: return
    //   241: aload_0
    //   242: aload_0
    //   243: invokespecial 479	com/squareup/okhttp/internal/DiskLruCache:newJournalWriter	()Lokio/BufferedSink;
    //   246: putfield 295	com/squareup/okhttp/internal/DiskLruCache:journalWriter	Lokio/BufferedSink;
    //   249: goto -13 -> 236
    //
    // Exception table:
    //   from	to	target	type
    //   17	116	183	finally
    //   116	183	183	finally
    //   192	202	183	finally
    //   210	236	183	finally
    //   241	249	183	finally
    //   192	202	209	java/io/EOFException
  }

  private void readJournalLine(String paramString)
    throws IOException
  {
    int i = paramString.indexOf(' ');
    if (i == -1)
      throw new IOException("unexpected journal line: " + paramString);
    int j = i + 1;
    int k = paramString.indexOf(' ', j);
    Object localObject2;
    Object localObject1;
    if (k == -1)
    {
      localObject2 = paramString.substring(j);
      localObject1 = localObject2;
      if (i != "REMOVE".length())
        break label112;
      localObject1 = localObject2;
      if (!paramString.startsWith("REMOVE"))
        break label112;
      this.lruEntries.remove(localObject2);
    }
    label112: 
    do
    {
      return;
      localObject1 = paramString.substring(j, k);
      Entry localEntry = (Entry)this.lruEntries.get(localObject1);
      localObject2 = localEntry;
      if (localEntry == null)
      {
        localObject2 = new Entry((String)localObject1, null);
        this.lruEntries.put(localObject1, localObject2);
      }
      if ((k != -1) && (i == "CLEAN".length()) && (paramString.startsWith("CLEAN")))
      {
        paramString = paramString.substring(k + 1).split(" ");
        Entry.access$802((Entry)localObject2, true);
        Entry.access$902((Entry)localObject2, null);
        ((Entry)localObject2).setLengths(paramString);
        return;
      }
      if ((k == -1) && (i == "DIRTY".length()) && (paramString.startsWith("DIRTY")))
      {
        Entry.access$902((Entry)localObject2, new Editor((Entry)localObject2, null));
        return;
      }
    }
    while ((k == -1) && (i == "READ".length()) && (paramString.startsWith("READ")));
    throw new IOException("unexpected journal line: " + paramString);
  }

  private void rebuildJournal()
    throws IOException
  {
    while (true)
    {
      Entry localEntry;
      try
      {
        if (this.journalWriter != null)
          this.journalWriter.close();
        BufferedSink localBufferedSink1 = Okio.buffer(this.fileSystem.sink(this.journalFileTmp));
        try
        {
          localBufferedSink1.writeUtf8("libcore.io.DiskLruCache").writeByte(10);
          localBufferedSink1.writeUtf8("1").writeByte(10);
          localBufferedSink1.writeDecimalLong(this.appVersion).writeByte(10);
          localBufferedSink1.writeDecimalLong(this.valueCount).writeByte(10);
          localBufferedSink1.writeByte(10);
          Iterator localIterator = this.lruEntries.values().iterator();
          if (!localIterator.hasNext())
            break;
          localEntry = (Entry)localIterator.next();
          if (localEntry.currentEditor != null)
          {
            localBufferedSink1.writeUtf8("DIRTY").writeByte(32);
            localBufferedSink1.writeUtf8(localEntry.key);
            localBufferedSink1.writeByte(10);
            continue;
          }
        }
        finally
        {
          localBufferedSink1.close();
        }
      }
      finally
      {
      }
      localBufferedSink2.writeUtf8("CLEAN").writeByte(32);
      localBufferedSink2.writeUtf8(localEntry.key);
      localEntry.writeLengths(localBufferedSink2);
      localBufferedSink2.writeByte(10);
    }
    localBufferedSink2.close();
    if (this.fileSystem.exists(this.journalFile))
      this.fileSystem.rename(this.journalFile, this.journalFileBackup);
    this.fileSystem.rename(this.journalFileTmp, this.journalFile);
    this.fileSystem.delete(this.journalFileBackup);
    this.journalWriter = newJournalWriter();
    this.hasJournalErrors = false;
  }

  private boolean removeEntry(Entry paramEntry)
    throws IOException
  {
    if (paramEntry.currentEditor != null)
      Editor.access$1902(paramEntry.currentEditor, true);
    int i = 0;
    while (i < this.valueCount)
    {
      this.fileSystem.delete(paramEntry.cleanFiles[i]);
      this.size -= paramEntry.lengths[i];
      paramEntry.lengths[i] = 0L;
      i += 1;
    }
    this.redundantOpCount += 1;
    this.journalWriter.writeUtf8("REMOVE").writeByte(32).writeUtf8(paramEntry.key).writeByte(10);
    this.lruEntries.remove(paramEntry.key);
    if (journalRebuildRequired())
      this.executor.execute(this.cleanupRunnable);
    return true;
  }

  private void trimToSize()
    throws IOException
  {
    while (this.size > this.maxSize)
      removeEntry((Entry)this.lruEntries.values().iterator().next());
  }

  private void validateKey(String paramString)
  {
    if (!LEGAL_KEY_PATTERN.matcher(paramString).matches())
      throw new IllegalArgumentException("keys must match regex [a-z0-9_-]{1,120}: \"" + paramString + "\"");
  }

  public void close()
    throws IOException
  {
    while (true)
    {
      int i;
      try
      {
        if ((!this.initialized) || (this.closed))
        {
          this.closed = true;
          return;
        }
        Entry[] arrayOfEntry = (Entry[])this.lruEntries.values().toArray(new Entry[this.lruEntries.size()]);
        int j = arrayOfEntry.length;
        i = 0;
        if (i < j)
        {
          Entry localEntry = arrayOfEntry[i];
          if (localEntry.currentEditor != null)
            localEntry.currentEditor.abort();
        }
        else
        {
          trimToSize();
          this.journalWriter.close();
          this.journalWriter = null;
          this.closed = true;
          continue;
        }
      }
      finally
      {
      }
      i += 1;
    }
  }

  public void delete()
    throws IOException
  {
    close();
    this.fileSystem.deleteContents(this.directory);
  }

  public Editor edit(String paramString)
    throws IOException
  {
    return edit(paramString, -1L);
  }

  public void evictAll()
    throws IOException
  {
    try
    {
      initialize();
      Entry[] arrayOfEntry = (Entry[])this.lruEntries.values().toArray(new Entry[this.lruEntries.size()]);
      int j = arrayOfEntry.length;
      int i = 0;
      while (i < j)
      {
        removeEntry(arrayOfEntry[i]);
        i += 1;
      }
      return;
    }
    finally
    {
    }
  }

  public void flush()
    throws IOException
  {
    try
    {
      boolean bool = this.initialized;
      if (!bool);
      while (true)
      {
        return;
        checkNotClosed();
        trimToSize();
        this.journalWriter.flush();
      }
    }
    finally
    {
    }
  }

  public Snapshot get(String paramString)
    throws IOException
  {
    try
    {
      initialize();
      checkNotClosed();
      validateKey(paramString);
      Object localObject = (Entry)this.lruEntries.get(paramString);
      if (localObject != null)
      {
        boolean bool = ((Entry)localObject).readable;
        if (bool);
      }
      else
      {
        paramString = null;
      }
      while (true)
      {
        return paramString;
        localObject = ((Entry)localObject).snapshot();
        if (localObject == null)
        {
          paramString = null;
        }
        else
        {
          this.redundantOpCount += 1;
          this.journalWriter.writeUtf8("READ").writeByte(32).writeUtf8(paramString).writeByte(10);
          paramString = (String)localObject;
          if (journalRebuildRequired())
          {
            this.executor.execute(this.cleanupRunnable);
            paramString = (String)localObject;
          }
        }
      }
    }
    finally
    {
    }
    throw paramString;
  }

  public File getDirectory()
  {
    return this.directory;
  }

  public long getMaxSize()
  {
    try
    {
      long l = this.maxSize;
      return l;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  public void initialize()
    throws IOException
  {
    try
    {
      if ((!$assertionsDisabled) && (!Thread.holdsLock(this)))
        throw new AssertionError();
    }
    finally
    {
    }
    boolean bool = this.initialized;
    if (bool)
      return;
    if (this.fileSystem.exists(this.journalFileBackup))
    {
      if (!this.fileSystem.exists(this.journalFile))
        break label189;
      this.fileSystem.delete(this.journalFileBackup);
    }
    while (true)
    {
      while (true)
      {
        bool = this.fileSystem.exists(this.journalFile);
        if (bool)
          try
          {
            readJournal();
            processJournal();
            this.initialized = true;
          }
          catch (IOException localIOException)
          {
            Platform.get().logW("DiskLruCache " + this.directory + " is corrupt: " + localIOException.getMessage() + ", removing");
            delete();
            this.closed = false;
          }
      }
      rebuildJournal();
      this.initialized = true;
      break;
      label189: this.fileSystem.rename(this.journalFileBackup, this.journalFile);
    }
  }

  public boolean isClosed()
  {
    try
    {
      boolean bool = this.closed;
      return bool;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  public boolean remove(String paramString)
    throws IOException
  {
    try
    {
      initialize();
      checkNotClosed();
      validateKey(paramString);
      paramString = (Entry)this.lruEntries.get(paramString);
      if (paramString == null);
      for (boolean bool = false; ; bool = removeEntry(paramString))
        return bool;
    }
    finally
    {
    }
    throw paramString;
  }

  public void setMaxSize(long paramLong)
  {
    try
    {
      this.maxSize = paramLong;
      if (this.initialized)
        this.executor.execute(this.cleanupRunnable);
      return;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  public long size()
    throws IOException
  {
    try
    {
      initialize();
      long l = this.size;
      return l;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  public Iterator<Snapshot> snapshots()
    throws IOException
  {
    try
    {
      initialize();
      Iterator local3 = new Iterator()
      {
        final Iterator<DiskLruCache.Entry> delegate = new ArrayList(DiskLruCache.this.lruEntries.values()).iterator();
        DiskLruCache.Snapshot nextSnapshot;
        DiskLruCache.Snapshot removeSnapshot;

        public boolean hasNext()
        {
          if (this.nextSnapshot != null)
            return true;
          synchronized (DiskLruCache.this)
          {
            if (DiskLruCache.this.closed)
              return false;
            while (this.delegate.hasNext())
            {
              DiskLruCache.Snapshot localSnapshot = ((DiskLruCache.Entry)this.delegate.next()).snapshot();
              if (localSnapshot != null)
              {
                this.nextSnapshot = localSnapshot;
                return true;
              }
            }
          }
          return false;
        }

        public DiskLruCache.Snapshot next()
        {
          if (!hasNext())
            throw new NoSuchElementException();
          this.removeSnapshot = this.nextSnapshot;
          this.nextSnapshot = null;
          return this.removeSnapshot;
        }

        // ERROR //
        public void remove()
        {
          // Byte code:
          //   0: aload_0
          //   1: getfield 74	com/squareup/okhttp/internal/DiskLruCache$3:removeSnapshot	Lcom/squareup/okhttp/internal/DiskLruCache$Snapshot;
          //   4: ifnonnull +13 -> 17
          //   7: new 81	java/lang/IllegalStateException
          //   10: dup
          //   11: ldc 83
          //   13: invokespecial 86	java/lang/IllegalStateException:<init>	(Ljava/lang/String;)V
          //   16: athrow
          //   17: aload_0
          //   18: getfield 24	com/squareup/okhttp/internal/DiskLruCache$3:this$0	Lcom/squareup/okhttp/internal/DiskLruCache;
          //   21: aload_0
          //   22: getfield 74	com/squareup/okhttp/internal/DiskLruCache$3:removeSnapshot	Lcom/squareup/okhttp/internal/DiskLruCache$Snapshot;
          //   25: invokestatic 92	com/squareup/okhttp/internal/DiskLruCache$Snapshot:access$2100	(Lcom/squareup/okhttp/internal/DiskLruCache$Snapshot;)Ljava/lang/String;
          //   28: invokevirtual 95	com/squareup/okhttp/internal/DiskLruCache:remove	(Ljava/lang/String;)Z
          //   31: pop
          //   32: aload_0
          //   33: aconst_null
          //   34: putfield 74	com/squareup/okhttp/internal/DiskLruCache$3:removeSnapshot	Lcom/squareup/okhttp/internal/DiskLruCache$Snapshot;
          //   37: return
          //   38: astore_1
          //   39: aload_0
          //   40: aconst_null
          //   41: putfield 74	com/squareup/okhttp/internal/DiskLruCache$3:removeSnapshot	Lcom/squareup/okhttp/internal/DiskLruCache$Snapshot;
          //   44: return
          //   45: astore_1
          //   46: aload_0
          //   47: aconst_null
          //   48: putfield 74	com/squareup/okhttp/internal/DiskLruCache$3:removeSnapshot	Lcom/squareup/okhttp/internal/DiskLruCache$Snapshot;
          //   51: aload_1
          //   52: athrow
          //
          // Exception table:
          //   from	to	target	type
          //   17	32	38	java/io/IOException
          //   17	32	45	finally
        }
      };
      return local3;
    }
    finally
    {
      localObject = finally;
      throw localObject;
    }
  }

  public final class Editor
  {
    private boolean committed;
    private final DiskLruCache.Entry entry;
    private boolean hasErrors;
    private final boolean[] written;

    private Editor(DiskLruCache.Entry arg2)
    {
      DiskLruCache.Entry localEntry;
      this.entry = localEntry;
      if (DiskLruCache.Entry.access$800(localEntry));
      for (this$1 = null; ; this$1 = new boolean[DiskLruCache.this.valueCount])
      {
        this.written = DiskLruCache.this;
        return;
      }
    }

    public void abort()
      throws IOException
    {
      synchronized (DiskLruCache.this)
      {
        DiskLruCache.this.completeEdit(this, false);
        return;
      }
    }

    public void abortUnlessCommitted()
    {
      synchronized (DiskLruCache.this)
      {
        boolean bool = this.committed;
        if (bool);
      }
      try
      {
        DiskLruCache.this.completeEdit(this, false);
        label25: return;
        localObject = finally;
        throw localObject;
      }
      catch (IOException localIOException)
      {
        break label25;
      }
    }

    public void commit()
      throws IOException
    {
      synchronized (DiskLruCache.this)
      {
        if (this.hasErrors)
        {
          DiskLruCache.this.completeEdit(this, false);
          DiskLruCache.this.removeEntry(this.entry);
          this.committed = true;
          return;
        }
        DiskLruCache.this.completeEdit(this, true);
      }
    }

    public Sink newSink(int paramInt)
      throws IOException
    {
      synchronized (DiskLruCache.this)
      {
        if (DiskLruCache.Entry.access$900(this.entry) != this)
          throw new IllegalStateException();
      }
      if (!DiskLruCache.Entry.access$800(this.entry))
        this.written[paramInt] = true;
      Object localObject2 = DiskLruCache.Entry.access$1400(this.entry)[paramInt];
      try
      {
        localObject2 = DiskLruCache.this.fileSystem.sink((File)localObject2);
        localObject2 = new FaultHidingSink((Sink)localObject2)
        {
          protected void onException(IOException arg1)
          {
            synchronized (DiskLruCache.this)
            {
              DiskLruCache.Editor.access$1902(DiskLruCache.Editor.this, true);
              return;
            }
          }
        };
        return localObject2;
      }
      catch (FileNotFoundException localFileNotFoundException)
      {
        Sink localSink = DiskLruCache.NULL_SINK;
        return localSink;
      }
    }

    public Source newSource(int paramInt)
      throws IOException
    {
      synchronized (DiskLruCache.this)
      {
        if (DiskLruCache.Entry.access$900(this.entry) != this)
          throw new IllegalStateException();
      }
      if (!DiskLruCache.Entry.access$800(this.entry))
        return null;
      try
      {
        Source localSource = DiskLruCache.this.fileSystem.source(DiskLruCache.Entry.access$1300(this.entry)[paramInt]);
        return localSource;
      }
      catch (FileNotFoundException localFileNotFoundException)
      {
      }
      return null;
    }
  }

  private final class Entry
  {
    private final File[] cleanFiles;
    private DiskLruCache.Editor currentEditor;
    private final File[] dirtyFiles;
    private final String key;
    private final long[] lengths;
    private boolean readable;
    private long sequenceNumber;

    private Entry(String arg2)
    {
      this.key = ((String)localObject);
      this.lengths = new long[DiskLruCache.this.valueCount];
      this.cleanFiles = new File[DiskLruCache.this.valueCount];
      this.dirtyFiles = new File[DiskLruCache.this.valueCount];
      Object localObject = new StringBuilder((String)localObject).append('.');
      int j = ((StringBuilder)localObject).length();
      int i = 0;
      while (i < DiskLruCache.this.valueCount)
      {
        ((StringBuilder)localObject).append(i);
        this.cleanFiles[i] = new File(DiskLruCache.this.directory, ((StringBuilder)localObject).toString());
        ((StringBuilder)localObject).append(".tmp");
        this.dirtyFiles[i] = new File(DiskLruCache.this.directory, ((StringBuilder)localObject).toString());
        ((StringBuilder)localObject).setLength(j);
        i += 1;
      }
    }

    private IOException invalidLengths(String[] paramArrayOfString)
      throws IOException
    {
      throw new IOException("unexpected journal line: " + Arrays.toString(paramArrayOfString));
    }

    private void setLengths(String[] paramArrayOfString)
      throws IOException
    {
      if (paramArrayOfString.length != DiskLruCache.this.valueCount)
        throw invalidLengths(paramArrayOfString);
      int i = 0;
      try
      {
        while (i < paramArrayOfString.length)
        {
          this.lengths[i] = Long.parseLong(paramArrayOfString[i]);
          i += 1;
        }
      }
      catch (NumberFormatException localNumberFormatException)
      {
        throw invalidLengths(paramArrayOfString);
      }
    }

    DiskLruCache.Snapshot snapshot()
    {
      if (!Thread.holdsLock(DiskLruCache.this))
        throw new AssertionError();
      Source[] arrayOfSource = new Source[DiskLruCache.this.valueCount];
      Object localObject = (long[])this.lengths.clone();
      int i = 0;
      try
      {
        while (i < DiskLruCache.this.valueCount)
        {
          arrayOfSource[i] = DiskLruCache.this.fileSystem.source(this.cleanFiles[i]);
          i += 1;
        }
        localObject = new DiskLruCache.Snapshot(DiskLruCache.this, this.key, this.sequenceNumber, arrayOfSource, (long[])localObject, null);
        return localObject;
      }
      catch (FileNotFoundException localFileNotFoundException)
      {
        i = 0;
        while ((i < DiskLruCache.this.valueCount) && (arrayOfSource[i] != null))
        {
          Util.closeQuietly(arrayOfSource[i]);
          i += 1;
        }
      }
      return null;
    }

    void writeLengths(BufferedSink paramBufferedSink)
      throws IOException
    {
      long[] arrayOfLong = this.lengths;
      int j = arrayOfLong.length;
      int i = 0;
      while (i < j)
      {
        long l = arrayOfLong[i];
        paramBufferedSink.writeByte(32).writeDecimalLong(l);
        i += 1;
      }
    }
  }

  public final class Snapshot
    implements Closeable
  {
    private final String key;
    private final long[] lengths;
    private final long sequenceNumber;
    private final Source[] sources;

    private Snapshot(String paramLong, long arg3, Source[] paramArrayOfLong, long[] arg6)
    {
      this.key = paramLong;
      this.sequenceNumber = ???;
      this.sources = paramArrayOfLong;
      Object localObject;
      this.lengths = localObject;
    }

    public void close()
    {
      Source[] arrayOfSource = this.sources;
      int j = arrayOfSource.length;
      int i = 0;
      while (i < j)
      {
        Util.closeQuietly(arrayOfSource[i]);
        i += 1;
      }
    }

    public DiskLruCache.Editor edit()
      throws IOException
    {
      return DiskLruCache.this.edit(this.key, this.sequenceNumber);
    }

    public long getLength(int paramInt)
    {
      return this.lengths[paramInt];
    }

    public Source getSource(int paramInt)
    {
      return this.sources[paramInt];
    }

    public String key()
    {
      return this.key;
    }
  }
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     com.squareup.okhttp.internal.DiskLruCache
 * JD-Core Version:    0.6.2
 */