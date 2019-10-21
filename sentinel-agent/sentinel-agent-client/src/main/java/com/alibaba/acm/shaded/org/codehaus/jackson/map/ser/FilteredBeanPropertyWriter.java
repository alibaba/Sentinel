package com.alibaba.acm.shaded.org.codehaus.jackson.map.ser;

import com.alibaba.acm.shaded.org.codehaus.jackson.JsonGenerator;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.JsonSerializer;
import com.alibaba.acm.shaded.org.codehaus.jackson.map.SerializerProvider;

/**
 * Decorated {@link BeanPropertyWriter} that will filter out
 * properties that are not to be included in currently active
 * JsonView.
 *
 * @since 1.4
 */
public abstract class FilteredBeanPropertyWriter
{    
    public static BeanPropertyWriter constructViewBased(BeanPropertyWriter base, Class<?>[] viewsToIncludeIn)
    {
        if (viewsToIncludeIn.length == 1) {
            return new SingleView(base, viewsToIncludeIn[0]);
        }
        return new MultiView(base, viewsToIncludeIn);
    }

    /*
    /**********************************************************
    /* Concrete sub-classes
    /**********************************************************
     */

    private final static class SingleView
        extends BeanPropertyWriter
    {
        protected final BeanPropertyWriter _delegate;

        protected final Class<?> _view;
        
        protected SingleView(BeanPropertyWriter delegate, Class<?> view)
        {
            super(delegate);
            _delegate = delegate;
            _view = view;
        }
        
        @Override
        public BeanPropertyWriter withSerializer(JsonSerializer<Object> ser) {
            return new SingleView(_delegate.withSerializer(ser), _view);
        }
        
        @Override
        public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov)
            throws Exception
        {
            Class<?> activeView = prov.getSerializationView();
            if (activeView == null || _view.isAssignableFrom(activeView)) {
                _delegate.serializeAsField(bean, jgen, prov);
            }
        }
    }

    private final static class MultiView
        extends BeanPropertyWriter
    {
        protected final BeanPropertyWriter _delegate;

        protected final Class<?>[] _views;
        
        protected MultiView(BeanPropertyWriter delegate, Class<?>[] views) {
            super(delegate);
            _delegate = delegate;
            _views = views;
        }
        
        @Override
        public BeanPropertyWriter withSerializer(JsonSerializer<Object> ser) {
            return new MultiView(_delegate.withSerializer(ser), _views);
        }
        
        @Override
        public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov)
            throws Exception
        {
            final Class<?> activeView = prov.getSerializationView();
            if (activeView != null) {
                int i = 0, len = _views.length;
                for (; i < len; ++i) {
                    if (_views[i].isAssignableFrom(activeView)) break;
                }
                // not included, bail out:
                if (i == len) {
                    return;
                }
            }
            _delegate.serializeAsField(bean, jgen, prov);
        }
    }
}
