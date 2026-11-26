# Student Registration

A minimal Java 21 application that serves a student registration form and accepts submissions in memory. It does not store student data.

## Run locally

```sh
mvn package
java --add-modules jdk.httpserver -jar target/student-registration-1.0.0.jar
```

Open <http://localhost:8080>. The form requires a student name, roll number, valid email address, and course.

## Jenkins

Add JDK 21 and Maven installations in Jenkins Global Tool Configuration using the names `JDK 21` and `Maven 3`. Create a Pipeline job pointing at this repository and use the repository's `Jenkinsfile`.

## GitHub Actions

The workflow at `.github/workflows/maven.yml` runs `mvn -B clean verify` on pushes and pull requests.

## Docker

```sh
docker build -t student-registration .
docker run --rm -p 8080:8080 student-registration
```

Open <http://localhost:8080> to use the form.
