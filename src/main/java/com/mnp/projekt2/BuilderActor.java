package com.mnp.projekt2;

import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.Behaviors;
import com.mnp.projekt2.ComponentBuilder.PartBuilt;

/**
 * Start-Aktor der Produktion.
 */
public class BuilderActor {

    public static Behavior<PartBuilt> create() {
        return Behaviors.setup(context -> {
            ActorRef<PartBuilt> self = context.getSelf();
            context.spawnAnonymous(ComponentBuilder.create(ComponentType.EB4, self));

            return Behaviors.receive(ComponentBuilder.PartBuilt.class)
                    .onMessage(ComponentBuilder.PartBuilt.class, msg -> {
                        context.getLog().info("Produktion abgeschlossen: {}", msg.component);
                        context.getSystem().terminate();
                        return Behaviors.stopped();
                    })
                    .build();
        });
    }
}
