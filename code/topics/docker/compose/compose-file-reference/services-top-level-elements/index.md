# Services顶级元素

## volumes

`volumes` 属性定义了服务容器可以访问的主机路径或命名卷。您可以使用 volumes 定义多种类型的挂载：`volume`、`bind`、`tmpfs` 或
`npipe`。

如果挂载是一个主机路径且仅被单个服务使用，可以在服务定义中声明它。若要在`多个`服务之间重用一个卷，则
`必须在顶层元素 volumes 中声明一个命名卷`。

以下示例展示了一个`命名卷`（db-data）被后端服务使用，以及一个为单个服务定义的绑定挂载。

```yaml
services:
  backend:
    image: example/backend
    volumes:
      - type: volume
        source: db-data
        target: /data
        volume:
          nocopy: true
          subpath: sub
      - type: bind
        source: /var/run/postgres/postgres.sock
        target: /var/run/postgres/postgres.sock

volumes:
  db-data:
```

### 简写语法

短语法使用一个包含以冒号分隔的值的字符串来指定卷挂载（`VOLUME:CONTAINER_PATH`），或访问模式（`VOLUME:CONTAINER_PATH:
ACCESS_MODE`）。

- `VOLUME`：可以是托管容器的平台上的`主机路径（绑定挂载）`或`卷名称`。
- `CONTAINER_PATH`：容器中挂载卷的路径。
- `ACCESS_MODE`：以逗号分隔的选项列表：
	- `rw`：读写访问。如果未指定，这是默认值。
	- `ro`：只读访问。
	- `z`：SELinux选项，表示绑定挂载的主机内容在多个容器之间共享。
	- `Z`：SELinux选项，表示绑定挂载的主机内容是私有的，不与其他容器共享。

!!! note

	在没有 SELinux 的平台上，SELinux 重标记绑定挂载选项将被忽略。

!!! note

	相对主机路径仅支持部署到本地容器运行时的 Compose。这是因为相对路径是从 Compose 文件的父目录解析的，这仅适用于本地情况。当 Compose 部署到非本地平台时，会拒绝使用相对主机路径的 Compose 文件并报错。为了避免与命名卷产生歧义，相对路径应始终以 `.` 或 `..` 开头。

### 长语法

长格式语法允许您配置无法通过短格式表达的其他字段。

- `type`：挂载类型，可以是 volume、bind、tmpfs、npipe 或 cluster
- `source`：挂载的来源，对于 bind 挂载是主机上的路径，或者是顶层 volumes 键中定义的卷名称。不适用于 tmpfs 挂载。
- `target`：卷在容器中挂载的路径。
- `read_only`：设置卷为只读的标志。
- `bind`：用于配置额外的 bind 选项：
	- `propagation`：bind 使用的传播模式。
	- `create_host_path`：如果主机上的源路径不存在，则创建一个目录。如果路径已存在，Compose 不会执行任何操作。为了与旧版 docker-compose 的短语法向后兼容，此行为是自动隐含的。
	- `selinux`：SELinux 重标记选项，z（共享）或 Z（私有）。
- `volume`：配置额外的卷选项：
	- `nocopy`：在创建卷时禁用从容器复制数据的标志。
	- `subpath`：挂载卷内的某个路径，而不是卷的根目录。
- `tmpfs`：配置额外的 tmpfs 选项：
	- `size`：tmpfs 挂载的大小，以字节为单位（可以是数字或字节单位）。
	- `mode`：tmpfs 挂载的文件模式，以 Unix 权限位的八进制数字表示。此功能在 Docker Compose 2.14.0 版本中引入。
- `consistency`：挂载的一致性要求。可用值因平台而异。

!!! note

	使用大型代码库或单一代码库，或者使用无法与代码库规模匹配的虚拟文件系统？Compose 现在利用了同步文件共享功能，并会自动为绑定挂载创建文件共享。请确保您已使用付费订阅登录 Docker，并在 Docker Desktop 的设置中启用了“访问实验性功能”和“使用 Compose 管理同步文件共享”选项。
