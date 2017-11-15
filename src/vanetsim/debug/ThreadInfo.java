package vanetsim.debug;

import java.util.ArrayList;

/**
 * 該ThreadInfo定期檢查程式所有Thread的狀態
 * 於 2017/10/23_1232 新增
 */
public class ThreadInfo extends Thread{

    /**
     * /////////////////////////////////////
     * //      instance variable
     * /////////////////////////////////////
     */

    /**
     * 只允許產生單一ThreadInfo物件
     */
    private final static ThreadInfo INSTANCE = new ThreadInfo();
    /**
     * 利用ArrayList來儲存所有Thread的資訊
     */
    private static ArrayList<Thread> threadArrayList = null;

    /**
     * 監看執行緒狀態的頻率（毫秒為單位）
     */
    private long UPDATE_PERIOD = 1000;



    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */
    private ThreadInfo()
    {
        System.out.println("ThreadInfo() ------> ");
        threadArrayList = new ArrayList<Thread>();
    }

    public static ThreadInfo getInstance()
    {

        return INSTANCE;
    }

    /**
     * 將此Thread加入進行監督
     * @param thread  欲監督的thread
     * @return 是否成功被加入監督
     */
    public boolean addThreadＳupervise(Thread thread)
    {
        if(thread == null) return false;
        this.threadArrayList.add(thread);

        if(this.threadArrayList.contains(thread)) return true;
        return false;
    }

    /**
     * 設定更新頻率
     * @param period 更新時間（毫秒）
     */
    public void setUpdatePeriod(long period)
    {
        UPDATE_PERIOD = period;
    }

    private void showInfo()
    {
        System.out.println(".");
        System.out.println("//////////////////////////////////////////////////////////////////////////////////////");
        System.out.println("//                            Thread Info");
        System.out.println("//////////////////////////////////////////////////////////////////////////////////////");
        System.out.println("|  ThreadGroup   |     Name    |    State    |   priority   |   isAlive   |   isInterrupt");
        System.out.println("+--------------------------------------------------------------------------------------+");

        for(Thread thread : this.threadArrayList)
        {
            System.out.println(
                    "|   "
                    + thread.getThreadGroup().getClass().getName()
                    +"   |  "
                    + thread.getName()
                    +"  |  "
                    + thread.getState()
                    +"  |  "
                    + thread.getPriority()
                    +"  |  "
                    + thread.isAlive()
                    +"  |  "
                    + thread.isInterrupted()
                    +"  |  "
            );
            System.out.println("+-----------------------------------------------------------------------+");
        }

    }

    @Override
    public void run() {

        while(true)
        {
            try {

                Thread.sleep(UPDATE_PERIOD);
                showInfo();

            }catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
        }

    }
}
