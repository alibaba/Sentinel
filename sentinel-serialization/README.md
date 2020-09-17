# sentinel-serialization

Sentinel serialization module.

The purpose of abstracting serialization functions is to keep the space using different JSON (and others in future) libraries for users. 

## Why you may care about it

### Security Reason

As we know serialization libraries are usually vulnerable because they often stands on the edge in an architecture and maybe can be accessed directly from outside.

So if some security events happened you may want to do whether an updating of affected library or replace it using another.

### Reuse Existing Library

Serialization library is used widely (eg. json) you may have already had one in dependencies of your project. So it's better using existing one instead of a new one, right?

## How to begin

### No Extra Work

`sentinel-serialization-common` makes a default implementation which adapts to `gson` and `fastjson` in priority and introduces a new dependency `gson` in its pom.xml. So you can use sentinel as what you did before. Nothing different except `gson` replaces `fastjson` automatically.

### Continue to Use `fastjson`

But if you still want to use fastjson and don't want to introduce `gson` into your project, you can easily achieve that by excluding `gson` in your pom.xml. Make sure you have an available fastjson and everything would work with fastjson.

Due to several security alerts in recent releases of fastjson you'd better check which version to use carefully in your project.

### Use `jackson`

`jackson` is another popular json serialization library in `springframework`. If you want to use it instead of `gson` you will have an extra step to do after excluding `gson` (optional because it will not be used actually in this case): Introduce `sentinel-serialization-jackson` into your project as shown below:

```xml
<dependency>
	<groupId>com.alibaba.csp</groupId>
	<artifactId>sentinel-serialization-jackson</artifactId>
	<version>${keep_consistent_with_sentinel}</version>
</dependency>
```

### Use A New Library Unsupported Currently

Normally `gson` / `fastjson` / `jackson` will cover most of cases in practice. But if you indeed have a new library especially an internal maintained one you can use SPI interfaces `com.alibaba.csp.sentinel.serialization.common.JsonSerializer` and `com.alibaba.csp.sentinel.serialization.common.JsonDeserializer` to get your own implementation referenced by sentinel. Hope the new library supports `Class` and `Type` deserializing or you will have to support it manually.

