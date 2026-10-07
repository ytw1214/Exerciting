# 1단계: 빌드
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# 2단계: 실행 (JRE만)
FROM eclipse-temurin:17-jre
ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
# 비밀값은 이미지에 넣지 않고 실행 시 환경 변수로 넘긴다:
#   docker run --env-file .env -p 8080:8080 exerciting
# Selenium 크롤링은 Chrome이 없는 이 이미지에서는 동작하지 않는다(크롤러는 별도 실행 권장).
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
