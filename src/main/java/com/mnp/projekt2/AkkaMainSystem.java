package com.mnp.projekt2;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


/**
 * Akka-Hauptsystem, das die Produktion von elektronischen Bauteilen steuert.
 * Startet die Produktion mit dem Bauteil EB5 als Wurzel.
 */
public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Message> {

    /**
     * Nachrichten für den AkkaMainSystem-Actor.
     */
    public interface Message { }

    /**
     * Nachricht zum Beenden des Systems.
     */
    public static class Terminate implements Message {
    }

    /**
     * Erzeugt die Behavior-Instanz von AkkaMainSystem.
     */
    public static Behavior<Message> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    private AkkaMainSystem(ActorContext<Message> context) {
        super(context);
        // Produktionsstart für EB5
        getContext().getLog().info("Starte Produktion des Bauteils EB5.");
        // Starte den Builder-Actor für EB5
        context.spawnAnonymous(BuilderActor.create(ComponentType.EB5));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Terminate.class, this::onTerminate)
                .build();
    }

    private Behavior<Message> onTerminate(Terminate msg) {
        getContext().getLog().info("System wird beendet.");
        // System herunterfahren
        getContext().getSystem().terminate();
        return Behaviors.stopped();
    }
}
