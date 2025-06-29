package com.mnp.projekt2;

import akka.actor.typed.ActorSystem;

/**
 * Startet das Akka-System und initialisiert den AkkaMainSystem-Actor.
 */
public class AkkaStart {
  public static void main(String[] args) {
    // ActorSystem mit dem AkkaMainSystem-Actor erzeugen
    ActorSystem<AkkaMainSystem.Message> system =
            ActorSystem.create(AkkaMainSystem.create(), "akkaMainSystem");

    System.out.println(">>> Druecke ENTER zum Beenden <<<");
    try {
      System.in.read();
    } catch (Exception ignored) {
    }
    // Nachricht senden, um das System zu beenden
    system.tell(new AkkaMainSystem.Terminate());
  }
}
