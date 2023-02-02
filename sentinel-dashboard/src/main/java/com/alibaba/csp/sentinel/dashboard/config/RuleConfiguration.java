package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.config.RuleConfiguration.RuleConfigurationImportSelector;
import com.alibaba.csp.sentinel.dashboard.config.rule.RuleStoreType;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author FengJianxin
 * @since 1.8.6.1
 */
@Configuration
@Import({RuleConfigurationImportSelector.class})
public class RuleConfiguration {


    static class RuleConfigurationImportSelector implements ImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            RuleStoreType[] types = RuleStoreType.values();
            String[] imports = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                imports[i] = types[i].getConfigurationClass().getName();
            }
            return imports;
        }
    }

}
