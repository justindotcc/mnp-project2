package com.mnp.p2;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Create> {

    public static class Create {
    }

    public static Behavior<Create> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    private AkkaMainSystem(ActorContext<Create> context) {
        super(context);
    }

    @Override
    public Receive<Create> createReceive() {
        return newReceiveBuilder().onMessage(Create.class, this::onCreate).build();
    }

    private Behavior<Create> onCreate(Create command) {
        var addition = this.getContext().spawn(Add.create(), "Addition");
        addition.tell(new Add.EvalAndWait("3", "5"));

        var addition_check = this.getContext().spawn(Add.create(), "AdditionCheck");
        addition_check.tell(new Add.EvalAndCheck("4", "6"));

        return this;
    }
}