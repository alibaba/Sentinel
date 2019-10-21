package com.alibaba.acm.shaded.org.codehaus.jackson.map;

import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedField;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedMethod;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.introspect.AnnotatedParameter;

/**
 * Class that defines how names of JSON properties ("external names")
 * are derived from names of POJO methods and fields ("internal names"),
 * in cases where they are not
 * auto-detected and no explicit annotations exist for naming.
 * Methods are passed information about POJO member for which name is needed,
 * as well as default name that would be used if no custom strategy was used.
 *<p>
 * Default implementation returns suggested ("default") name unmodified.
 *<p>
 * Note that the strategy is guaranteed to be called once per logical property
 * (which may be represented by multiple members; such as pair of a getter and
 * a setter), but may be called for each: implementations should not count on
 * exact number of times, and should work for any member that represent a
 * property.
 *<p>
 * In absence of a registered custom strategy, default Java property naming strategy
 * is used, which leaves field names as is, and removes set/get/is prefix
 * from methods (as well as lower-cases initial sequence of capitalized
 * characters).
 * 
 * @since 1.8
 */
public abstract class PropertyNamingStrategy
{
    /*
    /**********************************************************
    /* API
    /**********************************************************
     */

    /**
     * Method called to find external name (name used in JSON) for given logical
     * POJO property,
     * as defined by given field.
     * 
     * @param config Configuration in used: either <code>SerializationConfig</code>
     *   or <code>DeserializationConfig</code>, depending on whether method is called
     *   during serialization or deserialization
     * @param field Field used to access property
     * @param defaultName Default name that would be used for property in absence of custom strategy
     * 
     * @return Logical name to use for property that the field represents
     */
    public String nameForField(MapperConfig<?> config, AnnotatedField field,
            String defaultName)
    {
        return defaultName;
    }

    /**
     * Method called to find external name (name used in JSON) for given logical
     * POJO property,
     * as defined by given getter method; typically called when building a serializer.
     * (but not always -- when using "getter-as-setter", may be called during
     * deserialization)
     * 
     * @param config Configuration in used: either <code>SerializationConfig</code>
     *   or <code>DeserializationConfig</code>, depending on whether method is called
     *   during serialization or deserialization
     * @param method Method used to access property.
     * @param defaultName Default name that would be used for property in absence of custom strategy
     * 
     * @return Logical name to use for property that the method represents
     */
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method,
            String defaultName)
    {
        return defaultName;
    }

    /**
     * Method called to find external name (name used in JSON) for given logical
     * POJO property,
     * as defined by given setter method; typically called when building a deserializer
     * (but not necessarily only then).
     * 
     * @param config Configuration in used: either <code>SerializationConfig</code>
     *   or <code>DeserializationConfig</code>, depending on whether method is called
     *   during serialization or deserialization
     * @param method Method used to access property.
     * @param defaultName Default name that would be used for property in absence of custom strategy
     * 
     * @return Logical name to use for property that the method represents
     */
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method,
            String defaultName)
    {
        return defaultName;
    }

    /**
     * Method called to find external name (name used in JSON) for given logical
     * POJO property,
     * as defined by given constructor parameter; typically called when building a deserializer
     * (but not necessarily only then).
     * 
     * @param config Configuration in used: either <code>SerializationConfig</code>
     *   or <code>DeserializationConfig</code>, depending on whether method is called
     *   during serialization or deserialization
     * @param ctorParam Constructor parameter used to pass property.
     * @param defaultName Default name that would be used for property in absence of custom strategy
     * @since 1.9
     */
    public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam,
            String defaultName)
    {
        return defaultName;
    }

    /*
    /**********************************************************
    /* Standard implementations 
    /**********************************************************
     */
    
    /**
     * @since 1.9
     */
    public static abstract class PropertyNamingStrategyBase extends PropertyNamingStrategy
    {
        @Override
        public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName)
        {
            return translate(defaultName);
        }

        @Override
        public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName)
        {
            return translate(defaultName);
        }

        @Override
        public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName)
        {
            return translate(defaultName);
        }

        @Override
        public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam,
                String defaultName)
        {
            return translate(defaultName);
        }
        
        public abstract String translate(String propertyName);
    }
        
    
    /*
    /**********************************************************
    /* Standard implementations 
    /**********************************************************
     */

    /**
     * See {@link LowerCaseWithUnderscoresStrategy} for details.
     * 
     * @since 1.9
     */
    public static final PropertyNamingStrategy CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES =
        new LowerCaseWithUnderscoresStrategy();
    
    /**
     * A {@link PropertyNamingStrategy} that translates typical camel case Java 
     * property names to lower case JSON element names, separated by 
     * underscores.  This implementation is somewhat lenient, in that it 
     * provides some additional translations beyond strictly translating from 
     * camel case only.  In particular, the following translations are applied 
     * by this PropertyNamingStrategy.
     * 
     * <ul><li>Every upper case letter in the Java property name is translated 
     * into two characters, an underscore and the lower case equivalent of the 
     * target character, with three exceptions.
     * <ol><li>For contiguous sequences of upper case letters, characters after
     * the first character are replaced only by their lower case equivalent, 
     * and are not preceded by an underscore.
     * <ul><li>This provides for reasonable translations of upper case acronyms, 
     * e.g., &quot;theWWW&quot; is translated to &quot;the_www&quot;.</li></ul></li>
     * <li>An upper case character in the first position of the Java property 
     * name is not preceded by an underscore character, and is translated only 
     * to its lower case equivalent.
     * <ul><li>For example, &quot;Results&quot; is translated to &quot;results&quot;, 
     * and not to &quot;_results&quot;.</li></ul></li>
     * <li>An upper case character in the Java property name that is already 
     * preceded by an underscore character is translated only to its lower case 
     * equivalent, and is not preceded by an additional underscore.
     * <ul><li>For example, &quot;user_Name&quot; is translated to 
     * &quot;user_name&quot;, and not to &quot;user__name&quot; (with two 
     * underscore characters).</li></ul></li></ol></li>
     * <li>If the Java property name starts with an underscore, then that 
     * underscore is not included in the translated name, unless the Java 
     * property name is just one character in length, i.e., it is the 
     * underscore character.  This applies only to the first character of the 
     * Java property name.</li></ul>
     * 
     * These rules result in the following additional example translations from 
     * Java property names to JSON element names.
     * <ul><li>&quot;userName&quot; is translated to &quot;user_name&quot;</li>
     * <li>&quot;UserName&quot; is translated to &quot;user_name&quot;</li>
     * <li>&quot;USER_NAME&quot; is translated to &quot;user_name&quot;</li>
     * <li>&quot;user_name&quot; is translated to &quot;user_name&quot; (unchanged)</li>
     * <li>&quot;user&quot; is translated to &quot;user&quot; (unchanged)</li>
     * <li>&quot;User&quot; is translated to &quot;user&quot;</li>
     * <li>&quot;USER&quot; is translated to &quot;user&quot;</li>
     * <li>&quot;_user&quot; is translated to &quot;user&quot;</li>
     * <li>&quot;_User&quot; is translated to &quot;user&quot;</li>
     * <li>&quot;__user&quot; is translated to &quot;_user&quot; 
     * (the first of two underscores was removed)</li>
     * <li>&quot;user__name&quot; is translated to &quot;user__name&quot;
     * (unchanged, with two underscores)</li></ul>
     * 
     * @since 1.9
     */
    public static class LowerCaseWithUnderscoresStrategy extends PropertyNamingStrategyBase
    {
        @Override
        public String translate(String input)
        {
            if (input == null) return input; // garbage in, garbage out
            int length = input.length();
            StringBuilder result = new StringBuilder(length * 2);
            int resultLength = 0;
            boolean wasPrevTranslated = false;
            for (int i = 0; i < length; i++)
            {
                char c = input.charAt(i);
                if (i > 0 || c != '_') // skip first starting underscore
                {
                    if (Character.isUpperCase(c))
                    {
                        if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != '_')
                        {
                            result.append('_');
                            resultLength++;
                        }
                        c = Character.toLowerCase(c);
                        wasPrevTranslated = true;
                    }
                    else
                    {
                        wasPrevTranslated = false;
                    }
                    result.append(c);
                    resultLength++;
                }
            }
            return resultLength > 0 ? result.toString() : input;
        }
    }
}
