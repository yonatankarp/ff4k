# Why I Built a Kotlin-Native Feature Flag Library

## Introduction

It started with a security task: upgrading Spring Boot by three minor versions. Simple, right?

After hours with a broken build, I finally found the culprit. Spring Boot had deprecated javax annotations in favor of jakarta, and our feature flag library — FF4J — didn't support the change.

No problem, I thought. It's open source. I'll contribute a fix.

I searched for existing issues and found [this response from the maintainers](link):

> "The project has been dormant for a little while. If you propose some PR we will still review them and engage to do a needed release, but we are not working actively on it — just logging bugs."

Still, I pushed forward. With Claude's help, I replaced all javax references with jakarta and opened a pull request. It was merged relatively quickly, but it requires a new major release. As of this writing, there's no timeline for that release — if it happens at all.

That's when the thought hit me: I could build this myself. And make it Kotlin-idiomatic. And multiplatform.

So I did.

---

## The Problem

FF4J served the Java community well for years. But it's now in maintenance limbo — no active development, accumulating issues, and breaking with modern dependency updates.

For teams already using Kotlin, the options are limited:

- **Keep using FF4J**: Risk more breakages, no new features, uncertain future
- **Switch to a paid service**: LaunchDarkly, Split, etc. — great products, but overkill for many use cases
- **Use a Java wrapper**: Lose Kotlin's ergonomics, fight with nullability, no multiplatform support

None of these felt right. Kotlin deserves a native solution — one that embraces coroutines, DSLs, multiplatform, and the type safety we've come to expect.

That's the gap FF4K fills.

---

## The Vision

FF4K isn't just a feature toggle library — it's a complete feature management platform for Kotlin:

**Core Capabilities:**
- **Feature Flags**: Toggle functionality on/off at runtime without redeployment
- **Properties**: Type-safe configuration values (strings, numbers, dates, custom types) — not just booleans
- **Flipping Strategies**: Control *when* and *for whom* features are enabled — percentage rollouts, user targeting, time-based scheduling, and composable logic

**Extensibility at Every Layer:**
- **Custom Strategies**: Build your own flipping logic for domain-specific rules
- **Custom Property Types**: Define typed properties beyond the built-ins
- **Contract Tests**: Verify your custom implementations work correctly

**Production-Ready Infrastructure:**
- **Persistence**: Plug in your own storage backend — in-memory, SQL, NoSQL, whatever fits your stack
- **Caching**: Reduce latency and database load
- **Platform-Specific Integrations**: Redis, Spring Boot, and more (JVM-only where necessary)
- **Monitoring & Auditing**: Track feature usage and changes

**Cross-Platform by Design:**
- Shared core across JVM, Android, and iOS
- Platform-specific extensions where they make sense

The goal: whether you need a simple toggle or a sophisticated rollout system with persistence and caching, FF4K scales with you.

---

## What Makes FF4K Different

**Kotlin-Native, Not a Wrapper**

FF4K isn't a Java library with Kotlin bindings. It's built from the ground up with:
- Idiomatic DSLs for configuration
- Coroutines and suspend functions throughout
- Null safety baked in, not bolted on

**Truly Multiplatform**

One library, one API — whether you're building a backend service, an Android app, or an iOS app. Share feature flag logic across your entire stack.

**No Vendor Lock-In**

Feature flags are infrastructure. You shouldn't need a SaaS subscription for basic functionality, and you shouldn't be locked into a single provider's ecosystem.

**Modern Dependencies**

Built on kotlinx.serialization and kotlinx.datetime — no legacy Java baggage, timezone-aware from day one.

---

## Where We Are Today

FF4K is in its early stages. The core functionality is stable and tested, but there's much more to build:

✅ Core feature flag management
✅ Multiple flipping strategies
✅ Type-safe properties
✅ Multiplatform support (JVM, Android, iOS)
✅ Contract testing framework

🚧 Coming soon:
- [Your roadmap items]
- [Community-requested features]
- [Integration with popular frameworks]

---

## Why Open Source

Since around 2013, when I started learning how to code professionally, I've always wanted to contribute to the open-source community. But I never quite knew where or how.

For years, I had this small personal dream: publishing my own library to Maven Central. Not because the world desperately needed it — but because I wanted to leave a tiny, immutable mark somewhere.

I never really knew what that library should be. At one point, I even considered writing a Kotlin SDK for the famous cat-facts API.

Then the FF4J incident happened, and I finally found my direction.

Over the following weeks, I implemented the core: typed features and properties, extensibility, and a clean DSL. Along the way, I got a boost I didn't expect — someone I'd never met reached out asking to work on an issue I'd marked as "good first issue." Knowing that someone found the project interesting enough to invest their time genuinely meant a lot.

FF4K is now live on Maven Central.

It's early. The API will evolve. But that tiny, immutable mark? It's finally there.

---

## Get Involved

FF4K is at the stage where real-world usage matters most. If you're building Kotlin applications and want to try a native feature flag solution, I'd love your feedback:

- Try it in a side project or proof of concept
- Tell me what feels intuitive and what doesn't
- Report rough edges in the API

This isn't about feature requests — it's about validating that the foundation is solid before building more on top.

- **GitHub**: https://github.com/yonatankarp/ff4k
- **Documentation**: https://yonatankarp.github.io/ff4k

---

## Closing

Feature flags shouldn't be complicated. They shouldn't require a paid service for basic functionality. And for Kotlin developers, they should feel like Kotlin.

That's what FF4K aims to be.

[Your sign-off]

---

## Publishing Tips

1. **Include a diagram** - Architecture or how strategies compose
2. **Be vulnerable** - Early stage = opportunity for readers to shape it
3. **Cross-post** - Share on dev.to, Hashnode, LinkedIn for reach
4. **Tags**: `kotlin`, `feature-flags`, `open-source`, `kotlin-multiplatform`, `android`