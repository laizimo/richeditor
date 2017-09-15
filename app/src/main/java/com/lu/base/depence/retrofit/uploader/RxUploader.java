package com.lu.base.depence.retrofit.uploader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import com.lu.base.depence.retrofit.RetrofitClient;
import com.lu.base.depence.retrofit.uploader.api.Uploader;
import com.lu.base.depence.retrofit.uploader.utils.UploadHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 *  小文件上传（图片，json）没有断点续传
 * Created by 陆正威 on 2017/7/31.
 */
@SuppressWarnings("unused")
public enum RxUploader {
    INSTANCE;
    private int maxRunningNum = Integer.MAX_VALUE;
    private static Long DEFAULT_TASKS_ID = 0x0002331331L;
    private AtomicInteger runTaskNum;
    private ConcurrentLinkedQueue<UploadSingleTask> mPreparingTasks;
    private ConcurrentHashMap<String ,UploadSingleTask> mTasks;

    private Retrofit mRetrofit;
    private OkHttpClient mOkHttpClient;

    RxUploader(){
        mPreparingTasks = new ConcurrentLinkedQueue<>();
        mTasks = new ConcurrentHashMap<>();
        maxRunningNum = getOkHttpClient().dispatcher().getMaxRequests();
        runTaskNum = new AtomicInteger(0);
    }

    public static String MakeUUID(Long childId){
        return MakeUUID(null,childId);
    }

    public static String MakeUUID(Long parentId,Long id){
        parentId = (parentId== null ? DEFAULT_TASKS_ID : parentId);
        return parentId+"#"+id;
    }

    public TasksController upload(String url, long tasksId, String parName, String ...filePaths){
        ArrayList<TaskController> tasks = new ArrayList<>();
        long id = 0;
        int size = filePaths.length;
        for (String filePath :
                filePaths) {
            tasks.add(upload(url,parName, filePath,id++));
        }
        return new TasksController(tasksId,tasks);
    }

    public TasksController upload(String url, long tasksId, String ...filePaths){
        return upload(url, tasksId, UploadHelper.DEFAULT_FILE_KEY, filePaths);
    }

    public TaskController upload(String url, String filePath, Long id){
        return upload(url, filePath, UploadHelper.DEFAULT_FILE_KEY,id);
    }

    public TaskController upload(String url, File file, Long id){
        return upload(url,UploadHelper.DEFAULT_FILE_KEY, file,id);
    }

    public TaskController upload(String url, String parName, String filePath, Long id){
        return upload(url,UploadHelper.DEFAULT_MEDIA_TYPE,parName,new File(filePath),id);
    }

    public TaskController upload(String url, String mediaType, String parName, String filePath, Long id){
        return upload(url,mediaType,parName,new File(filePath),id);
    }

    public TaskController upload(String url, String parName, File file, Long id){
        return upload(url,UploadHelper.DEFAULT_MEDIA_TYPE,parName,file,id);
    }

    public TaskController upload(String url, String mediaType, String parName, File file, final Long id){
        UploadSingleTask singleTask;
        final String uuid = MakeUUID(id);
        TaskController taskController = new TaskController(DEFAULT_TASKS_ID,id,file.getPath());
        if(!mTasks.containsKey(uuid) && mTasks.size()< maxRunningNum) {
            singleTask = new UploadSingleTask(taskController,url, mediaType, file, parName);
            singleTask.setOnTaskAutoFinishedListener(new Uploader.OnTaskAutoFinishedListener() {
                @Override
                public void onTaskAutoFinished(TaskController taskController) {
                    runTaskNum.decrementAndGet();
                    mTasks.remove(uuid);
                    pull2UploadQueue();
                }
            });
            add2TaskQueue(singleTask);
        }
        return taskController;
    }
    //----------------------------------------------------------------------------------------------------------------------------------
    public @Nullable
    TaskController handle(Long id){
        final String uuid = MakeUUID(id);
        if(mTasks.containsKey(uuid))
            return mTasks.get(uuid).getTaskController();
        else
            return null;
    }

    public @Nullable
    TaskController handle(Long parentId, Long id){
        final String uuid = MakeUUID(parentId,id);
        if(mTasks.containsKey(uuid))
            return mTasks.get(uuid).getTaskController();
        else
            return null;
    }

   public @Nullable TaskController handleLatestPreparing(){
        if(!mPreparingTasks.isEmpty())
            return mPreparingTasks.peek().getTaskController();
        else
            return null;
    }

    public void clearPreparing(){
        mPreparingTasks.clear();
    }
    //------------------------------------------------------------------------------------------------------------------------------------

    public boolean checkTaskExited(Long id){
        return mTasks.containsKey(MakeUUID(id)) || isInPreparingQueue(new TaskController(DEFAULT_TASKS_ID,id,null));
    }

    public boolean checkTaskExited(Long parentId,Long id){
        return mTasks.containsKey(MakeUUID(parentId,id)) || isInPreparingQueue(new TaskController(parentId,id,null));
    }

    private Retrofit getDefaultRetrofit(){
        return RetrofitClient.getInstance().getRetrofit();
    }

    private OkHttpClient getDefaultOkHttpClient(){
        return RetrofitClient.getInstance().getOkHttpClient();
    }

    private Retrofit getRetrofit(){
        return mRetrofit == null ? getDefaultRetrofit() : mRetrofit;
    }

    private OkHttpClient getOkHttpClient(){
        return mOkHttpClient == null ? getDefaultOkHttpClient() : mOkHttpClient;
    }

    public synchronized void setRetrofit(Retrofit retrofit) {
        this.mRetrofit = retrofit;
    }

    public synchronized void setOkHttpClient(OkHttpClient okHttpClient) {
        this.mOkHttpClient = okHttpClient;
        int newMaxNum = okHttpClient.dispatcher().getMaxRequests();
    }

    private void add2TaskQueue(UploadSingleTask task){
        //上传队列已满
        if(runTaskNum.get() >= maxRunningNum){
            mPreparingTasks.add(task);
        }else {
            mTasks.put(task.getTaskController().getUUID(),task);
            runTaskNum.incrementAndGet();
            task.start();
        }
    }

    private void pull2UploadQueue(){
        if(!mPreparingTasks.isEmpty()){
            UploadSingleTask task = mPreparingTasks.poll();
            mTasks.put(task.getTaskController().getUUID(),task);
            runTaskNum.incrementAndGet();
            task.start();
        }
    }

    private boolean isInPreparingQueue(TaskController taskController){
        return mPreparingTasks.contains(UploadSingleTask.getFakeTask(taskController));
    }

    public class TasksController implements Uploader.UploadTaskInfo {
        private Long id;
        private ArrayList<TaskController> tasks;

        private TasksController(@NonNull Long id, ArrayList<TaskController> tasks){
            this.tasks = (tasks == null ? new ArrayList<TaskController>():tasks);
            this.id = id;
        }

        public TasksController startAll(){
            for (TaskController task:
                 tasks) {
                task.start();
            }
            return this;
        }

        public @Nullable
        TaskController start(Long id){
            TaskController task = getTask(id);
            if(task != null){
                return task.start();
            }
            return null;
        }

        public TasksController start(Long id, Uploader.TaskHandler handler){
            TaskController task = getTask(id);
            if(task != null){
                handler.handle(id,task.start());
            }else {
                handler.touchTaskFailed();
            }
            return this;
        }

        public void cancelAll(){
            for (TaskController task:
                    tasks) {
                task.cancel();
            }
        }

        public void removeAll(){
            for (TaskController task:
                    tasks) {
                task.remove();
            }
        }

        public boolean cancel(Long id) {
            TaskController task = getTask(id);
            return task != null && task.cancel();
        }

        public TasksController cancel(String filePath, Uploader.AfterOperationTaskHandler handler){
            TaskController task = getTask(id);
            if(task != null){
                if(task.cancel()){
                    handler.preHandleSuccess(id,task);
                }else {
                    handler.handle(id,task);
                }
                return this;
            }
            handler.touchTaskFailed();
            return this;
        }

        public boolean remove(Long id){
            TaskController task = getTask(id);
            return task != null && task.remove();
        }

        public TasksController remove(Long id, Uploader.AfterOperationTaskHandler handler){
            TaskController task = getTask(id);
            if(task != null){
                if(task.remove()){
                    handler.preHandleSuccess(id,task);
                }else {
                    handler.handle(id,task);
                }
                return this;
            }
            handler.touchTaskFailed();
            return this;
        }

        public TaskController get(Long childId){
            TaskController fake = new TaskController(id,childId,null);
            int index =  tasks.indexOf(fake);
            if(index != -1){
                fake = tasks.get(index);
            }
            return fake;
        }

        public TasksController each(Uploader.TaskHandler handler){
            if(tasks == null || tasks.isEmpty()) {
                handler.touchTaskFailed();
                return this;
            }
            for (TaskController task :
                    tasks) {
                handler.handle(id,task);
            }
            return this;
        }

        public Long getImageId() {
            return id;
        }

        private TaskController getTask(Long childId){
            TaskController fake = new TaskController(id,childId,null);
            int index =  tasks.indexOf(fake);
            if(index != -1){
                return tasks.get(index);
            }
            return null;
        }

        @Override
        public String getUUID() {
            return String.valueOf(id);
        }
    }

    public class TaskController implements Uploader.UploadTaskInfo {
        private Long parentId;
        private Long id;
        private String filePath;

        private TaskController(Long parentId , Long id, String filePath){
            this.parentId = parentId;
            this.id = id;
            this.filePath = filePath;
        }

        public TaskController start(){
            if(isPreparing()) {
                pull2UploadQueue();
            }else if(mTasks.containsKey(getUUID())){
                mTasks.get(getUUID()).start();
            }
            return this;
        }

        public String getFilePath() {
            return filePath;
        }

        public boolean isRunning(){
            return mTasks.containsKey(getUUID()) && mTasks.get(getUUID()).isRunning();
        }

        public boolean isPreparing(){
            return isInPreparingQueue(this);
        }

        public boolean remove(){
            if(isPreparing()){
                mPreparingTasks.remove(UploadSingleTask.getFakeTask(this));
                return true;
            }else {
                return cancel();
            }
        }

        public boolean cancel(){
            if(isRunning()) {
                mTasks.get(getUUID()).cancel();
                mTasks.remove(getUUID());
                pull2UploadQueue();
                return true;
            }
            return false;
        }

        public void receiveEvent(Uploader.OnUploadListener listener){
            int num = 0;
            Set<Class> set = UploadHelper.getAllInterceptorClasses(getOkHttpClient());
            if(!set.isEmpty()){
                if(set.contains(HttpLoggingInterceptor.class))
                    num ++;
            }
            receiveEvent(listener,num);
        }

        public void receiveEvent(Uploader.OnUploadListener listener, int filterNum){
            if(mTasks.containsKey(getUUID())|| isInPreparingQueue(this)){
                mTasks.get(getUUID()).receiveEvent(listener,filterNum);
            }
        }

        public void stopReceive(){
            if(mTasks.containsKey(getUUID()) || isInPreparingQueue(this)){
                mTasks.get(getUUID()).stopReceiveEvent();
                mTasks.get(getUUID()).setOnTaskAutoFinishedListener(null);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TaskController task = (TaskController) o;

            return parentId.equals(task.parentId) && id.equals(task.id);

        }

        @Override
        public int hashCode() {
            int result = parentId.hashCode();
            result = 31 * result + id.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "TaskController{" +
                    "id=" + id +
                    ", filePath='" + filePath + '\'' +
                    '}';
        }

        @Override
        public String getUUID() {
            return parentId + "#" + id;
        }
    }

}
