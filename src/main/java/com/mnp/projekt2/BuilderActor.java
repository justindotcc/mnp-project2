package com.mnp.projekt2;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.ActorRef;

import java.util.HashMap;
import java.util.Map;

public class BuilderActor extends AbstractBehavior<BuilderActor.Command> {
    public interface Command extends ComponentBuilder.Command {
    }

    public record BuildComponent(
            String componentName,
            ActorRef<ComponentBuilder.Done> replyTo) implements Command {
    }

    public record WrappedDone(ComponentBuilder.Done done) implements Command {
    }

    private final Map<String, ActorRef<ComponentBuilder.Done>> pending = new HashMap<>();
    private final ActorRef<ComponentBuilder.Done> doneAdapter;

    public static Behavior<Command> create() {
        return Behaviors.setup(ctx -> new BuilderActor(ctx));
    }

    private BuilderActor(ActorContext<Command> ctx) {
        super(ctx);
        this.doneAdapter = ctx.messageAdapter(ComponentBuilder.Done.class, WrappedDone::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuildComponent.class, this::onBuildComponent)
                .onMessage(WrappedDone.class, this::onDone)
                .build();
    }

    private Behavior<Command> onBuildComponent(BuildComponent msg) {
        String name = msg.componentName;
        getContext().getLog().info("BuilderActor: building {}", name);
        pending.put(name, msg.replyTo);
        var builder = getContext().spawnAnonymous(ComponentBuilder.create(name));
        builder.tell(new ComponentBuilder.Build(doneAdapter));
        return this;
    }

    private Behavior<Command> onDone(WrappedDone msg) {
        String name = msg.done.name();
        getContext().getLog().info("BuilderActor: {} done", name);
        var original = pending.remove(name);
        if (original != null) {
            original.tell(msg.done);
        } else {
            getContext().getLog().warn("No pending request for {}", name);
        }
        return this;
    }
}