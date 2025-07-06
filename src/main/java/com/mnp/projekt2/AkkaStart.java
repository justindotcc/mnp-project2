/**
 * Isabelle Bille 156252
 * Justin Gottwald 201237
 * Ilia Orlov 251287
 */

package com.mnp.projekt2;

import akka.actor.typed.ActorSystem;

/**
 * Entry point of the application.
 * Initializes the Akka actor system and starts the main coordination actor.
 */
public class AkkaStart {
  public static void main(String[] args) {
    // Create the actor system and start the top-level actor responsible for managing production
    ActorSystem<ComponentBuilder.Done> system = ActorSystem.create(AkkaMainSystem.create(), "akkaMainSystem");

    // Keep the application running until user input is received
    System.out.println(">>> Press ENTER to terminate <<<");
    try {
      System.in.read();
    } catch (Exception ignored) {
    }

    // Shutdown the actor system after input
    system.terminate();
  }
}
