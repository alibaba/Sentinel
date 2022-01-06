
package com.alibaba.csp.sentinel.dashboard.repository.store;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;

/**
 */
public abstract class AbstractDynamicRuleRepository<T> implements DynamicRuleRepository<T>
{

    protected RuleRepository<T, Long> repository;
    
    /**
     * 
     * @return
     */
    public RuleRepository<T, Long> getRespository()
    {
        return repository;
    }
    
    @Override
    public T save(T entity)
    {
        return repository.save(entity);
    }

    @Override
    public List<T> saveAll(List<T> rules)
    {
        return repository.saveAll(rules);
    }

    @Override
    public T delete(Long id)
    {
        return repository.delete(id);
    }

    @Override
    public T findById(Long id)
    {
        return repository.findById(id);
    }

    @Override
    public List<T> findAllByMachine(MachineInfo machineInfo)
    {
        return repository.findAllByMachine(machineInfo);
    }

    @Override
    public List<T> findAllByApp(String appName)
    {
        return repository.findAllByApp(appName);
    }

}
