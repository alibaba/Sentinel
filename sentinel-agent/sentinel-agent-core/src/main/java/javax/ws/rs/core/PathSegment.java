package javax.ws.rs.core;

public interface PathSegment {
   String getPath();

   MultivaluedMap<String, String> getMatrixParameters();
}
