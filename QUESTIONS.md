<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <version>3.0.0-M9</version>
    <configuration>
        <argLine>${argLine} -Dquarkus.profile=integrationtest</argLine>
        <systemPropertyVariables>
            <quarkus.profile>integrationtest</quarkus.profile>
        </systemPropertyVariables>
    </configuration><executions>
        <execution>
            <goals>
                <goal>integration-test</goal>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
</plugin># Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**
```txt
I believe the Open API yaml approach has better advantages in terms of maintainability and consistency. It allows for a clear contract between the API and its consumers, and the generated code can help reduce boilerplate and ensure that the implementation stays in sync with the specification. However, it can add complexity to the development process, especially if the team is not familiar with OpenAPI or if the API is simple enough that the overhead of maintaining a separate yaml file is not justified.
On the other hand, hand-coding the endpoints can be faster for simple APIs and allows for more flexibility in implementation. However, it can lead to inconsistencies and make it harder to maintain the API as it evolves, especially if there are multiple developers working on it. So, in a production grade system Open API yaml approach suits better for the API from maintainability perspective and I would definitely go with it.
```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would focus on a balanced testing strategy that prioritizes integration tests for critical paths and unit tests for core business logic. Given the nature of the project, I would start by writing integration tests that cover the main API endpoints to ensure that the system works end-to-end. Then I would add multiple validation unit tests to cover the business rules and edge cases. Parameterized tests can be useful for testing various input scenarios without duplicating code, so I would use them for validation logic that has multiple input combinations. Concurrency tests are required to ensure data integrity, normally to prevent the race conditions usually occur in production. To ensure effective coverage over time, I would set up a continuous integration pipeline that runs all tests on every commit and periodically review test coverage reports and publishes success/failure status based on coverage ratio.
```
