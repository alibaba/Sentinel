package javax.ws.rs.core;

import java.util.Date;
import java.util.List;

public interface Request {
   String getMethod();

   Variant selectVariant(List<Variant> var1);

   Response.ResponseBuilder evaluatePreconditions(EntityTag var1);

   Response.ResponseBuilder evaluatePreconditions(Date var1);

   Response.ResponseBuilder evaluatePreconditions(Date var1, EntityTag var2);

   Response.ResponseBuilder evaluatePreconditions();
}
