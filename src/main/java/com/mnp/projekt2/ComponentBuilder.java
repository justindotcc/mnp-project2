package com.mnp.projekt2;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.ActorRef;

import java.util.List;
import java.util.Map;

public class ComponentBuilder extends AbstractBehavior<ComponentBuilder.Command> {
    public interface Command {
    }

    public record Build(ActorRef<Done> replyTo) implements Command {
    }

    public record Done(String name) implements Command {
    }

    private final String componentName;
    private ActorRef<Done> replyTo;
    private int received = 0;

    private static final Map<String, List<String>> PLANS = Map.of(
            "EB1", List.of("BB1", "BB2"),
            "EB2", List.of("EB1", "BB2"),
            "EB3", List.of("BB3", "EB2"),
            "EB4", List.of("EB1", "EB3"),
            "EB5", List.of("EB2", "EB4"));

    public static Behavior<Command> create(String componentName) {
        return Behaviors.setup(ctx -> new ComponentBuilder(ctx, componentName));
    }

    private ComponentBuilder(ActorContext<Command> ctx, String componentName) {
        super(ctx);
        this.componentName = componentName;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Build.class, this::onBuild)
                .onMessage(Done.class, this::onDone)
                .build();
    }

    private Behavior<Command> onBuild(Build msg) {
        this.replyTo = msg.replyTo;
        getContext().getLog().info("Start building {}", componentName);

        // Base component: finish immediately
        if (componentName.startsWith("BB")) {
            getContext().getLog().info("Finished building base component {}", componentName);
            replyTo.tell(new Done(componentName));
            return Behaviors.stopped();
        }

        // Composite: build two children
        var children = PLANS.get(componentName);
        var leftChild = getContext().spawnAnonymous(create(children.get(0)));
        var rightChild = getContext().spawnAnonymous(create(children.get(1)));

        var adapter = getContext().messageAdapter(Done.class, d -> d);
        leftChild.tell(new Build(adapter));
        rightChild.tell(new Build(adapter));
        return this;
    }

    private Behavior<Command> onDone(Done msg) {
        getContext().getLog().info("Component {}: child {} completed", componentName, msg.name);
        received++;
        if (received == 2) {
            getContext().getLog().info("Finished building {}", componentName);
            replyTo.tell(new Done(componentName));
            return Behaviors.stopped();
        }
        return this;
    }
}