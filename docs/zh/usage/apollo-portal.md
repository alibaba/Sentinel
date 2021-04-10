# Apollo Portal

sentinel的控制台为了方便运维，提供了直接调用Apollo Portal（Apollo的控制台）的功能

在输入Cookie后，可以直接调用Apollo Portal进行一系列的授权操作，避开麻烦的人工操作

这里做一个HTTP接口的记录

Sentinel控制台主要用的是[赋权](#赋权)

## 开放平台授权管理

参考源码https://github.com/ctripcorp/apollo/blob/master/apollo-portal/src/main/java/com/ctrip/framework/apollo/portal/controller/ConsumerController.java

以及根据浏览器调试，获取相关信息，整理出如下接口

### 查询第三方应用ID是否存在

#### 请求

HTTP GET

URL

```
consumers/by-appId?appId={appId}
```

`{appId}`替换成具体的名字

例如

```
consumers/by-appId?appId=abcd
```

#### 响应

##### 未创建

如果第三方应用未创建，会没有response data

##### 已创建

如果第三方应用已经被创建，会返回类似如下的response data

```json
{
  "consumerId": 3,
  "token": "fbe414f685ffe5fbc891ea146f4997a664db3bf1",
  "expires": "2099-01-01T00:00:00.000+0800",
  "id": 3,
  "isDeleted": false,
  "dataChangeCreatedBy": "apollo",
  "dataChangeCreatedTime": "2021-04-08T22:59:29.000+0800",
  "dataChangeLastModifiedBy": "apollo",
  "dataChangeLastModifiedTime": "2021-04-08T22:59:29.000+0800"
}
```

### 创建第三方应用

#### 请求

HTTP POST

URL

```
consumers
```

Request Body

```json
{
  "appId": "abc",
  "name": "abc-server",
  "orgId": "TEST1",
  "orgName": "样例部门1",
  "ownerName": "apollo"
}
```

| json string | 说明           |
| ----------- | -------------- |
| appId       | 第三方应用ID   |
| name        | 第三方应用名称 |
| orgId       | 部门（Id）     |
| orgName     | 部门（名字）   |
| ownerName   | 项目负责人     |

#### 响应

Response Body

```json
{
  "consumerId": 3,
  "token": "fbe414f685ffe5fbc891ea146f4997a664db3bf1",
  "expires": "2099-01-01T00:00:00.000+0800",
  "id": 3,
  "isDeleted": false,
  "dataChangeCreatedBy": "apollo",
  "dataChangeCreatedTime": "2021-04-08T22:59:28.594+0800",
  "dataChangeLastModifiedBy": "apollo",
  "dataChangeLastModifiedTime": "2021-04-08T22:59:28.594+0800"
}
```

### 赋权

授权类型是**App**（为了可以自动创建集群）

#### 请求

HTTP POST

URL

```
consumers/{token}/assign-role?type=AppRole
```

例如

```
consumers/58bb334437c20e55b86329d4238cd5baf4d4d420/assign-role?type=AppRole
```

Request Body

```json
{"appId":"test"}
```

#### 响应

只有1个element的json array

```json
[
  {
    "consumerId": 2,
    "roleId": 2,
    "id": 7,
    "isDeleted": false,
    "dataChangeCreatedBy": "apollo",
    "dataChangeCreatedTime": "2021-03-30T21:25:10.000+0800",
    "dataChangeLastModifiedBy": "apollo",
    "dataChangeLastModifiedTime": "2021-03-30T21:25:10.000+0800"
  }
]
```

