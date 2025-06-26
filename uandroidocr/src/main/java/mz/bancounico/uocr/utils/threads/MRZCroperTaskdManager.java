package mz.bancounico.uocr.utils.threads;

import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Frank Tan on 11/04/2016.
 * A Singleton Manager for managing the thread pool
 */
public class MRZCroperTaskdManager {

    private static MRZCroperTaskdManager sInstance = null;
    public static final int DEFAULT_THREAD_POOL_SIZE = 8;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    private final ExecutorService mExecutorService;
    private final BlockingQueue<Runnable> mTaskQueue;
    private List<Future> mrzTaskList;

    public final static int DETECTION_MAX_ATTEMPS = 8;

    private WeakReference<UiThreadCallback> uiThreadCallbackWeakReference;

    // The class is used as a singleton
    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        sInstance = new MRZCroperTaskdManager();
    }

    // Made constructor private to avoid the class being initiated from outside
    private MRZCroperTaskdManager() {
        // initialize a queue for the thread pool. New tasks will be added to this queue
        mTaskQueue = new LinkedBlockingQueue<Runnable>();

        Log.e(Util.LOG_TAG, "Available cores: " + NUMBER_OF_CORES);
        /*
            TODO: You can choose between a fixed sized thread pool and a dynamic sized pool
            TODO: Comment one and uncomment another to see the difference.
         */

        mrzTaskList = new ArrayList<>();
        //mExecutorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE, new BackgroundThreadFactory());
        mExecutorService = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES * 2, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mTaskQueue, new BackgroundThreadFactory());


    }

    public static MRZCroperTaskdManager getInstance() {
        return sInstance;
    }

    // Add a callable to the queue, which will be executed by the next available thread in the pool

    public Future addTask(Runnable runnable) throws InterruptedException {
        Future future = mExecutorService.submit(runnable);
        mrzTaskList.add(future);
        return future;
    }

    /* Remove all tasks in the queue and stop all running threads
     * Notify UI thread about the cancellation
     */
    public void cancelAllTasks() throws InterruptedException {

        synchronized (sInstance) {
            if (mrzTaskList.size() > 0) {
                for (int i = 0; i < mrzTaskList.size(); i++) {
                    Future task = null;
                        task = mrzTaskList.get(i);
                    if (task != null && !task.isCancelled())
                        task.cancel(true);
                }

                mrzTaskList.clear();
            }
        }
        // sendMessageToUiThread(Util.createMessage(Util.MESSAGE_SUCCESS, "All tasks in the thread pool are cancelled"));
    }

    public void removeTask(Future future){
        mrzTaskList.remove(future);
    }

    public int getTasksNumber(){
        return mrzTaskList.size();
    }


    // Keep a weak reference to the UI thread, so we can send messages to the UI thread

    public void setUiThreadCallback(UiThreadCallback uiThreadCallback) {
        this.uiThreadCallbackWeakReference = new WeakReference<UiThreadCallback>(uiThreadCallback);
    }

    // Pass the message to the UI thread
    public void sendMessageToUiThread(Message message) {
        if (uiThreadCallbackWeakReference != null && uiThreadCallbackWeakReference.get() != null) {
            uiThreadCallbackWeakReference.get().publishToUiThread(message);
        }
    }


    /* A ThreadFactory implementation which create new threads for the thread pool.
       The threads created is set to background priority, so it does not compete with the UI thread.
     */
    private static class BackgroundThreadFactory implements ThreadFactory {
        private static int sTag = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("CustomThread" + sTag);
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);

            // A exception handler is created to log the exception from threads
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Log.e(Util.LOG_TAG, thread.getName() + " encountered an error: " + ex.getMessage());
                }
            });
            return thread;
        }
    }
}
