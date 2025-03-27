# Volumes顶层元素

Volumes是由容器引擎实现的持久化数据存储。Compose 提供了一种中立的方式，让服务可以挂载卷，并通过配置参数将其分配到基础设施中。顶层的 volumes 声明允许您配置可在多个服务之间重复使用的命名卷。

要在多个服务之间共享一个卷，必须在顶级元素 services 中通过 volumes 属性明确为每个服务授予访问权限。volumes 属性还具有额外的语法，可以实现更细粒度的控制。

!!! note

	使用大型代码库或单一代码库，或者使用无法与代码库规模匹配的虚拟文件系统？Compose 现在利用了同步文件共享功能，并会自动为绑定挂载创建文件共享。请确保您已使用付费订阅登录 Docker，并在 Docker Desktop 的设置中启用了“访问实验性功能”和“使用 Compose 管理同步文件共享”。

## 示例

以下示例展示了一个两服务的设置，其中数据库的数据目录作为名为 db-data 的卷与另一个服务共享，以便可以定期进行备份。

```yaml
services:
  backend:
    image: example/database
    volumes:
      - db-data:/etc/data

  backup:
    image: backup-service
    volumes:
      - db-data:/var/lib/backup/data

volumes:
  db-data:
```

`db-data` 卷分别挂载在容器路径 `/var/lib/backup/data` 和 `/etc/data`，用于备份和后端。

运行 `docker compose up` 会在卷不存在时自动创建；如果卷已存在，则会直接使用。如果卷在 Compose 之外被手动删除，则会重新创建。

## 属性

在顶级的 volumes 部分下，可以留空，此时将使用容器引擎的默认配置来创建卷。您也可以使用以下键进行配置。

### driver

指定应使用哪个卷驱动程序。如果该驱动程序不可用，Compose 将返回错误并不会部署应用程序。

```yaml
volumes:
  db-data:
    driver: foobar
```

### driver_opts

`driver_opts` 指定了一组以键值对形式传递给该卷驱动程序的选项，这些选项依赖于具体的驱动程序。

```yaml
volumes:
  example:
    driver_opts:
      type: "nfs"
      o: "addr=10.40.0.199,nolock,soft,rw"
      device: ":/docker/example"
```

### external

如果设置为 `true`：

- `external` 指定该卷已经存在于平台上，其生命周期由平台管理，而不是由应用程序管理。因此，Compose 不会创建该卷，如果该卷不存在，则会返回错误。
- 除了 name 之外的所有其他属性都无关紧要。如果 Compose 检测到任何其他属性，它会将该 Compose 文件视为无效并拒绝。

在以下示例中，Compose 不会尝试创建名为 {project_name}_db-data 的卷，而是查找一个已存在的、名为 db-data 的卷，并将其挂载到 backend 服务的容器中。

```yaml
services:
  backend:
    image: example/database
    volumes:
      - db-data:/etc/data

volumes:
  db-data:
    external: true
```

### labels

`labels`用于为卷添加元数据。您可以使用数组或字典来定义标签。

建议使用反向DNS表示法，以避免您的标签与其他软件使用的标签发生冲突。

```yaml
volumes:
  db-data:
    labels:
      com.example.description: "Database volume"
      com.example.department: "IT/Ops"
      com.example.label-with-empty-value: ""
```

```yaml
volumes:
  db-data:
    labels:
      - "com.example.description=Database volume"
      - "com.example.department=IT/Ops"
      - "com.example.label-with-empty-value"
```

### name

name 用于为卷设置自定义名称。name 字段可用于引用包含特殊字符的卷。该名称会按原样使用，不会与堆栈名称关联。

```yaml
volumes:
  db-data:
    name: "my-app-data"
```

这使得可以将该查找名称作为 Compose 文件的一个参数，从而使卷的模型 ID 被硬编码，但平台上的实际卷 ID 在部署时的运行时设置。

例如，如果 `.env` 文件中包含 `DATABASE_VOLUME=my_volume_001`：

```yaml
volumes:
  db-data:
    name: ${DATABASE_VOLUME}
```

运行 `docker compose up` 会使用名为 `my_volume_001` 的卷。

它也可以与 `external` 属性一起使用。这意味着，在平台上查找实际卷时使用的名称，与在 Compose 文件中引用该卷时使用的名称是分开设置的：

```yaml
volumes:
  db-data:
    external: true
    name: actual-name-of-volume
```
