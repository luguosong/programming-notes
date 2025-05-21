# RestTemplate

## 入门案例

将RestTemplate注册到Spring容器：

``` java title="RestTemplateConfig.java"
--8<-- "code/java-serve/distributed/invocation/rest-template/rest-template-hello/src/main/java/com/luguosong/resttemplatehello/config/RestTemplateConfig.java"
```

使用RestTemplate调用服务：

``` java title="TestController.java"
--8<-- "code/java-serve/distributed/invocation/rest-template/rest-template-hello/src/main/java/com/luguosong/resttemplatehello/controller/TestController.java"
```

