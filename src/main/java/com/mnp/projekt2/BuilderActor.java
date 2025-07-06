/**
 * Isabelle Bille 156252
 * Justin Gottwald 201237
 * Ilia Orlov 251287
 */

package com.mnp.projekt2;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.ActorRef;

import java.util.HashMap;
import java.util.Map;

// Actor responsible for managing the building process of components
public class BuilderActor extends AbstractBehavior<BuilderActor.Command> {

    // Interface for all messages that BuilderActor can handle
    public interface Command extends ComponentBuilder.Command {
    }

    // Command to trigger the building of a component
    public record BuildComponent(
            String componentName,
            ActorRef<ComponentBuilder.Done> replyTo) implements Command {
    }

    // Internal message wrapper to handle completion messages from child builders
    public record WrappedDone(ComponentBuilder.Done done) implements Command {
    }

    // Keeps track of which component build requests are still pending
    private final Map<String, ActorRef<ComponentBuilder.Done>> pending = new HashMap<>();

    // Adapter to convert Done messages from ComponentBuilder to WrappedDone
    private final ActorRef<ComponentBuilder.Done> doneAdapter;

    // Factory method to create a BuilderActor behavior
    public static Behavior<Command> create() {
        return Behaviors.setup(ctx -> new BuilderActor(ctx));
    }

    // Constructor initializes the message adapter for handling Done messages
    private BuilderActor(ActorContext<Command> ctx) {
        super(ctx);
        this.doneAdapter = ctx.messageAdapter(ComponentBuilder.Done.class, WrappedDone::new);
    }

    // Define how this actor reacts to different incoming messages
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuildComponent.class, this::onBuildComponent) // handle build requests
                .onMessage(WrappedDone.class, this::onDone)              // handle completed builds
                .build();
    }

    // Handles a request to build a component
    private Behavior<Command> onBuildComponent(BuildComponent msg) {
        String name = msg.componentName;
        getContext().getLog().info("BuilderActor: building {}", name);

        // Store reply target to notify when component is done
        pending.put(name, msg.replyTo);

        // Spawn a new anonymous actor to build the component in parallel
        var builder = getContext().spawnAnonymous(ComponentBuilder.create(name));

        // Send build instruction with the reply adapter
        builder.tell(new ComponentBuilder.Build(doneAdapter));
        return this;
    }

    // Handles the completion message from a component builder
    private Behavior<Command> onDone(WrappedDone msg) {
        String name = msg.done.name();
        getContext().getLog().info("BuilderActor: {} done", name);

        // Notify the original requester if available
        var original = pending.remove(name);
        if (original != null) {
            original.tell(msg.done);
        } else {
            getContext().getLog().warn("No pending request for {}", name);
        }
        return this;
    }
}
