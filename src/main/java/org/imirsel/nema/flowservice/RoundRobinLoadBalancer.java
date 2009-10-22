package org.imirsel.nema.flowservice;

import net.jcip.annotations.ThreadSafe;


/**
 * A {@link MeandreLoadBalancer} implementation that uses a round robin
 * algorithm to deal out servers. Implemented as a circular doubly linked list.
 *
 * @author shirk
 * @since 1.0
 */
@ThreadSafe
public class RoundRobinLoadBalancer implements MeandreLoadBalancer {

   //~ Instance fields ---------------------------------------------------------

   /** Pointer to the current node the load balancer is on. */
   Node curr = null;

   /** Number of servers the load balancer is managing. */
   int size = 0;

   //~ Constructors ------------------------------------------------------------

   /**
    * Creates a new {@link RoundRobinLoadBalancer} instance.
    */
   public RoundRobinLoadBalancer() {
   }

   //~ Methods -----------------------------------------------------------------

   /**
    * @see org.imirsel.nema.flowservice.MeandreLoadBalancer#addServer(org.imirsel.nema.flowservice.MeandreServer)
    */
   public synchronized void addServer(MeandreServer server) {
	   // do not allow duplicate servers
	  if(findNodeWithServer(server)!=null) {
		  return;
	  }
      Node node = new Node(server);
      if (size == 0) {
         node.setNext(node);
         node.setPrev(node);
         curr = node;
      } else {
         node.setNext(curr.getNext());
         curr.getNext()
         .setPrev(node);
         node.setPrev(curr);
         curr.setNext(node);
      }
      size++;
   }

   /**
    * @see org.imirsel.nema.flowservice.MeandreLoadBalancer#removeServer(org.imirsel.nema.flowservice.MeandreServer)
    */
   public synchronized void removeServer(MeandreServer server)
         throws IllegalStateException {
      if (size == 0) {
         return;
      }

      Node match = findNodeWithServer(server);

      if (match == null) {
         return;
      }

      if (server.getNumJobsRunning() != 0) {
         throw new IllegalStateException(
            "Server " +
            server.getHost() +
            ":" + server.getPort() +
            " cannot be removed from the scheduler because it is currently running a job.");
      }

      if (size == 1) {
         curr = null;
      } else {
         if (curr == match) {
            curr = curr.getNext();
         }
         match.getNext().setPrev(match.getPrev());
         match.getPrev().setNext(match.getNext());
      }
      size--;
   }

   /**
    * Finds the list {@link Node} that contains the specified
    * {@link MeandreServer}.
    *
    * @param server The {@link MeandreServer} to search for in the list
    * {@link Node}s.
    * @return List {@link Node} that contains the specified
    * {@link MeandreServer}.
    */
   private Node findNodeWithServer(MeandreServer server) {
      if (size == 0) {
         return null;
      }

      Node headNode = curr;
      Node iterNode = curr;

      do {
         if (iterNode.getServer().equals(server)) {
            return iterNode;
         }
         iterNode = iterNode.getNext();

      } while (!iterNode.equals(headNode));

      return null;
   }

   /**
    * @see org.imirsel.nema.flowservice.MeandreLoadBalancer#nextAvailableServer()
    */
   @Override
   public synchronized MeandreServer nextAvailableServer() {
      if (size == 0) {
         return null;
      }

      assert curr != null : "Current node should not be null";

      Node headNode = curr.getNext();
      Node iterNode = headNode;

      do {
         if (!iterNode.getServer().isBusy()) {
            curr = iterNode;
            return iterNode.getServer();
         }
         iterNode = iterNode.getNext();

      } while (!iterNode.equals(headNode));

      return null;
   }

   /**
    * @see org.imirsel.nema.flowservice.MeandreLoadBalancer#nextAvailableServer()
    */
   @Override
   public synchronized boolean hasAvailableServer() {
      if (size == 0) {
         return false;
      }

      assert curr != null : "Current node should not be null";

      Node headNode = curr.getNext();
      Node iterNode = headNode;

      do {
         if (!iterNode.getServer().isBusy()) {
            return true;
         }
         iterNode = iterNode.getNext();

      } while (!iterNode.equals(headNode));

      return false;
   }
   
   /**
    * Return the number of servers this load balancer is managing.
    */
   public synchronized int size() {
	   return size;
   }
   
   //~ Inner Classes -----------------------------------------------------------

   /**
    * Doubly linked list node.
    */
   private class Node {
      Node next = null;
      Node prev = null;
      MeandreServer server = null;

      public Node(MeandreServer server) { this.server = server; }

      public void setNext(Node next) { this.next = next; }

      public Node getNext() { return next; }

      public void setPrev(Node prev) { this.prev = prev; }

      public Node getPrev() { return prev; }

      public MeandreServer getServer() { return server; }

      @Override
      public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = (prime * result) + ((server == null) ? 0 : server.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         }
         if (obj == null) {
            return false;
         }
         if (getClass() != obj.getClass()) {
            return false;
         }

         final Node other = (Node) obj;
         if (server == null) {
            if (other.server != null) {
               return false;
            }
         } else if (!server.equals(other.server)) {
            return false;
         }
         return true;
      }

   }

}
