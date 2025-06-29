package com.mnp.projekt2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

/**
 * Top-level actor of the system.
 * Starts the production process and receives completion messages from built components.
 */
public class AkkaMainSystem extends AbstractBehavior<ComponentBuilder.Done> {

    // ActorRef to the internal BuilderActor, which handles sequential production.
    private final ActorRef<BuilderActor.Command> controller;

    /**
     * Creates the behavior for the main system actor.
     */
    public static Behavior<ComponentBuilder.Done> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    /**
     * Constructor initializes the builder controller and starts production.
     */
    private AkkaMainSystem(ActorContext<ComponentBuilder.Done> ctx) {
        super(ctx);
        controller = ctx.spawnAnonymous(BuilderActor.create());
        startProduction();
    }

    /**
     * Starts the production of components.
     * The order of messages here determines the build sequence.
     */
    private void startProduction() {
        controller.tell(new BuilderActor.BuildComponent("EB4", getContext().getSelf()));
        controller.tell(new BuilderActor.BuildComponent("EB5", getContext().getSelf()));
    }

    /**
     * Defines how this actor handles incoming messages.
     */
    @Override
    public Receive<ComponentBuilder.Done> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComponentBuilder.Done.class, this::onDone)
                .build();
    }

    /**
     * Handles notification that a component has been completely built.
     */
    private Behavior<ComponentBuilder.Done> onDone(ComponentBuilder.Done msg) {
        getContext().getLog().info("MainSystem: production of {} completed", msg.name());
        return this;
    }
}
