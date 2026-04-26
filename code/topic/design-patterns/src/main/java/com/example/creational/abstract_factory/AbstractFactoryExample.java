package com.example.creational.abstract_factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 抽象工厂模式 - 正例
 * 工厂族保证同一平台的组件总是一起创建，切换平台只需换一个工厂实例
 */
public class AbstractFactoryExample {
    public static void main(String[] args) {
        // ✅ 只需切换工厂，所有 DAO 自动换成对应数据库的实现
        runApp(new MySQLDaoFactory());
        System.out.println("---");
        runApp(new H2DaoFactory());
    }

    static void runApp(DaoFactory factory) {
        OrderApplicationService service = new OrderApplicationService(factory);
        service.createAndQueryOrder();
    }
}

// 领域模型
class User {
    private final Long   id;
    private final String name;

    public User(Long id, String name) { this.id = id; this.name = name; }

    public Long   getId()   { return id;   }
    public String getName() { return name; }
}

class Order {
    private final Long   id;
    private final Long   userId;
    private final String productId;
    private       String status;

    public Order(Long id, Long userId, String productId) {
        this.id        = id;
        this.userId    = userId;
        this.productId = productId;
        this.status    = "PENDING";
    }

    public Long   getId()        { return id;        }
    public Long   getUserId()    { return userId;    }
    public String getProductId() { return productId; }
    public String getStatus()    { return status;    }
    public void   setStatus(String status) { this.status = status; }
}

// DAO 接口
interface UserDao {
    void   save(User user);
    User   findById(Long id);
    List<User> findAll();
}

interface OrderDao {
    void    save(Order order);
    Order   findById(Long id);
    List<Order> findByUserId(Long userId);
    void    updateStatus(Long orderId, String status);
}

// MySQL 实现
class MySQLUserDao implements UserDao {
    private final List<User> store = new ArrayList<>();

    @Override public void save(User user) { store.add(user); System.out.println("[MySQL] 保存用户: " + user.getName()); }
    @Override public User findById(Long id) { return store.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null); }
    @Override public List<User> findAll() { return store; }
}

class MySQLOrderDao implements OrderDao {
    private final List<Order> store = new ArrayList<>();

    @Override public void save(Order order) { store.add(order); System.out.println("[MySQL] 保存订单: " + order.getId()); }
    @Override public Order findById(Long id) { return store.stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null); }
    @Override public List<Order> findByUserId(Long userId) { return store.stream().filter(o -> o.getUserId().equals(userId)).toList(); }
    @Override public void updateStatus(Long orderId, String status) { findById(orderId).setStatus(status); }
}

// H2 实现（内存）
class H2UserDao implements UserDao {
    private final List<User> store = new ArrayList<>();

    @Override public void save(User user) { store.add(user); System.out.println("[H2] 保存用户: " + user.getName()); }
    @Override public User findById(Long id) { return store.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null); }
    @Override public List<User> findAll() { return store; }
}

class H2OrderDao implements OrderDao {
    private final List<Order> store = new ArrayList<>();

    @Override public void save(Order order) { store.add(order); System.out.println("[H2] 保存订单: " + order.getId()); }
    @Override public Order findById(Long id) { return store.stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null); }
    @Override public List<Order> findByUserId(Long userId) { return store.stream().filter(o -> o.getUserId().equals(userId)).toList(); }
    @Override public void updateStatus(Long orderId, String status) { findById(orderId).setStatus(status); }
}

// 抽象工厂：创建一族相关 DAO
interface DaoFactory {
    UserDao  createUserDao();
    OrderDao createOrderDao();
}

// 具体工厂：MySQL
class MySQLDaoFactory implements DaoFactory {
    @Override public UserDao  createUserDao()  { return new MySQLUserDao();  }
    @Override public OrderDao createOrderDao() { return new MySQLOrderDao(); }
}

// 具体工厂：H2（用于测试）
class H2DaoFactory implements DaoFactory {
    @Override public UserDao  createUserDao()  { return new H2UserDao();  }
    @Override public OrderDao createOrderDao() { return new H2OrderDao(); }
}

// 应用服务：只依赖抽象工厂，不依赖任何具体数据库
class OrderApplicationService {
    private final UserDao  userDao;
    private final OrderDao orderDao;

    public OrderApplicationService(DaoFactory factory) {
        this.userDao  = factory.createUserDao();
        this.orderDao = factory.createOrderDao();
    }

    public void createAndQueryOrder() {
        User  user  = new User(1L, "张三");
        Order order = new Order(100L, 1L, "product-" + UUID.randomUUID().toString().substring(0, 8));

        userDao.save(user);
        orderDao.save(order);
        orderDao.updateStatus(100L, "PAID");

        Order found = orderDao.findById(100L);
        System.out.println("查询订单状态: " + found.getStatus()); // PAID
    }
}
