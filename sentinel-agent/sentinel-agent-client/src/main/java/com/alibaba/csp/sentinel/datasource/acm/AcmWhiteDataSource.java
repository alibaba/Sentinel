package com.alibaba.csp.sentinel.datasource.acm;

import com.alibaba.edas.acm.listener.ConfigChangeListener;
import com.taobao.diamond.client.impl.DiamondEnvRepo;
import com.taobao.diamond.exception.DiamondException;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import java.util.ArrayList;

public class AcmWhiteDataSource<T> extends AbstractDataSource<String, T> {
   private String tenantId;
   private String dataId;
   private String group;
   private ConfigChangeListener listener;

   public AcmWhiteDataSource(String tenantId, String dataId, String group, Converter<String, T> parser) throws DiamondException {
      super(parser);
      RecordLog.info("AcmWhiteDataSource, dataId=" + dataId + ", group=" + group);
      this.tenantId = tenantId;
      this.dataId = dataId;
      this.group = group;

      try {
         T value = this.loadConfig();
         this.getProperty().updateValue(value);
      } catch (Exception var6) {
         RecordLog.info("AcmWhiteDataSource error", (Throwable)var6);
      }

      this.listener = new ConfigChangeListener() {
         public void receiveConfigInfo(String conf) {
            try {
               RecordLog.info("receive conf ->" + conf);
               T newValue = AcmWhiteDataSource.this.loadConfig(conf);
               AcmWhiteDataSource.this.getProperty().updateValue(newValue);
            } catch (Exception var3) {
               RecordLog.info("AcmWhiteDataSource error", (Throwable)var3);
            }

         }
      };
      DiamondEnvRepo.getDefaultEnv().addListeners(tenantId, dataId, group, new ArrayList<ConfigChangeListener>() {
         {
            this.add(AcmWhiteDataSource.this.listener);
         }
      });
   }

   public String readSource() throws Exception {
      String conf = DiamondEnvRepo.getDefaultEnv().getConfig(this.tenantId, this.dataId, this.group, 5000L);
      RecordLog.info("read conf ->" + conf);
      return conf;
   }

   public void close() throws Exception {
   }
}
