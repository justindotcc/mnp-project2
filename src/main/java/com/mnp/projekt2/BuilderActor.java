package com.mnp.projekt2;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.*;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * BuilderActor receives top-level component build requests (e.g., EB4, EB5, etc.).
 * It ensures that only one component is built at a time, but each component's
 * internal structure is built in parallel via ComponentBuilder actors.
 */
public class BuilderActor extends AbstractBehavior<BuilderActor.Command> {

    // --- Supported messages ---
    public interface Command {}

    /**
     * Request to build a named component.
     * @param componentName the name of the component to build (e.g. EB4)
     * @param replyTo       the actor to notify when the build is complete
     */
    public record BuildComponent(String componentName, ActorRef<ComponentBuilder.Done> replyTo) implements Command {}

    /**
     * Internal message used to signal that a component has been built.
     */
    public record Done(ComponentBuilder.Done done) implements Command {}

    // --- Internal state ---
    private final ActorRef<ComponentBuilder.Done> doneAdapter; // adapts Done -> BuilderActor.Done
    private final Queue<BuildComponent> queue = new ArrayDeque<>(); // FIFO queue for requested components
    private boolean isBuilding = false; // true if one component is currently being built

    /**
     * Creates the BuilderActor behavior.
     */
    public static Behavior<Command> create() {
        return Behaviors.setup(BuilderActor::new);
    }

    /**
     * Constructor sets up the message adapter for Done messages from ComponentBuilder.
     */
    private BuilderActor(ActorContext<Command> context) {
        super(context);
        this.doneAdapter = context.messageAdapter(ComponentBuilder.Done.class, Done::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuildComponent.class, this::onBuildComponent)
                .onMessage(Done.class, this::onDone)
                .build();
    }

    /**
     * Handles a new build request. Adds it to the queue and triggers the next build if idle.
     */
    private Behavior<Command> onBuildComponent(BuildComponent msg) {
        queue.add(msg);
        tryNextBuild();
        return this;
    }

    /**
     * Called when a component is finished. Sends Done to original requester and starts next build if available.
     */
    private Behavior<Command> onDone(Done msg) {
        getContext().getLog().info("BuilderActor: {} done", msg.done.name());
        isBuilding = false;

        // Notify the original actor that requested the build
        BuildComponent justBuilt = queue.poll();
        if (justBuilt != null) {
            justBuilt.replyTo().tell(msg.done());
        }

        tryNextBuild();
        return this;
    }

    /**
     * Starts the next component from the queue, if no component is currently being built.
     */
    private void tryNextBuild() {
        if (!isBuilding && !queue.isEmpty()) {
            BuildComponent next = queue.peek();
            getContext().getLog().info("BuilderActor: building {}", next.componentName());

            // Start a new ComponentBuilder actor for this component
            var builder = getContext().spawnAnonymous(
                    ComponentBuilder.create(next.componentName(), doneAdapter)
            );
            builder.tell(new ComponentBuilder.Start());

            isBuilding = true;
        }
    }
}
