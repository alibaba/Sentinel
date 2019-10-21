Sentinel-agent


通过agent的方式接入sentinel


启动方式：


-Djava.net.preferIPv4Stack=true  -javaagent:/Users/guxin/Documents/GitHub/Sentinel/sentinel-agent/sentinel-agent-common/target/sentinel-agent-common-jar-with-dependencies.jar -Dproject.name=consumer33 -Dahas.namespace=default -Dcsp.sentinel.dashboard.server=localhost:8081