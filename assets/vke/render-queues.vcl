<render-queues>
    <render-queue name="deferred">
        <class name="com.vke.impl.rendering.queue.DeferredQueueExecutor"/>
        <type name="instancing"/>
    </render-queue>
    <render-queue name="forward+">
        <class name="com.vke.impl.rendering.queue.ClusteredQueueExecutor"/>
        <type name="instancing"/>
    </render-queue>
    <render-queue name="basic-text">
        <class name="com.vke.impl.rendering.queue.BasicTextExecutor"/>
        <type name="basic"/>
    </render-queue>
</render-queues>