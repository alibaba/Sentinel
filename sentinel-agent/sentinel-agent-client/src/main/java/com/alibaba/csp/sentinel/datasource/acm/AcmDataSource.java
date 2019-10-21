package com.alibaba.csp.sentinel.datasource.acm;

import com.alibaba.edas.acm.ConfigService;
import com.alibaba.edas.acm.listener.ConfigChangeListener;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;

public class AcmDataSource<T> extends AbstractDataSource<String, T> {
   private String dataId;
   private String group;
   private ConfigChangeListener listener;

   public AcmDataSource(String dataId, String group, Converter<String, T> parser) {
      super(parser);
      this.dataId = dataId;
      this.group = group;

      try {
         T value = this.loadConfig();
         this.getProperty().updateValue(value);
      } catch (Exception var5) {
         RecordLog.info(var5.getMessage(), (Throwable)var5);
      }

      this.listener = new ConfigChangeListener() {
         public void receiveConfigInfo(String conf) {
            try {
               RecordLog.info("receive conf ->" + conf);
               T newValue = AcmDataSource.this.loadConfig(conf);
               AcmDataSource.this.getProperty().updateValue(newValue);
            } catch (Exception var3) {
               RecordLog.info(var3.getMessage(), (Throwable)var3);
            }

         }
      };
      ConfigService.addListener(dataId, group, this.listener);
   }

   public String readSource() throws Exception {
      String conf = ConfigService.getConfig(this.dataId, this.group, 5000L);
      RecordLog.info("read conf ->" + conf);
      return conf;
   }

   public void close() {
   }
}
