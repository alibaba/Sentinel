package javax.ws.rs.ext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface ParamConverterProvider {
   <T> ParamConverter<T> getConverter(Class<T> var1, Type var2, Annotation[] var3);
}
