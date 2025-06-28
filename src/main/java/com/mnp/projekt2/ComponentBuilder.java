package com.mnp.projekt2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

import java.util.Map;
import java.util.List;

/**
 * Actor zum parallelen Bauen von Komponenten (BB, EB).
 */
public class ComponentBuilder {

    public static class PartBuilt {
        public final ComponentType component;
        public PartBuilt(ComponentType component) {
            this.component = component;
        }
    }

    private static final Map<ComponentType, List<ComponentType>> BLUEPRINT =
            Map.of(
                    ComponentType.EB1, List.of(ComponentType.BB1, ComponentType.BB2),
                    ComponentType.EB2, List.of(ComponentType.EB1, ComponentType.BB2),
                    ComponentType.EB3, List.of(ComponentType.BB3, ComponentType.EB2),
                    ComponentType.EB4, List.of(ComponentType.EB1, ComponentType.EB3),
                    ComponentType.EB5, List.of(ComponentType.EB4, ComponentType.EB1)
            );

    public static Behavior<PartBuilt> create(ComponentType component, ActorRef<PartBuilt> replyTo) {
        return Behaviors.setup(context -> {
            if (!BLUEPRINT.containsKey(component)) {
                context.getLog().info("Bauteil {} fertiggestellt", component);
                replyTo.tell(new PartBuilt(component));
                return Behaviors.stopped();
            }

            List<ComponentType> parts = BLUEPRINT.get(component);
            ComponentType part1 = parts.get(0);
            ComponentType part2 = parts.get(1);

            context.spawnAnonymous(ComponentBuilder.create(part1, context.getSelf()));

            return Behaviors.receive(ComponentBuilder.PartBuilt.class)
                    .onMessage(ComponentBuilder.PartBuilt.class, msg1 -> {
                        context.spawnAnonymous(ComponentBuilder.create(part2, context.getSelf()));
                        return Behaviors.receive(ComponentBuilder.PartBuilt.class)
                                .onMessage(ComponentBuilder.PartBuilt.class, msg2 -> {
                                    context.getLog().info("Bauteil {} fertiggestellt", component);
                                    replyTo.tell(new PartBuilt(component));
                                    return Behaviors.stopped();
                                })
                                .build();
                    })
                    .build();
        });
    }
}
