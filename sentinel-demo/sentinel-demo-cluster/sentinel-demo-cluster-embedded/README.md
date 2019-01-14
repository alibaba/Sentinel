# Sentinel Cluster Embedded Mode Demo

This demo demonstrates how to configure data source for cluster rules and configuration
in **embedded mode**. You can start multiple `ClusterDemoApplication` instances and do cluster assignment in Sentinel dashboard or via dynamic data source.

See [DemoClusterInitFunc](https://github.com/alibaba/Sentinel/blob/master/sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-embedded/src/main/java/com/alibaba/csp/sentinel/demo/cluster/init/DemoClusterInitFunc.java) for a sample of dynamic configuration.