/**
 * Isabelle Bille 156252
 * Justin Gottwald 201237
 * Ilia Orlov 251287
 */

package com.mnp.projekt2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

// Main system actor that initiates the production process
public class AkkaMainSystem extends AbstractBehavior<ComponentBuilder.Done> {

    // Reference to the controller actor that handles building components
    private final ActorRef<BuilderActor.Command> controller;

    // Factory method to create the main system behavior
    public static Behavior<ComponentBuilder.Done> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    // Constructor: initializes the controller actor and starts the production process
    private AkkaMainSystem(ActorContext<ComponentBuilder.Done> ctx) {
        super(ctx);
        // Spawn the controller anonymously to allow parallel dynamic actors
        controller = ctx.spawnAnonymous(BuilderActor.create());

        // Start the production
        startProduction();
    }

    // Sends messages to start production
    private void startProduction() {
        // Starts production of EB4 and EB5
        controller.tell(new BuilderActor.BuildComponent("EB4", getContext().getSelf()));
        controller.tell(new BuilderActor.BuildComponent("EB5", getContext().getSelf()));
    }

    // Defines how this actor reacts to incoming messages
    @Override
    public Receive<ComponentBuilder.Done> createReceive() {
        return newReceiveBuilder()
                // Handle completion messages from built components
                .onMessage(ComponentBuilder.Done.class, this::onDone)
                .build();
    }

    // Logs when a component production is completed
    private Behavior<ComponentBuilder.Done> onDone(ComponentBuilder.Done msg) {
        getContext().getLog().info("MainSystem: production of {} completed", msg.name());
        return this;
    }
}
