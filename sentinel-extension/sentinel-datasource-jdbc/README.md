# Sentinel JdbcDataSource 

Sentinel JdbcDataSource provides integration with jdbc from database.

Class inheritance:  JdbcDataSource->AutoRefreshDataSource->AbstractDataSource->ReadableDataSource


## description

* This class using javax.sql.DataSource dbDatasource, String sql, Object[] sqlParameters</br> 
to query <b>effective</b> sentinel rules from databse, and convert the List<Map<String, Object>></br>
to sentinel rule objects.
 
* Extends on AutoRefreshDataSource<S, T> only dependency on sentinel-datasource-extension.</br>
use Class and JDBC API in java.sql and javax.sql package, so has no other dependencies.</br>

* Users are free to choose their own jdbc databse like MySQL,Oracle and so on, desgin tables for storage,</br> 
choose different ORM framework like Spring JDBC, MyBatis and so on.</br>
Only provide a standard javaxDataSource, and the query rule sql and sql parameters.


## usage

To use Sentinel JdbcDataSource, you should add the following dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-jdbc</artifactId>
    <version>x.y.z</version>
</dependency>
```

Then you can create an `JdbcDataSource` and register to rule managers.
For instance:

```java
// `jdbcTemplate` is a Spring JdbcTemplate which your application should supply, in order to execute sql query from your database
// `appName` is your app name
// `ruleRefreshSec` is the interval which pull data from database per seconds, if null 30 seconds by default
ReadableDataSource<List<Map<String, Object>>, List<FlowRule>> dataSource = new JdbcDataSource(dbDataSource, sql, new JdbcDataSource.JdbcFlowRuleConverter(), ruleRefreshSec);
FlowRuleManager.register2Property(dataSource.getProperty());

ReadableDataSource<List<Map<String, Object>>, List<DegradeRule>> dataSource = new JdbcDataSource(dbDataSource, sql, new JdbcDataSource.JdbcDegradeRuleConverter(), ruleRefreshSec);
FlowRuleManager.register2Property(dataSource.getProperty());

ReadableDataSource<List<Map<String, Object>>, List<SystemRule>> dataSource = new JdbcDataSource(dbDataSource, new JdbcDataSource.JdbcSystemRuleConverter(), ruleRefreshSec);
FlowRuleManager.register2Property(dataSource.getProperty());
```
> note: you can extends JdbcDataSource to do more custom logic with your tables. 
