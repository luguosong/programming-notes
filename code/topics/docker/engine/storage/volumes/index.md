# 数据卷(Volumes)

`数据卷(Volumes)` 是容器的持久化数据存储，由 Docker 创建和管理。您可以通过 `docker volume create` 命令显式创建一个卷，或者在创建容器或服务时由 Docker 自动创建。

当您创建一个卷时，它会存储在 Docker 主机的一个目录中。当您将该卷挂载到容器时，这个目录会被挂载到容器中。这种方式类似于绑定挂载（bind mounts）的工作原理，但卷由 Docker 管理，并与主机的核心功能隔离开来。

## 什么时候使用volumes

卷是持久化由 Docker 容器生成和使用的数据的首选机制。与依赖于主机目录结构和操作系统的绑定挂载不同，卷完全由 Docker 管理。以下场景中，卷是一个不错的选择：

- 卷比绑定挂载更容易备份或迁移。
- 您可以使用 Docker CLI 命令或 Docker API 来管理卷。
- 卷可用于 Linux 和 Windows 容器。
- 卷可以更安全地在多个容器之间共享。
- 新建的卷可以由容器或构建预先填充内容。
- 当您的应用程序需要高性能 I/O 时。

如果需要从主机访问文件，卷（Volumes）并不是一个好的选择，因为卷完全由 Docker 管理。如果需要同时从容器和主机访问文件或目录，建议使用`绑定挂载（Bind Mounts）`。

相比直接将数据写入容器，使用卷通常是更好的选择，因为卷不会增加使用它的容器的大小。此外，使用卷的速度也更快；写入容器的可写层需要存储驱动程序来管理文件系统。存储驱动程序通过 Linux 内核提供联合文件系统（Union Filesystem），这种额外的抽象会降低性能，而使用卷则直接写入主机文件系统，性能更高。

如果容器生成的是非持久化的状态数据，可以考虑使用 `tmpfs 挂载`，这样可以避免将数据永久存储在任何地方，同时通过避免写入容器的可写层来提升容器的性能。

卷使用 `rprivate（递归私有）`绑定传播，且绑定传播对于卷来说是不可配置的。

## 生命周期

卷的内容独立于特定容器的生命周期。当容器被销毁时，其可写层也会随之销毁。使用卷可以确保即使使用该卷的容器被移除，数据仍然得以保留。

同一个卷可以同时挂载到多个容器中。当没有运行中的容器使用某个卷时，该卷仍然可供 Docker 使用，并不会被自动移除。您可以使用 `docker volume prune` 命令删除未使用的卷。

## 覆盖现有数据挂载卷

如果将一个`非空卷`挂载到容器中已存在文件或目录的目录中，挂载操作会遮蔽原本存在的文件。这类似于在 Linux 主机上将文件保存到 `/mnt` 目录，然后将一个 USB 驱动器挂载到 `/mnt`，此时 `/mnt` 的内容会被 USB 驱动器的内容遮蔽，直到卸载 USB 驱动器为止。而在容器中，没有直接的方法可以移除挂载以重新显示被遮蔽的文件。最好的解决办法是重新创建容器并避免挂载该卷。

如果将一个空卷挂载到容器中已存在文件或目录的目录中，这些文件或目录会默认被传播（复制）到该卷中。同样地，如果启动一个容器并指定了一个尚不存在的卷，Docker 会为你创建一个空卷。这是一种为其他容器预填充所需数据的好方法。

如果想阻止 Docker 将容器中已有的文件复制到空卷中，可以使用 `volume-nocopy` 选项，具体请参阅 `--mount` 的选项说明。

## 命名卷和匿名卷 

一个卷可以是命名卷，也可以是匿名卷。匿名卷会被赋予一个随机名称，该名称在特定的 Docker 主机中是唯一的。与命名卷一样，匿名卷即使在删除使用它们的容器后仍会保留，除非在创建容器时使用了 `--rm` 标志，此时与容器关联的匿名卷会被销毁。

如果连续创建多个使用匿名卷的容器，每个容器都会创建自己的卷。匿名卷不会被自动重用或在容器之间共享。如果需要在两个或多个容器之间共享匿名卷，必须使用随机生成的卷 ID 挂载该匿名卷。

## 语法

要使用 `docker run` 命令挂载卷，可以使用 `--mount` 或 `--volume` 标志。

```shell
docker run --mount type=volume,src=<volume-name>,dst=<mount-path>
docker run --volume <volume-name>:<mount-path>
```

一般来说，建议使用 `--mount`。主要区别在于 `--mount` 标志更加明确，并支持所有可用选项。

如果您需要以下操作，则必须使用 `--mount`：

- 指定卷驱动程序选项
- 挂载卷的子目录
- 将卷挂载到 Swarm 服务中

