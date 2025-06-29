package com.mnp.projekt2;

import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


/**
 * Actor, der ein Bauteil produziert. Für jedes Teil wird ein eigener Actor
 * gestartet, der bei zusammengesetzten Bauteilen die Unterteile baut.
 */
public class BuilderActor extends AbstractBehavior<Void> {

    private final ComponentType type;
    private int childrenRemaining;

    /**
     * Erzeugt den Behavior für einen BuilderActor.
     *
     * @param type Das Bauteil, das produziert werden soll.
     * @return Die Behavior-Instanz des Actors.
     */
    public static Behavior<Void> create(ComponentType type) {
        return Behaviors.setup(context -> {
            // Wenn es sich um einen Basisbaustein handelt, wird direkt produziert und der Actor beendet sich.
            if (type.isBasePart()) {
                context.getLog().info("Bauteil {}: Produktionsstart.", type);
                context.getLog().info("Bauteil {}: Fertiggestellt.", type);
                return Behaviors.stopped();
            }
            // Ansonsten erstelle einen Composite-Builder-Actor.
            return new BuilderActor(context, type);
        });
    }

    private BuilderActor(ActorContext<Void> context, ComponentType type) {
        super(context);
        this.type = type;
        // Log: Produktionsstart des Bauteils
        getContext().getLog().info("Bauteil {}: Produktionsstart.", type);

        // Komponenten dieses Bauteils ermitteln und jeweils einen neuen Actor starten.
        ComponentType[] components = ComponentBuilder.getComponents(type);
        // Anzahl der zu wartenden Kinder
        this.childrenRemaining = components.length;

        // Gemäß Vorgabe immer zwei Komponenten pro Bauteil
        // Ersten Unterbauteil bauen (als eigener Actor)
        if (components.length > 0) {
            ActorRef<Void> child1 = context.spawnAnonymous(BuilderActor.create(components[0]));
            // Überwache das Ende des Kind-Actors
            context.watch(child1);
        }
        // Zweiten Unterbauteil bauen (als eigener Actor)
        if (components.length > 1) {
            ActorRef<Void> child2 = context.spawnAnonymous(BuilderActor.create(components[1]));
            context.watch(child2);
        }
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder()
                .onSignal(Terminated.class, signal -> {
                    childrenRemaining--;
                    if (childrenRemaining == 0) {
                        getContext().getLog().info("Bauteil {}: Fertiggestellt.", type);
                        return Behaviors.stopped();
                    }
                    return this;
                })
                .build();
    }

}

