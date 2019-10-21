package com.alibaba.csp.sentinel.datasource.acm;

import com.alibaba.edas.acm.listener.ConfigChangeListener;
import com.taobao.diamond.client.impl.DiamondEnvRepo;
import com.taobao.diamond.exception.DiamondException;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import java.util.ArrayList;

public class DecryptAcmWhiteDataSource<T> extends AbstractDataSource<String, T> {
   private String tenantId;
   private String dataId;
   private String group;
   private ConfigChangeListener listener;
   private Decryptor decryptor;

   public DecryptAcmWhiteDataSource(String tenantId, String dataId, String group, Converter<String, T> parser, Decryptor decryptor) throws DiamondException {
      super(parser);
      RecordLog.info("DecryptAcmWhiteDataSource, dataId=" + dataId + ", group=" + group);
      this.tenantId = tenantId;
      this.dataId = dataId;
      this.group = group;
      this.decryptor = decryptor;

      try {
         T value = this.loadConfig();
         this.getProperty().updateValue(value);
      } catch (Exception var7) {
         RecordLog.info("DecryptAcmWhiteDataSource error", (Throwable)var7);
      }

      this.listener = new ConfigChangeListener() {
         public void receiveConfigInfo(String conf) {
            try {
               conf = DecryptAcmWhiteDataSource.this.decryptor.decrypt(conf);
               RecordLog.info("receive conf ->" + conf);
               T newValue = DecryptAcmWhiteDataSource.this.loadConfig(conf);
               DecryptAcmWhiteDataSource.this.getProperty().updateValue(newValue);
            } catch (Exception var3) {
               RecordLog.info("DecryptAcmWhiteDataSource error", (Throwable)var3);
            }

         }
      };
      DiamondEnvRepo.getDefaultEnv().addListeners(tenantId, dataId, group, new ArrayList<ConfigChangeListener>() {
         {
            this.add(DecryptAcmWhiteDataSource.this.listener);
         }
      });
   }

   public String readSource() throws Exception {
      String conf = DiamondEnvRepo.getDefaultEnv().getConfig(this.tenantId, this.dataId, this.group, 5000L);
      conf = this.decryptor.decrypt(conf);
      RecordLog.info("read conf ->" + conf);
      return conf;
   }

   public void close() throws Exception {
   }
}
