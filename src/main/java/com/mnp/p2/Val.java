package com.mnp.p2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Val extends AbstractBehavior<Val.Value> {
    public interface Value {};

    public record Eval(String expr) implements Value {}
    public record Result(ActorRef<Add.Addition> replyTo) implements Value{}
    public record SetResults(ActorRef<Add.Addition> replyTo) implements Value{}


    public static Behavior<Value> create (String termSide){
        return Behaviors.setup(context -> new Val(context, termSide));
    }
    public int result;
    public final String termside;

    private Val(ActorContext<Value> context, String termSide){
        super(context);
        this.termside = termSide;
    }

    public Receive<Value> createReceive(){
        return newReceiveBuilder()
                .onMessage(Eval.class, this::onEval)
                .onMessage(Result.class, this::onResult)
                .onMessage(SetResults.class, this::onSetResults)
                .build();
    }

    private Behavior<Value> onEval(Eval msg){
        this.result = Integer.parseInt(msg.expr);
        getContext().getLog().info("Val: {}", this.result);
        return this;
    }

    private Behavior<Value> onResult(Result msg){
        int result = this.result;
        String side = this.termside;
        msg.replyTo.tell(new Add.Result(result, side));
        return this;
    }

    private Behavior<Value> onSetResults(SetResults msg){
        int result = this.result;
        String side = this.termside;
        msg.replyTo.tell(new Add.SetResults(result, side));
        return this;
    }

}
