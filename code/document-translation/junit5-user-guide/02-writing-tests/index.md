# 编写测试

以下示例展示了在 JUnit Jupiter 中编写测试的最低要求。本章后续部分将详细介绍所有可用功能。

```java title="第一个测试用例"
import static org.junit.jupiter.api.Assertions.assertEquals;

import example.util.Calculator;

import org.junit.jupiter.api.Test;

class MyFirstJUnitJupiterTests {

	private final Calculator calculator = new Calculator();

	@Test
	void addition() {
		assertEquals(2, calculator.add(1, 1));
	}

}
```

## 注解

JUnit Jupiter 支持以下注解，用于配置测试和扩展框架。

除非另有说明，所有核心注解均位于 junit-jupiter-api 模块中的 org.junit.jupiter.api 包内。

| 注解                     | 描述                                                                                                                                                                                                 |
|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| @Test                  | 表示某个方法是测试方法。与 JUnit 4 的 @Test 注解不同，此注解不声明任何属性，因为 JUnit Jupiter 中的测试扩展是基于其专用注解运行的。这类方法会被继承，除非它们被重写。                                                                                                 |
| @ParameterizedTest     | 表示某方法为参数化测试。此类方法会被继承，除非它们被重写。                                                                                                                                                                      |
| @RepeatedTest          | 标示某方法为重复测试的测试模板。此类方法会被继承，除非它们被重写。                                                                                                                                                                  |
| @TestFactory           | 表示某个方法是用于动态测试的测试工厂。此类方法会被继承，除非它们被重写。                                                                                                                                                               |
| @TestTemplate          | 表示某个方法是测试用例的模板，可根据注册的提供者返回的调用上下文数量多次调用。此类方法会被继承，除非被重写。                                                                                                                                             |
| @TestClassOrder        | 用于配置带有 @Nested 注解的测试类中嵌套测试类的执行顺序。此类注解是可继承的。                                                                                                                                                        |
| @TestMethodOrder       | 用于配置带注解测试类的测试方法执行顺序，类似于 JUnit 4 的 @FixMethodOrder。此类注解是可继承的。                                                                                                                                       |
| @TestInstance          | 用于配置带注解测试类的测试实例生命周期。此类注解是可继承的。                                                                                                                                                                     |
| @DisplayName           | 声明测试类或测试方法的自定义显示名称。此类注解不会被继承。                                                                                                                                                                      |
| @DisplayNameGeneration | 声明一个用于测试类的自定义显示名称生成器。此类注解是可继承的。                                                                                                                                                                    |
| @BeforeEach            | 表示被注解的方法应在当前类中的每个 @Test、@RepeatedTest、@ParameterizedTest 或 @TestFactory 方法执行之前运行；类似于 JUnit 4 的 @Before。这些方法会被继承，除非它们被重写。                                                                           |
| @AfterEach             | 表示被注解的方法应在当前类中的每个 @Test、@RepeatedTest、@ParameterizedTest 或 @TestFactory 方法执行后运行；类似于 JUnit 4 的 @After。这些方法会被继承，除非它们被重写。                                                                             |
| @BeforeAll             | 表示被注解的方法应在当前类中的所有 @Test、@RepeatedTest、@ParameterizedTest 和 @TestFactory 方法之前执行；类似于 JUnit 4 的 @BeforeClass。这类方法会被继承，除非被重写，并且必须是静态方法，除非使用“按类”测试实例生命周期。                                               |
| @AfterAll              | 标注的方法表示应在当前类中的所有 @Test、@RepeatedTest、@ParameterizedTest 和 @TestFactory 方法执行完毕后运行；类似于 JUnit 4 的 @AfterClass。这类方法会被继承，除非被重写，并且必须是静态方法，除非使用“按类”测试实例生命周期。                                              |
| @Nested                | 标注该类为非静态嵌套测试类。在 Java 8 至 Java 15 中，除非使用“按类”测试实例生命周期模式，否则无法直接在 @Nested 测试类中使用 @BeforeAll 和 @AfterAll 方法。从 Java 16 开始，可以在 @Nested 测试类中将 @BeforeAll 和 @AfterAll 方法声明为静态方法，无论使用哪种测试实例生命周期模式。这些注解不会被继承。 |
| @Tag                   | 用于声明测试过滤的标签，可以在类级别或方法级别使用；类似于 TestNG 中的测试组或 JUnit 4 中的分类。这类注解在类级别是可继承的，但在方法级别不可继承。                                                                                                                 |
| @Disabled              | 用于禁用测试类或测试方法；类似于 JUnit 4 的 @Ignore。此类注解不会被继承。                                                                                                                                                      |
| @AutoClose             | 标注的字段表示一个资源，该资源将在测试执行后自动关闭。                                                                                                                                                                        |
| @Timeout               | 用于在测试、测试工厂、测试模板或生命周期方法的执行时间超过指定时长时使其失败。此类注解是可继承的。                                                                                                                                                  |
| @TempDir               | 用于通过字段注入或参数注入，在测试类的构造方法、生命周期方法或测试方法中提供临时目录；位于 org.junit.jupiter.api.io 包中。此类字段是可继承的。                                                                                                               |
| @ExtendWith            | 用于以声明方式注册扩展。这类注解是可继承的。                                                                                                                                                                             |
| @RegisterExtension     | 通过字段以编程方式注册扩展。这些字段是可继承的。                                                                                                                                                                           |

!!! warning

	某些注解可能仍处于实验阶段。请查阅实验性 API 表格以获取详细信息。

### 元注解与组合注解

`JUnit Jupiter` 的注解可以用作元注解。这意味着你可以定义自己的组合注解，并自动继承其元注解的语义。

例如，与其在代码中到处复制粘贴 `@Tag("fast")`（参见`标签和过滤功能`），你可以创建一个名为 `@Fast` 的自定义组合注解，如下所示。之后，
`@Fast` 就可以作为 `@Tag("fast")` 的直接替代使用。

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Tag("fast")
public @interface Fast {
}
```

以下 `@Test` 方法展示了 `@Fast` 注解的使用。

```java

@Fast
@Test
void myFastTest() {
	// ...
}
```

您甚至可以更进一步，创建一个自定义的 `@FastTest` 注解，用作 `@Tag("fast")` 和 `@Test` 的替代方案。

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Tag("fast")
@Test
public @interface FastTest {
}
```

JUnit 会自动识别以下方法为带有 "fast" 标签的 @Test 方法。

```java

@FastTest
void myFastTest() {
	// ...
}
```

## 定义

!!! note "Platform概念"

	- `Container(容器)`: `测试树`中的一个节点，包含其他`容器`或`Test测试`作为其子节点（例如，一个`测试类`）。
	- `Test(测试)`: `测试树`中的一个节点，用于在执行时验证预期行为（例如，一个 `@Test 方法`）。

!!! note "Jupiter概念"

	- `Lifecycle Method(生命周期方法)`:任何直接使用 `@BeforeAll`、`@AfterAll`、`@BeforeEach` 或 `@AfterEach` 注解或元注解的方法。
    - `Test Class`:任何顶级类、静态成员类或标注了 `@Nested` 的类，`只要包含至少一个测试方法`，即可视为一个`容器`。测试类不能是抽象类，并且必须只有一个构造函数。同时，Java 的 record 类也受到支持。
	- `Test Method`:任何直接使用 `@Test`、`@RepeatedTest`、`@ParameterizedTest`、`@TestFactory` 或 `@TestTemplate` 注解或元注解的实例方法。除了 `@Test` 之外，这些注解会在测试树中创建一个容器，用于分组测试，或者（对于 @TestFactory）可能分组其他容器。

## 测试类与方法

`测试方法`和`生命周期方法`可以在当前测试类中本地声明，也可以从`父类继承`，或者从接口继承（参见测试接口和默认方法）。此外，测试方法和生命周期方法
`不能是抽象的`，并且`不能返回值`（@TestFactory方法除外，这些方法必须返回一个值）。

!!! note "类和方法的可见性"

	测试类、测试方法和生命周期方法不要求是 public，但不能是 private。

	通常建议对测试类、测试方法和生命周期方法省略 `public` 修饰符，除非有技术上的必要。例如，当一个测试类需要被另一个包中的测试类继承时，这是一个合理的技术理由。另一个将类和方法声明为 `public` 的技术原因是，在使用 Java 模块系统时，可以简化模块路径上的测试过程。

!!! note "字段与方法继承"

	测试类中的字段是可以继承的。例如，超类中的 @TempDir 字段在子类中也会始终生效。

	测试方法和生命周期方法会被继承，除非根据 Java 语言的可见性规则进行了重写。例如，来自父类的 @Test 方法在子类中始终会被应用，除非子类显式地重写了该方法。同样地，如果父类中声明了一个包级私有的 @Test 方法，而父类与子类位于不同的包中，那么该 @Test 方法也会始终在子类中被应用，因为子类无法重写来自不同包的父类中的包级私有方法。

以下测试类展示了 @Test 方法的使用以及所有支持的生命周期方法。有关运行时语义的更多信息，请参阅`测试执行顺序`和
`回调的包装行为`。

```java
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class StandardTests {

	@BeforeAll
	static void initAll() {
	}

	@BeforeEach
	void init() {
	}

	@Test
	void succeedingTest() {
	}

	@Test
	void failingTest() {
		fail("a failing test");
	}

	@Test
	@Disabled("for demonstration purposes")
	void skippedTest() {
		// not executed
	}

	@Test
	void abortedTest() {
		assumeTrue("abc".contains("Z"));
		fail("test should have been aborted");
	}

	@AfterEach
	void tearDown() {
	}

	@AfterAll
	static void tearDownAll() {
	}

}
```

也可以使用 Java 记录类作为测试类，以下示例对此进行了说明。

```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import example.util.Calculator;

import org.junit.jupiter.api.Test;

record MyFirstJUnitJupiterRecordTests() {

	@Test
	void addition() {
		assertEquals(2, new Calculator().add(1, 1));
	}

}
```

## 显示名称

测试类和测试方法可以通过 `@DisplayName` 注解声明自定义`显示名称`——显示名称可以包含空格、特殊字符，甚至是表情符号——这些名称将在
`测试报告`中以及`测试运行器`和 `IDE` 中显示。

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("A special test case")
class DisplayNameDemo {

	@Test
	@DisplayName("Custom test name containing spaces")
	void testWithDisplayNameContainingSpaces() {
	}

	@Test
	@DisplayName("╯°□°）╯")
	void testWithDisplayNameContainingSpecialCharacters() {
	}

	@Test
	@DisplayName("😱")
	void testWithDisplayNameContainingEmoji() {
	}

}
```

### 显示名称生成器

JUnit Jupiter 支持通过 `@DisplayNameGeneration` 注解配置自定义`显示名称生成器`。通过 `@DisplayName` 注解提供的值始终`优先`
于由 `DisplayNameGenerator` 生成的显示名称。

生成器可以通过实现 DisplayNameGenerator 接口来创建。以下是 Jupiter 中可用的一些默认生成器：

| DisplayNameGenerator | 行为                                        |
|----------------------|-------------------------------------------|
| Standard             | 与自 JUnit Jupiter 5.0 发布以来的标准显示名称生成行为保持一致。 |
| Simple               | 去除无参数方法的尾部括号。                             |
| ReplaceUnderscores   | 将下划线替换为空格。                                |
| IndicativeSentences  | 生成完整句子，通过连接测试名称和外部类名称。                    |

请注意，对于 `IndicativeSentences`，您可以通过使用 `@IndicativeSentencesGeneration` 来自定义分隔符和底层生成器，如以下示例所示。

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.IndicativeSentencesGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DisplayNameGeneratorDemo {

	@Nested
	@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
	class A_year_is_not_supported {

		@Test
		void if_it_is_zero() {
		}

		@DisplayName("A negative value for year is not supported by the leap year computation.")
		@ParameterizedTest(name = "For example, year {0} is not supported.")
		@ValueSource(ints = {-1, -4})
		void if_it_is_negative(int year) {
		}

	}

	@Nested
	@IndicativeSentencesGeneration(separator = " -> ", generator = ReplaceUnderscores.class)
	class A_year_is_a_leap_year {

		@Test
		void if_it_is_divisible_by_4_but_not_by_100() {
		}

		@ParameterizedTest(name = "Year {0} is a leap year.")
		@ValueSource(ints = {2016, 2020, 2048})
		void if_it_is_one_of_the_following_years(int year) {
		}

	}

}
```

```text
+-- DisplayNameGeneratorDemo [OK]
  +-- A year is not supported [OK]
  | +-- A negative value for year is not supported by the leap year computation. [OK]
  | | +-- For example, year -1 is not supported. [OK]
  | | '-- For example, year -4 is not supported. [OK]
  | '-- if it is zero() [OK]
  '-- A year is a leap year [OK]
    +-- A year is a leap year -> if it is divisible by 4 but not by 100. [OK]
    '-- A year is a leap year -> if it is one of the following years. [OK]
      +-- Year 2016 is a leap year. [OK]
      +-- Year 2020 is a leap year. [OK]
      '-- Year 2048 is a leap year. [OK]
```

### 设置默认显示名称生成器

您可以使用 `junit.jupiter.displayname.generator.default` 配置参数来指定默认使用的 `DisplayNameGenerator` 的完全限定类名。与通过
`@DisplayNameGeneration` 注解配置的显示名称生成器类似，提供的类必须实现 `DisplayNameGenerator`
接口。默认的显示名称生成器将用于所有测试，除非在包含的测试类或测试接口上存在 `@DisplayNameGeneration` 注解。通过
`@DisplayName` 注解提供的值始终优先于由 `DisplayNameGenerator` 生成的显示名称。

例如，要默认使用 `ReplaceUnderscores` 显示名称生成器，您需要将配置参数设置为对应的完全限定类名（例如，在
`src/test/resources/junit-platform.properties` 文件中）：

```properties
junit.jupiter.displayname.generator.default=\
    org.junit.jupiter.api.DisplayNameGenerator$ReplaceUnderscores
```

同样，您可以指定实现 `DisplayNameGenerator` 的任何自定义类的完全限定名称。

总而言之，测试类或方法的显示名称是根据以下优先级规则确定的：

1. 如果存在，则使用 @DisplayName 注解的值
2. 如果存在，则调用 @DisplayNameGeneration 注解中指定的 DisplayNameGenerator
3. 如果存在，则调用通过配置参数设置的默认 DisplayNameGenerator
4. 调用 org.junit.jupiter.api.DisplayNameGenerator.Standard

## 断言

JUnit Jupiter 提供了许多与 JUnit 4 相同的断言方法，并新增了一些非常适合与 Java 8 的 lambda 表达式一起使用的方法。所有
JUnit Jupiter 的断言方法都是 `org.junit.jupiter.api.Assertions` 类中的静态方法。

断言方法可以选择性地接受断言消息作为第三个参数，该参数可以是一个字符串（String）或一个字符串供应器（Supplier<String>）。

使用 `Supplier<String>`（例如，lambda 表达式）时，消息会被延迟计算。这种方式可以带来性能上的优势，尤其是在消息构造过程复杂或耗时的情况下，因为只有在断言失败时才会对消息进行计算。

```java
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;

import example.domain.Person;
import example.util.Calculator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class AssertionsDemo {

	private final Calculator calculator = new Calculator();

	private final Person person = new Person("Jane", "Doe");

	@Test
	void standardAssertions() {
		assertEquals(2, calculator.add(1, 1));
		assertEquals(4, calculator.multiply(2, 2),
				"可选的失败消息现在是最后一个参数。");

		// 延迟计算 generateFailureMessage('a', 'b')。
		assertTrue('a' < 'b', () -> generateFailureMessage('a', 'b'));
	}

	@Test
	void groupedAssertions() {
		// 在一组断言中，所有断言都会被执行，所有失败的情况将一起报告。
		assertAll("person",
				() -> assertEquals("Jane", person.getFirstName()),
				() -> assertEquals("Doe", person.getLastName())
		);
	}

	@Test
	void dependentAssertions() {
		// 在代码块中，如果断言失败，  
		// 同一代码块中的后续代码将被跳过。
		assertAll("properties",
				() -> {
					String firstName = person.getFirstName();
					assertNotNull(firstName);

					// 仅在前一个断言有效时执行。
					assertAll("first name",
							() -> assertTrue(firstName.startsWith("J")),
							() -> assertTrue(firstName.endsWith("e"))
					);
				},
				() -> {
					// 分组断言，因此独立于第一个名称断言的结果进行处理。
					String lastName = person.getLastName();
					assertNotNull(lastName);

					// 仅当先前的断言有效时执行。
					assertAll("last name",
							() -> assertTrue(lastName.startsWith("D")),
							() -> assertTrue(lastName.endsWith("e"))
					);
				}
		);
	}

	@Test
	void exceptionTesting() {
		Exception exception = assertThrows(ArithmeticException.class, () ->
				calculator.divide(1, 0));
		assertEquals("/ by zero", exception.getMessage());
	}

	@Test
	void timeoutNotExceeded() {
		// 以下断言成功。
		assertTimeout(ofMinutes(2), () -> {
			// 执行耗时少于两分钟的任务。
		});
	}

	@Test
	void timeoutNotExceededWithResult() {
		// 以下断言成功，并返回提供的对象。
		String actualResult = assertTimeout(ofMinutes(2), () -> {
			return "a result";
		});
		assertEquals("a result", actualResult);
	}

	@Test
	void timeoutNotExceededWithMethod() {
		// 以下断言调用了一个方法引用并返回一个对象。
		String actualGreeting = assertTimeout(ofMinutes(2), AssertionsDemo::greeting);
		assertEquals("Hello, World!", actualGreeting);
	}

	@Test
	void timeoutExceeded() {
		// 以下断言会失败，并出现类似以下的错误信息：  
		// 执行时间超过了 10 毫秒的超时时间，多出 91 毫秒
		assertTimeout(ofMillis(10), () -> {
			// 模拟耗时超过10毫秒的任务。
			Thread.sleep(100);
		});
	}

	@Test
	void timeoutExceededWithPreemptiveTermination() {
		// 以下断言会失败，并出现类似以下的错误信息：  
		// 执行超时，耗时 10 毫秒
		assertTimeoutPreemptively(ofMillis(10), () -> {
			// 模拟耗时超过10毫秒的任务。
			new CountDownLatch(1).await();
		});
	}

	private static String greeting() {
		return "Hello, World!";
	}

	private static String generateFailureMessage(char a, char b) {
		return "Assertion messages can be lazily evaluated -- "
				+ "to avoid constructing complex messages unnecessarily." + (a < b);
	}
}
```

!!! warning "使用 assertTimeoutPreemptively() 实现抢占式超时"

	Assertions 类中的各种 assertTimeoutPreemptively() 方法会在与调用代码不同的线程中执行提供的可执行对象或供应者。如果可执行对象或供应者中的代码依赖于 java.lang.ThreadLocal 存储，这种行为可能会导致不良的副作用。

	在 Spring 框架中，事务性测试支持是一个常见的例子。具体来说，Spring 的测试支持会在调用测试方法之前，通过 ThreadLocal 将事务状态绑定到当前线程。因此，如果传递给 `assertTimeoutPreemptively()` 的可执行代码或供应器调用了参与事务的 Spring 管理组件，那么这些组件执行的任何操作都不会随着测试管理的事务一起回滚。相反，这些操作会被提交到持久化存储（例如关系型数据库），即使测试管理的事务已被回滚。

	使用依赖于 ThreadLocal 存储的其他框架时，可能也会遇到类似的副作用。

### Kotlin断言支持

JUnit Jupiter 还提供了一些非常适合在 Kotlin 中使用的断言方法。所有 JUnit Jupiter 的 Kotlin 断言都是 org.junit.jupiter.api
包中的顶级函数。

```kotlin
import example.domain.Person
import example.util.Calculator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.assertTimeout
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.time.Duration

class KotlinAssertionsDemo {
    private val person = Person("Jane", "Doe")
    private val people = setOf(person, Person("John", "Doe"))

    @Test
    fun `exception absence testing`() {
        val calculator = Calculator()
        val result =
            assertDoesNotThrow("Should not throw an exception") {
                calculator.divide(0, 1)
            }
        assertEquals(0, result)
    }

    @Test
    fun `expected exception testing`() {
        val calculator = Calculator()
        val exception =
            assertThrows<ArithmeticException>("Should throw an exception") {
                calculator.divide(1, 0)
            }
        assertEquals("/ by zero", exception.message)
    }

    @Test
    fun `grouped assertions`() {
        assertAll(
            "Person properties",
            { assertEquals("Jane", person.firstName) },
            { assertEquals("Doe", person.lastName) }
        )
    }

    @Test
    fun `grouped assertions from a stream`() {
        assertAll(
            "People with first name starting with J",
            people
                .stream()
                .map {
                    // This mapping returns Stream<() -> Unit>
                    { assertTrue(it.firstName.startsWith("J")) }
                }
        )
    }

    @Test
    fun `grouped assertions from a collection`() {
        assertAll(
            "People with last name of Doe",
            people.map { { assertEquals("Doe", it.lastName) } }
        )
    }

    @Test
    fun `timeout not exceeded testing`() {
        val fibonacciCalculator = FibonacciCalculator()
        val result =
            assertTimeout(Duration.ofMillis(1000)) {
                fibonacciCalculator.fib(14)
            }
        assertEquals(377, result)
    }

    @Test
    fun `timeout exceeded with preemptive termination`() {
        // The following assertion fails with an error message similar to:
        // execution timed out after 10 ms
        assertTimeoutPreemptively(Duration.ofMillis(10)) {
            // Simulate task that takes more than 10 ms.
            Thread.sleep(100)
        }
    }

    @Test
    fun `assertNotNull with a smart cast`() {
        val nullablePerson: Person? = person

        assertNotNull(nullablePerson)

        // The compiler smart casts nullablePerson to a non-nullable object.
        // The safe call operator (?.) isn't required.
        assertEquals(person.firstName, nullablePerson.firstName)
        assertEquals(person.lastName, nullablePerson.lastName)
    }

    @Test
    fun `assertInstanceOf with a smart cast`() {
        val maybePerson: Any = person

        assertInstanceOf<Person>(maybePerson)

        // The compiler smart casts maybePerson to a Person object,
        // allowing to access the Person properties.
        assertEquals(person.firstName, maybePerson.firstName)
        assertEquals(person.lastName, maybePerson.lastName)
    }
}
```

### 第三方断言库

尽管 JUnit Jupiter 提供的断言功能已经能够满足许多测试场景的需求，但在某些情况下，可能需要更强大的功能或额外的特性，例如匹配器。在这种情况下，JUnit
团队推荐使用第三方断言库，例如 AssertJ、Hamcrest、Truth 等。因此，开发者可以自由选择自己喜欢的断言库来使用。

例如，结合匹配器和流式 API 可以使断言更加描述性和可读性。然而，JUnit Jupiter 的 `org.junit.jupiter.api.Assertions` 类并未提供类似于
JUnit 4 的 `org.junit.Assert` 类中接受 Hamcrest Matcher 的 `assertThat()` 方法。相反，开发者被鼓励使用第三方断言库中提供的内置匹配器支持。

以下示例展示了如何在 JUnit Jupiter 测试中使用 Hamcrest 提供的 assertThat() 支持。只要已将 Hamcrest 库添加到类路径中，就可以静态导入诸如
assertThat()、is() 和 equalTo() 等方法，然后像下面的 assertWithHamcrestMatcher() 方法中一样在测试中使用它们。

```java
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import example.util.Calculator;

import org.junit.jupiter.api.Test;

class HamcrestAssertionsDemo {

	private final Calculator calculator = new Calculator();

	@Test
	void assertWithHamcrestMatcher() {
		assertThat(calculator.subtract(4, 1), is(equalTo(3)));
	}

}
```

当然，基于 JUnit 4 编程模型的遗留测试仍然可以继续使用 org.junit.Assert#assertThat。

## 假设(Assumptions)

假设通常在继续执行某个测试已不再有意义时使用，例如，当测试依赖于当前运行环境中不存在的某些内容时。

- 当假设成立时，假设方法不会抛出异常，测试会像往常一样继续执行。
- 当假设不成立时，假设方法会抛出类型为 `org.opentest4j.TestAbortedException` 的异常，以表明测试应被中止，而不是标记为失败。

JUnit Jupiter 提供了一部分 JUnit 4 中的假设方法，并新增了一些方法，这些方法非常适合与 Java 8 的 lambda 表达式和方法引用一起使用。

JUnit Jupiter 的所有假设方法都是 `org.junit.jupiter.api.Assumptions` 类中的静态方法。

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import example.util.Calculator;

import org.junit.jupiter.api.Test;

class AssumptionsDemo {

    private final Calculator calculator = new Calculator();

    @Test
    void testOnlyOnCiServer() {
        assumeTrue("CI".equals(System.getenv("ENV")));
        // 测试的剩余部分
    }

    @Test
    void testOnlyOnDeveloperWorkstation() {
        assumeTrue("DEV".equals(System.getenv("ENV")),
            () -> "Aborting test: not on developer workstation");
        // 测试的剩余部分
    }

    @Test
    void testInAllEnvironments() {
        assumingThat("CI".equals(System.getenv("ENV")),
            () -> {
                // 仅在 CI 服务器上执行这些断言
                assertEquals(2, calculator.divide(4, 2));
            });

        // 在所有环境中执行这些断言
        assertEquals(42, calculator.multiply(6, 7));
    }

}
```

!!! note

	还可以使用 JUnit 4 中 org.junit.Assume 类的方法来进行假设。具体来说，JUnit Jupiter 支持使用 JUnit 4 的 `AssumptionViolatedException` 来表示测试应被中止，而不是标记为失败。

## 异常处理

JUnit Jupiter 提供了强大的支持来处理测试中的异常。这包括内置机制用于管理因异常导致的测试失败、异常在实现断言和假设中的作用，以及如何专门断言代码中的非抛出条件。

### 未捕获异常

在 JUnit Jupiter 中，如果测试方法、生命周期方法或扩展中抛出了异常，并且该异常未在对应的测试方法、生命周期方法或扩展中被捕获，框架将会将该测试或测试类标记为`失败`。

!!! warning "失败的假设偏离了这一普遍规则。"

	与失败的断言不同，失败的`假设(Assumptions)`不会导致测试失败；相反，失败的假设会导致测试被中止。

	请查看`假设(Assumptions)`部分以获取更多详细信息和示例。

在下面的示例中，`failsDueToUncaughtException()` 方法会抛出一个 `ArithmeticException` 异常。由于该异常未在测试方法中被捕获，JUnit Jupiter 会将该测试标记为失败。

```java
private final Calculator calculator = new Calculator();

@Test
void failsDueToUncaughtException() {
    // 以下代码由于除以零会抛出 ArithmeticException，导致测试失败。
    calculator.divide(1, 0);
}
```

!!! note

	指定测试方法中的 throws 子句对测试结果没有任何影响。JUnit Jupiter 不会将 throws 子句解释为对测试方法应该抛出哪些异常的预期或断言。测试仅在意外抛出异常或断言失败时才会失败。

### 断言失败

JUnit Jupiter中的断言是通过异常实现的。框架在`org.junit.jupiter.api.Assertions`类中提供了一组断言方法，当断言失败时会抛出`AssertionError`异常。这种机制是JUnit将断言失败处理为异常的核心部分。有关JUnit Jupiter断言支持的更多信息，请参阅`Assertions`部分。

!!! note

	第三方断言库可能会选择抛出 `AssertionError` 来表示断言失败；然而，它们也可能选择抛出其他类型的异常来表示失败。详见：第三方断言库。

!!! warning

	JUnit Jupiter 本身并不区分失败的断言（AssertionError）和其他类型的异常。所有未捕获的异常都会导致测试失败。然而，集成开发环境（IDE）和其他工具可能会通过检查抛出的异常是否是 AssertionError 的实例来区分这两种类型的失败。

在以下示例中，`failsDueToUncaughtAssertionError()` 方法会抛出一个 `AssertionError` 异常。由于该异常未在测试方法内捕获，JUnit Jupiter 会将该测试标记为失败。

```java
private final Calculator calculator = new Calculator();

@Test
void failsDueToUncaughtAssertionError() {
	// 以下错误的断言将导致测试失败。
	// 预期值应为 2，而不是 99。
    assertEquals(99, calculator.add(1, 1));
}
```

### 断言预期异常

JUnit Jupiter 提供了专门的断言，用于测试在预期条件下是否抛出了特定的异常。`assertThrows()` 和 `assertThrowsExactly()` 是验证代码在错误条件下是否通过抛出适当异常作出正确响应的重要工具。

#### assertThrows()

`assertThrows()` 方法用于验证在执行提供的可执行代码块时是否抛出了特定类型的异常。它不仅检查抛出异常的类型，还会检查其子类，因此非常适合用于更通用的异常处理测试。`assertThrows()` 断言方法会返回抛出的异常对象，从而允许对该对象执行额外的断言操作。

```java
@Test
void testExpectedExceptionIsThrown() {
	// 以下断言会成功，因为断言中的代码抛出了预期的 IllegalArgumentException。
	// 此断言还会返回抛出的异常，可用于进一步的断言，例如验证异常消息。
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("expected message");
        });
    assertEquals("expected message", exception.getMessage());

    // 以下断言也会成功
	// 因为断言中的代码抛出了 `IllegalArgumentException`，而它是 `RuntimeException` 的子类。
    assertThrows(RuntimeException.class, () -> {
        throw new IllegalArgumentException("expected message");
    });
}
```

#### assertThrowsExactly()

assertThrowsExactly() 方法用于断言抛出的异常必须严格是指定的异常类型，而不能是该异常类型的子类。当需要验证异常处理的精确行为时，这个方法非常有用。与 assertThrows() 类似，assertThrowsExactly() 也会返回实际抛出的异常对象，方便你对其进行进一步的断言。

```java
@Test
void testExpectedExceptionIsThrown() {
    // 以下断言会通过，因为被测试代码抛出了 IllegalArgumentException，
    // 与期望的异常类型完全一致。
    // 该断言还会返回抛出的异常对象，可用于进一步断言，例如断言异常消息。
    IllegalArgumentException exception =
        assertThrowsExactly(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("expected message");
        });
    assertEquals("expected message", exception.getMessage());

    // 以下断言会失败，因为断言期望抛出的是 RuntimeException，
    // 而实际抛出的是其子类 IllegalArgumentException，不完全相同。
    assertThrowsExactly(RuntimeException.class, () -> {
        throw new IllegalArgumentException("expected message");
    });
}
```

### 断言不应抛出异常

虽然在测试方法中抛出的任何异常都会导致测试失败，但在某些情况下，明确断言某段代码不会抛出异常也是有益的。assertDoesNotThrow() 断言可用于验证特定代码块在执行时不会抛出任何异常。

```java
@Test
void testExceptionIsNotThrown() {
    assertDoesNotThrow(() -> {
        shouldNotThrowException();
    });
}

void shouldNotThrowException() {
}
```

!!! note

	许多第三方断言库也提供类似的支持。例如，AssertJ 提供了 assertThatNoException().isThrownBy(() → …​) 方法。参见：第三方断言库。

## 禁用测试

可以通过 `@Disabled` 注解、条件测试执行中提到的其他注解，或自定义的 ExecutionCondition，来禁用整个测试类或单独的测试方法。

当 `@Disabled` 注解应用在类级别时，该类中的所有测试方法都会被自动禁用。

如果某个测试方法被 `@Disabled` 注解禁用，那么该测试方法以及方法级的生命周期回调（如 @BeforeEach、@AfterEach 方法和相关扩展 API）都不会被执行。不过，这并不会阻止测试类的实例化，也不会影响类级别的生命周期回调（如 @BeforeAll、@AfterAll 方法和相关扩展 API）的执行。

下面是一个使用 `@Disabled` 注解的测试类示例。

```java
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Disabled until bug #99 has been fixed")
class DisabledClassDemo {

    @Test
    void testWillBeSkipped() {
    }

}
```

下面是一个包含 @Disabled 测试方法的测试类。

```java
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DisabledTestsDemo {

    @Disabled("在 bug #42 修复之前禁用")
    @Test
    void testWillBeSkipped() {
    }

    @Test
    void testWillBeExecuted() {
    }

}
```

!!! warning

	`@Disabled` 注解可以不填写原因，但 JUnit 团队建议开发者简要说明禁用某个测试类或测试方法的原因。因此，上述示例都展示了如何添加原因，例如：`@Disabled("在 bug #42 修复之前禁用")`。有些开发团队甚至要求在原因中注明问题追踪编号，以便实现自动化追溯等功能。

!!! note

	@Disabled 注解并不会被继承。因此，如果你希望禁用一个其父类已被 @Disabled 注解的类，必须在子类上重新声明 @Disabled 注解。

## 条件性测试执行

JUnit Jupiter 中的 `ExecutionCondition` 扩展 API 允许开发者根据特定条件以编程方式启用或禁用测试类或测试方法。最简单的条件示例就是内置的 `DisabledCondition`，它支持 `@Disabled` 注解（详见“禁用测试”）。

除了 `@Disabled` 之外，JUnit Jupiter 还在 `org.junit.jupiter.api.condition` 包中提供了其他基于注解的条件，开发者可以通过这些注解以声明式的方式启用或禁用测试类和测试方法。如果你希望说明禁用的原因，每个与这些内置条件相关的注解都提供了 disabledReason 属性用于此目的。

当注册了多个 ExecutionCondition 扩展时，只要其中一个条件返回禁用，测试类或测试方法就会被禁用。如果一个测试类被禁用，该类中的所有测试方法也会自动被禁用。如果某个测试方法被禁用，该方法以及方法级的生命周期回调（如 @BeforeEach、@AfterEach 方法和相关扩展 API）都不会被执行。不过，这并不会阻止测试类的实例化，也不会影响类级生命周期回调（如 @BeforeAll、@AfterAll 方法和相关扩展 API）的执行。

更多细节请参见 `ExecutionCondition` 及以下相关章节。

!!! note "组合注解"

	请注意，以下各节中列出的任何条件注解都可以作为元注解使用，从而创建自定义的组合注解。例如，在 @EnabledOnOs 示例中，@TestOnMac 注解展示了如何将 @Test 和 @EnabledOnOs 结合为一个可复用的单一注解。

!!! note

	JUnit Jupiter 中的条件注解并不会被 @Inherited 继承。因此，如果你希望子类也具备相同的语义，需要在每个子类上重新声明这些条件注解。

!!! warning

	除非另有说明，下文各节中列出的每个条件注解在同一个测试接口、测试类或测试方法上只能声明一次。如果某个条件注解在同一元素上被直接、间接或通过元注解多次声明，JUnit 只会使用首次发现的注解，其他重复声明将被自动忽略而不作提示。但请注意，每个条件注解都可以与 org.junit.jupiter.api.condition 包中的其他条件注解结合使用。

### 操作系统与架构条件

可以通过 @EnabledOnOs 和 @DisabledOnOs 注解，在特定的操作系统、架构或两者的组合上启用或禁用某个容器或测试。

```java title="根据操作系统进行条件执行"
@Test
@EnabledOnOs(MAC)
void onlyOnMacOs() {
    // ...
}

@TestOnMac
void testOnMac() {
    // ...
}

@Test
@EnabledOnOs({ LINUX, MAC })
void onLinuxOrMac() {
    // ...
}

@Test
@DisabledOnOs(WINDOWS)
void notOnWindows() {
    // ...
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
@EnabledOnOs(MAC)
@interface TestOnMac {
}
```

```java title="根据架构进行条件执行"
@Test
@EnabledOnOs(architectures = "aarch64")
void onAarch64() {
    // ...
}

@Test
@DisabledOnOs(architectures = "x86_64")
void notOnX86_64() {
    // ...
}

@Test
@EnabledOnOs(value = MAC, architectures = "aarch64")
void onNewMacs() {
    // ...
}

@Test
@DisabledOnOs(value = MAC, architectures = "aarch64")
void notOnNewMacs() {
    // ...
}
```

### Java运行环境要求

可以通过 @EnabledOnJre 和 @DisabledOnJre 注解，在特定版本的 Java 运行环境（JRE）上启用或禁用某个容器或测试；也可以通过 @EnabledForJreRange 和 @DisabledForJreRange 注解，在特定版本范围的 JRE 上进行控制。该范围默认以 JRE.JAVA_8 作为下限，JRE.OTHER 作为上限，这样可以实现半开区间的使用。

下面的示例展示了如何结合预定义的 JRE 枚举常量来使用这些注解。

```java
@Test
@EnabledOnJre(JAVA_17)
void onlyOnJava17() {
    // ...
}

@Test
@EnabledOnJre({ JAVA_17, JAVA_21 })
void onJava17And21() {
    // ...
}

@Test
@EnabledForJreRange(min = JAVA_9, max = JAVA_11)
void fromJava9To11() {
    // ...
}

@Test
@EnabledForJreRange(min = JAVA_9)
void onJava9AndHigher() {
    // ...
}

@Test
@EnabledForJreRange(max = JAVA_11)
void fromJava8To11() {
    // ...
}

@Test
@DisabledOnJre(JAVA_9)
void notOnJava9() {
    // ...
}

@Test
@DisabledForJreRange(min = JAVA_9, max = JAVA_11)
void notFromJava9To11() {
    // ...
}

@Test
@DisabledForJreRange(min = JAVA_9)
void notOnJava9AndHigher() {
    // ...
}

@Test
@DisabledForJreRange(max = JAVA_11)
void notFromJava8To11() {
    // ...
}
```

由于 JRE 中定义的枚举常量在每个 JUnit 版本中都是静态的，因此你可能会遇到需要配置 JRE 枚举尚未支持的 Java 版本的情况。例如，截至 JUnit Jupiter 5.12，JRE 枚举中最高只支持到 JAVA_25。然而，你可能希望在更高版本的 Java 上运行测试。为满足这类需求，你可以通过 @EnabledOnJre 和 @DisabledOnJre 注解中的 versions 属性，或通过 @EnabledForJreRange 和 @DisabledForJreRange 注解中的 minVersion 和 maxVersion 属性，指定任意 Java 版本。

下面的示例演示了如何使用这些注解来支持任意 Java 版本。

```java
@Test
@EnabledOnJre(versions = 26)
void onlyOnJava26() {
    // ...
}

@Test
@EnabledOnJre(versions = { 25, 26 })
// Can also be expressed as follows.
// @EnabledOnJre(value = JAVA_25, versions = 26)
void onJava25And26() {
    // ...
}

@Test
@EnabledForJreRange(minVersion = 26)
void onJava26AndHigher() {
    // ...
}

@Test
@EnabledForJreRange(minVersion = 25, maxVersion = 27)
// Can also be expressed as follows.
// @EnabledForJreRange(min = JAVA_25, maxVersion = 27)
void fromJava25To27() {
    // ...
}

@Test
@DisabledOnJre(versions = 26)
void notOnJava26() {
    // ...
}

@Test
@DisabledOnJre(versions = { 25, 26 })
// Can also be expressed as follows.
// @DisabledOnJre(value = JAVA_25, versions = 26)
void notOnJava25And26() {
    // ...
}

@Test
@DisabledForJreRange(minVersion = 26)
void notOnJava26AndHigher() {
    // ...
}

@Test
@DisabledForJreRange(minVersion = 25, maxVersion = 27)
// 也可以这样写。
// @DisabledForJreRange(min = JAVA_25, maxVersion = 27)
void notFromJava25To27() {
    // ...
}
```
