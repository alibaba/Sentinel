package com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.std;

import java.io.IOException;

import com.alibaba.acm.shaded.org.codehaus.jackson.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.*;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.BeanDeserializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.SettableBeanProperty;

/**
 * Deserializer that builds on basic {@link BeanDeserializer} but
 * override some aspects like instance construction.
 *<p>
 * Note that this deserializer was significantly changed in Jackson 1.7
 * (due to massive changes in {@link BeanDeserializer}).
 * 
 * @since 1.9 (renamed from 'com.alibaba.acm.shaded.org.codehaus.jackson.map.deser.ThrowableDeserializer')
 */
public class ThrowableDeserializer
    extends BeanDeserializer
{
    protected final static String PROP_NAME_MESSAGE = "message";

    /*
    /************************************************************
    /* Construction
    /************************************************************
     */

    public ThrowableDeserializer(BeanDeserializer baseDeserializer)
    {
        super(baseDeserializer);
    }

    /**
     * Alternative constructor used when creating "unwrapping" deserializers
     * 
     * @since 1.9
     */
    protected ThrowableDeserializer(BeanDeserializer src, boolean ignoreAllUnknown)
    {
        super(src, ignoreAllUnknown);
    }
    
    @Override
    public JsonDeserializer<Object> unwrappingDeserializer()
    {
        if (getClass() != ThrowableDeserializer.class) {
            return this;
        }
        /* main thing really is to just enforce ignoring of unknown
         * properties; since there may be multiple unwrapped values
         * and properties for all may be interleaved...
         */
        return new ThrowableDeserializer(this, true);
    }

    
    /*
    /************************************************************
    /* Overridden methods
    /************************************************************
     */

    @Override
    public Object deserializeFromObject(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // 30-Sep-2010, tatu: Need to allow use of @JsonCreator, so:
        if (_propertyBasedCreator != null) { // proper @JsonCreator
            return _deserializeUsingPropertyBased(jp, ctxt);
        }
        if (_delegateDeserializer != null) {
            return _valueInstantiator.createUsingDelegate(_delegateDeserializer.deserialize(jp, ctxt));
        }
        if (_beanType.isAbstract()) { // for good measure, check this too
            throw JsonMappingException.from(jp, "Can not instantiate abstract type "+_beanType
                    +" (need to add/enable type information?)");
        }
        boolean hasStringCreator = _valueInstantiator.canCreateFromString();
        boolean hasDefaultCtor = _valueInstantiator.canCreateUsingDefault();
        // and finally, verify we do have single-String arg constructor (if no @JsonCreator)
        if (!hasStringCreator && !hasDefaultCtor) {
            throw new JsonMappingException("Can not deserialize Throwable of type "+_beanType
                    +" without having a default contructor, a single-String-arg constructor; or explicit @JsonCreator");
        }
        
        Object throwable = null;
        Object[] pending = null;
        int pendingIx = 0;

        for (; jp.getCurrentToken() != JsonToken.END_OBJECT; jp.nextToken()) {
            String propName = jp.getCurrentName();
            SettableBeanProperty prop = _beanProperties.find(propName);
            jp.nextToken(); // to point to field value

            if (prop != null) { // normal case
                if (throwable != null) {
                    prop.deserializeAndSet(jp, ctxt, throwable);
                    continue;
                }
                // nope; need to defer
                if (pending == null) {
                    int len = _beanProperties.size();
                    pending = new Object[len + len];
                }
                pending[pendingIx++] = prop;
                pending[pendingIx++] = prop.deserialize(jp, ctxt);
                continue;
            }

            // Maybe it's "message"?
            if (PROP_NAME_MESSAGE.equals(propName)) {
                if (hasStringCreator) {
                    throwable = _valueInstantiator.createFromString(jp.getText());
                    // any pending values?
                    if (pending != null) {
                        for (int i = 0, len = pendingIx; i < len; i += 2) {
                            prop = (SettableBeanProperty)pending[i];
                            prop.set(throwable, pending[i+1]);
                        }
                        pending = null;
                    }
                    continue;
                }
            }
            /* As per [JACKSON-313], things marked as ignorable should not be
             * passed to any setter
             */
            if (_ignorableProps != null && _ignorableProps.contains(propName)) {
                jp.skipChildren();
                continue;
            }
            if (_anySetter != null) {
                _anySetter.deserializeAndSet(jp, ctxt, throwable, propName);
                continue;
            }
            // Unknown: let's call handler method
            handleUnknownProperty(jp, ctxt, throwable, propName);
        }
        // Sanity check: did we find "message"?
        if (throwable == null) {
            /* 15-Oct-2010, tatu: Can't assume missing message is an error, since it may be
             *   suppressed during serialization, as per [JACKSON-388].
             *   
             *   Should probably allow use of default constructor, too...
             */
            //throw new JsonMappingException("No 'message' property found: could not deserialize "+_beanType);
            if (hasStringCreator) {
                throwable = _valueInstantiator.createFromString(null);
            } else {
                throwable = _valueInstantiator.createUsingDefault();
            }
            // any pending values?
            if (pending != null) {
                for (int i = 0, len = pendingIx; i < len; i += 2) {
                    SettableBeanProperty prop = (SettableBeanProperty)pending[i];
                    prop.set(throwable, pending[i+1]);
                }
            }
        }
        return throwable;
    }
}
