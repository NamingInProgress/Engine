<render-queues>
    <render-queue name="deferred">
        <class name="com.vke.impl.rendering.queue.DeferredQueueExecutor"/>
    </render-queue>
    <render-queue name="forward+">
        <class name="com.vke.impl.rendering.queue.ClusteredQueueExecutor"/>
    </render-queue>
</render-queues>