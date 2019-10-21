package javax.ws.rs.core;

import javax.ws.rs.ext.RuntimeDelegate;

public class EntityTag {
   private static final RuntimeDelegate.HeaderDelegate<EntityTag> HEADER_DELEGATE = RuntimeDelegate.getInstance().createHeaderDelegate(EntityTag.class);
   private String value;
   private boolean weak;

   public EntityTag(String value) {
      this(value, false);
   }

   public EntityTag(String value, boolean weak) {
      if (value == null) {
         throw new IllegalArgumentException("value==null");
      } else {
         this.value = value;
         this.weak = weak;
      }
   }

   public static EntityTag valueOf(String value) {
      return (EntityTag)HEADER_DELEGATE.fromString(value);
   }

   public boolean isWeak() {
      return this.weak;
   }

   public String getValue() {
      return this.value;
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (!(obj instanceof EntityTag)) {
         return super.equals(obj);
      } else {
         EntityTag other = (EntityTag)obj;
         return this.value.equals(other.getValue()) && this.weak == other.isWeak();
      }
   }

   public int hashCode() {
      int hash = 3;
      hash = 17 * hash + (this.value != null ? this.value.hashCode() : 0);
      hash = 17 * hash + (this.weak ? 1 : 0);
      return hash;
   }

   public String toString() {
      return HEADER_DELEGATE.toString(this);
   }
}
