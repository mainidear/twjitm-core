package com.twjitm.core.common.factory.thread;

import com.twjitm.core.common.config.global.GlobalConstants;
import com.twjitm.core.common.config.rpc.RpcSystemConfig;
import com.twjitm.core.common.enums.queue.BlockingQueueType;
import com.twjitm.core.common.factory.thread.policy.*;
import com.twjitm.core.utils.logs.LoggerUtils;
import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: 文江 Date: 2018/8/19  Time: 10:40
 * https://blog.csdn.net/baidu_23086307
 */
public class RpcHandlerThreadPoolFactory {
    private final Logger logger = LoggerUtils.getLogger(RpcHandlerThreadPoolFactory.class);
    private ExecutorService executor;

    /**
     * 线程决策。。。。。。。。。。
     *
     * @return
     * @TODO 需要深入了解一下
     */

    private RejectedExecutionHandler createPolicy() {
        RejectedPolicyType rejectedPolicyType = RejectedPolicyType.fromString(System.getProperty(RpcSystemConfig.SystemPropertyThreadPoolRejectedPolicyAttr, "CallerRunsPolicy"));

        switch (rejectedPolicyType) {
            case BLOCKING_POLICY:
                return new BlockingPolicy();
            case CALLER_RUNS_POLICY:
                return new CallerRunsPolicy();
            case ABORT_POLICY:
                return new AbortPolicy();
            case DISCARD_POLICY:
                return new DiscardPolicy();
            case DISCARD_OLDEST_POLICY:
                return new DiscardOldestPolicy();
        }

        logger.error("ERROR TYPE RejectedExecutionHandler createPolicy() NO HAVE TYPE");
        return null;
    }

    private BlockingQueue<Runnable> createBlockingQueue(int queues) {
        BlockingQueueType queueType = BlockingQueueType.
                fromString(System.getProperty(RpcSystemConfig.SystemPropertyThreadPoolQueueNameAttr,
                        "LinkedBlockingQueue"));
        switch (queueType) {
            case SYNCHRONOUS_QUEUE:
                return new SynchronousQueue<>();
            case LINKED_BLOCKING_QUEUE:
                return new LinkedBlockingQueue<>();
            case ARRAY_BLOCKING_QUEUE:
                return new ArrayBlockingQueue<>(RpcSystemConfig.PARALLEL * queues);
        }
        logger.error("ERROR TYPE RejectedExecutionHandler createBlockingQueue() NO HAVE QUEUE");

        return null;
    }

    public Executor createExecutor(int threads, int queues) {
        String name = GlobalConstants.Thread.RPC_HANDLER;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(threads, threads,
                0, TimeUnit.MILLISECONDS, createBlockingQueue(queues),
                new ThreadNameFactory(name, false), createPolicy());
        this.executor = executor;
        return executor;
    }

    public ExecutorService getExecutor() {
        return this.executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

}