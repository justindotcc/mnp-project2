/**
 * Isabelle Bille 156252
 * Justin Gottwald 201237
 * Ilia Orlov 251287
 */

package com.mnp.projekt2;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.ActorRef;

import java.util.List;
import java.util.Map;

// Actor responsible for constructing individual components (either basic or composed)
public class ComponentBuilder extends AbstractBehavior<ComponentBuilder.Command> {

    // Base interface for all commands that ComponentBuilder can process
    public interface Command {
    }

    // Message to trigger building of this component
    public record Build(ActorRef<Done> replyTo) implements Command {
    }

    // Message sent upon successful completion of this component
    public record Done(String name) implements Command {
    }

    // Name of the component to be built
    private final String componentName;

    // Actor to notify once this component is built
    private ActorRef<Done> replyTo;

    // Counter for how many subcomponents have finished
    private int received = 0;

    // Static blueprint map for how complex components are composed of subcomponents
    private static final Map<String, List<String>> PLANS = Map.of(
            "EB1", List.of("BB1", "BB2"),
            "EB2", List.of("EB1", "BB2"),
            "EB3", List.of("BB3", "EB2"),
            "EB4", List.of("EB1", "EB3"),
            "EB5", List.of("EB2", "EB4")
    );

    // Factory method to create a ComponentBuilder for a specific component name
    public static Behavior<Command> create(String componentName) {
        return Behaviors.setup(ctx -> new ComponentBuilder(ctx, componentName));
    }

    // Constructor initializes component name
    private ComponentBuilder(ActorContext<Command> ctx, String componentName) {
        super(ctx);
        this.componentName = componentName;
    }

    // Defines how this actor reacts to messages
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Build.class, this::onBuild)
                .onMessage(Done.class, this::onDone)
                .build();
    }

    // Handles the build request for this component
    private Behavior<Command> onBuild(Build msg) {
        this.replyTo = msg.replyTo;
        getContext().getLog().info("Start building {}", componentName);

        // If it's a basic building block (BB), complete immediately
        if (componentName.startsWith("BB")) {
            getContext().getLog().info("Finished building base component {}", componentName);
            replyTo.tell(new Done(componentName));
            return Behaviors.stopped();
        }

        // If it is a composed component, spawn builders for its two children
        var children = PLANS.get(componentName);
        var leftChild = getContext().spawnAnonymous(create(children.get(0)));
        var rightChild = getContext().spawnAnonymous(create(children.get(1)));

        // Adapter to forward Done messages as-is
        var adapter = getContext().messageAdapter(Done.class, d -> d);

        // Start building both subcomponents in parallel
        leftChild.tell(new Build(adapter));
        rightChild.tell(new Build(adapter));

        return this;
    }

    // Handles completion of a subcomponent
    private Behavior<Command> onDone(Done msg) {
        getContext().getLog().info("Component {}: child {} completed", componentName, msg.name);
        received++;

        // If both subcomponents are completed, report this component as done
        if (received == 2) {
            getContext().getLog().info("Finished building {}", componentName);
            replyTo.tell(new Done(componentName));
            return Behaviors.stopped();
        }

        return this;
    }
}
