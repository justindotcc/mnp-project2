package com.mnp.projekt2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.Map;
import java.util.List;

/**
 * Actor responsible for building a single electronic component (EBx or BBx).
 * - Base components (BBx) complete immediately.
 * - Composite components (EBx) trigger parallel building of their two required subcomponents.
 */
public class ComponentBuilder extends AbstractBehavior<ComponentBuilder.Message> {

    // Marker interface for all messages
    public interface Message {}

    /**
     * Trigger message to begin construction of this component.
     */
    public static class Start implements Message {}

    /**
     * Message sent back to parent once this component is completely built.
     */
    public static class Done implements Message {
        private final String name;
        public Done(String name) { this.name = name; }
        public String name() { return name; }
    }

    private final String componentName;             // The name of this component
    private final ActorRef<Done> replyTo;           // Recipient to notify when done
    private int received = 0;                       // Tracks how many child components are completed

    // Static build plan mapping: defines which two components each EBx depends on
    private static final Map<String, List<String>> PLANS = Map.of(
            "EB1", List.of("BB1", "BB2"),
            "EB2", List.of("EB1", "BB2"),
            "EB3", List.of("BB3", "EB2"),
            "EB4", List.of("EB1", "EB3"),
            "EB5", List.of("EB2", "EB4")
    );

    /**
     * Factory method to create a new ComponentBuilder actor for a given component.
     *
     * @param name     name of the component to build
     * @param replyTo  actor to notify once this component is finished
     */
    public static Behavior<Message> create(String name, ActorRef<Done> replyTo) {
        return Behaviors.setup(ctx -> new ComponentBuilder(ctx, name, replyTo));
    }

    /**
     * Private constructor used by the behavior factory.
     */
    private ComponentBuilder(ActorContext<Message> context, String name, ActorRef<Done> replyTo) {
        super(context);
        this.componentName = name;
        this.replyTo = replyTo;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Start.class, msg -> onStart())
                .onMessage(Done.class, this::onChildDone)
                .build();
    }

    /**
     * Starts the building process.
     * - If it's a base component (BBx), it's considered finished instantly.
     * - Otherwise, the required subcomponents are built in parallel.
     */
    private Behavior<Message> onStart() {
        getContext().getLog().info("Start building {}", componentName);

        // Base components: complete immediately
        if (componentName.startsWith("BB")) {
            getContext().getLog().info("Finished building base component {}", componentName);
            replyTo.tell(new Done(componentName));
            return Behaviors.stopped();
        }

        // Composite components: build subcomponents from plan
        var children = PLANS.get(componentName);
        if (children == null || children.size() != 2) {
            getContext().getLog().warn("Invalid plan for {}", componentName);
            replyTo.tell(new Done(componentName));
            return Behaviors.stopped();
        }

        // Build subcomponents in parallel, receive their Done replies
        ActorRef<Done> selfAdapter = getContext().messageAdapter(Done.class, d -> d);

        getContext().spawnAnonymous(ComponentBuilder.create(children.get(0), selfAdapter)).tell(new Start());
        getContext().spawnAnonymous(ComponentBuilder.create(children.get(1), selfAdapter)).tell(new Start());

        return this;
    }

    /**
     * Handles messages from child builders indicating their completion.
     * Once both children are done, this component is also marked as finished.
     */
    private Behavior<Message> onChildDone(Done msg) {
        getContext().getLog().info("Component {}: child {} completed", componentName, msg.name());
        received++;

        if (received == 2) {
            getContext().getLog().info("Finished building {}", componentName);
            replyTo.tell(new Done(componentName));
            return Behaviors.stopped();
        }

        return this;
    }
}
