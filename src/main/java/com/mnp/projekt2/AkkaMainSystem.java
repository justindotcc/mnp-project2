package com.mnp.projekt2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class AkkaMainSystem extends AbstractBehavior<ComponentBuilder.Done> {
    private final ActorRef<BuilderActor.Command> controller;

    public static Behavior<ComponentBuilder.Done> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    private AkkaMainSystem(ActorContext<ComponentBuilder.Done> ctx) {
        super(ctx);
        controller = ctx.spawnAnonymous(BuilderActor.create());
        startProduction();
    }

    private void startProduction() {
        controller.tell(new BuilderActor.BuildComponent("EB4", getContext().getSelf()));
        controller.tell(new BuilderActor.BuildComponent("EB5", getContext().getSelf()));
    }

    @Override
    public Receive<ComponentBuilder.Done> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComponentBuilder.Done.class, this::onDone)
                .build();
    }

    private Behavior<ComponentBuilder.Done> onDone(ComponentBuilder.Done msg) {
        getContext().getLog().info("MainSystem: production of {} completed", msg.name());
        return this;
    }
}