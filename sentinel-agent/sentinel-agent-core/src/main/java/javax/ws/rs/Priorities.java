package javax.ws.rs;

public final class Priorities {
   public static final int AUTHENTICATION = 1000;
   public static final int AUTHORIZATION = 2000;
   public static final int HEADER_DECORATOR = 3000;
   public static final int ENTITY_CODER = 4000;
   public static final int USER = 5000;

   private Priorities() {
   }
}
