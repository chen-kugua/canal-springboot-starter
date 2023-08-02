# canal-springboot-starter
canal-springboot-starter demo

```properties
# canal服务器地址逗号分隔
canal.cluster=192.168.148.102:11111
canal.username=
canal.password=
canal.destination=example
canal.test-idle-time=5000

```

1.实现com.cpiwx.canalstarter.service.CanalService   
2.重写handleInsert、handleUpdate、handleDelete、handleDdl方法即可
