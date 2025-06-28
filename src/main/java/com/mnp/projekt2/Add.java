package com.mnp.projekt2;

import akka.actor.typed.javadsl.TimerScheduler;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;

import java.util.Arrays;
import java.util.List;

public class Add extends AbstractBehavior<Add.Addition> {
    public interface Addition {};

    public record EvalAndWait(String left, String right) implements Addition {}
    public record EvalAndCheck(String left, String right) implements Addition {}
    public record Result(int result, String side) implements Addition {}
    public record AddResult() implements Addition {}
    public record PrintResult() implements Addition {}
    public record WaitForResult() implements Addition {}
    public record SetResults(int result, String side) implements Addition {}


    public static Behavior<Addition> create(){
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new Add(context, timers)) );
    }

    public int result;
    public List<Integer> results;
    private final TimerScheduler<Addition> timers;

    private Add(ActorContext<Addition> context, TimerScheduler<Addition> timers){
        super(context);
        this.timers = timers;
        this.result = 0;
        this.results = Arrays.asList(null, null);
    }

    @Override
    public Receive<Addition> createReceive() {
        return newReceiveBuilder()
                .onMessage(EvalAndWait.class, this::onEvalAndWait)
                .onMessage(EvalAndCheck.class, this::onEvalAndCheck)
                .onMessage(Result.class, this::onResult)
                .onMessage(PrintResult.class, this::onPrintResult)
                .onMessage(WaitForResult.class, this::onWaitForResult)
                .onMessage(SetResults.class, this::onSetResults)
                .onMessage(AddResult.class, this::onAddResult)
                .build();
    }

    private Behavior<Addition> onEvalAndWait(EvalAndWait msg){
        ActorRef<Val.Value> left = getContext().spawnAnonymous(Val.create("left"));
        ActorRef<Val.Value> right = getContext().spawnAnonymous(Val.create("right"));
        left.tell(new Val.Eval(msg.left));
        right.tell(new Val.Eval(msg.right));
        left.tell(new Val.Result(getContext().getSelf()));
        right.tell(new Val.Result(getContext().getSelf()));
        getContext().getSelf().tell(new WaitForResult());
        return this;
    }
    private Behavior<Addition> onResult(Result msg){
        this.result += msg.result;
        getContext().getLog().info("Calculating: {}", this.result);
        return this;
    }

    private Behavior<Addition> onPrintResult(PrintResult msg){
        getContext().getLog().info("Result: {}", this.result);
        return this;
    }

    private Behavior<Addition> onWaitForResult(WaitForResult msg){
        this.timers.startSingleTimer(new PrintResult(), Duration.ofSeconds(1));
        return this;
    }

    private Behavior<Addition> onEvalAndCheck(EvalAndCheck msg){
        ActorRef<Val.Value> left = getContext().spawnAnonymous(Val.create("left"));
        ActorRef<Val.Value> right = getContext().spawnAnonymous(Val.create("right"));
        left.tell(new Val.Eval(msg.left));
        right.tell(new Val.Eval(msg.right));
        left.tell(new Val.SetResults(getContext().getSelf()));
        right.tell(new Val.SetResults(getContext().getSelf()));
        getContext().getSelf().tell(new AddResult());
        return this;
    }

    private Behavior<Addition> onSetResults(SetResults msg){
        if (msg.side == "left" && this.results.get(0) == null){
            this.results.set(0, msg.result);
        } else if (msg.side == "right" && this.results.get(1) == null) {
            this.results.set(1, msg.result);
        }
        return this;
    }

    private Behavior<Addition> onAddResult(AddResult msg){
        if (this.results.get(0) == null || this.results.get(1) == null){
            getContext().getSelf().tell(new AddResult());
            return this;
        }
        int sum = this.results.stream().mapToInt(i -> i).sum();
        this.result = sum;
        getContext().getSelf().tell(new PrintResult());
        return this;
    }
}
