package com.mnp.projekt2;

import akka.actor.typed.ActorSystem;

/** Hauptklasse: startet das ActorSystem und die Produktion. */
public class Main {
    public static void main(String[] args) {
        // ActorSystem mit dem BuilderActor als Root-Behavior
        ActorSystem<ComponentBuilder.PartBuilt> system =
                ActorSystem.create(BuilderActor.create(), "ProductionSystem");
        // Das System läuft bis zur Terminierung nach EB4-Fertigung.
    }
}
