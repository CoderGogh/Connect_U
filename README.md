<div align="center">
  <h1>🔗 Connect_U</h1>
  <p><strong>통합 게시판 플랫폼 — 게시글 · 댓글 · 팔로우 · 검색을 하나로</strong></p>
  <p>
    <img src="https://img.shields.io/badge/Java%2017-007396?style=flat-square&logo=openjdk&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring_Boot%203.5-6DB33F?style=flat-square&logo=spring-boot&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
    <img src="https://img.shields.io/badge/Thymeleaf-005F0F?style=flat-square&logo=thymeleaf&logoColor=white"/>
    <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
    <img src="https://img.shields.io/badge/Google_Cloud_SQL-4285F4?style=flat-square&logo=googlecloud&logoColor=white"/>
    <img src="https://img.shields.io/badge/Google_Cloud_Storage-4285F4?style=flat-square&logo=googlecloud&logoColor=white"/>
    <img src="https://img.shields.io/badge/Swagger-85EA2D?style=flat-square&logo=swagger&logoColor=black"/>
    <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white"/>
  </p>
  <p>
    <img src="https://img.shields.io/badge/배포_상태-미배포-lightgrey?style=flat-square"/>
    <img src="https://img.shields.io/badge/팀-Ureka_Team_9-blue?style=flat-square"/>
  </p>
</div>

---

## 📌 목차

1. [프로젝트 소개](#-프로젝트-소개)
2. [기술 스택](#-기술-스택)
3. [주요 기능](#-주요-기능)
4. [프로젝트 구조](#-프로젝트-구조)
5. [커스텀 어노테이션](#-커스텀-어노테이션)
6. [시작하기](#-시작하기)
7. [API 문서](#-api-문서)

---

## 📋 프로젝트 소개

**Connect_U** 는 사용자들이 자유롭게 글을 쓰고, 서로를 팔로우하며 소통할 수 있는 **통합 게시판 플랫폼**입니다.

게시글 작성·수정·삭제, 트리 구조의 댓글·대댓글, 팔로우/언팔로우, 키워드 기반 검색 등 커뮤니티 서비스의 핵심 기능을 Spring Boot 기반으로 구현했습니다. 인프라는 **Google Cloud SQL(MySQL)** 과 **Google Cloud Storage** 를 활용하여 클라우드 환경에서 운영할 수 있도록 설계되었습니다.

---

## 🛠 기술 스택

### Backend

| 기술 | 버전 | 용도 |
| :--- | :---: | :--- |
| Java | 17 | 메인 언어 |
| Spring Boot | 3.5.8 | 애플리케이션 프레임워크 |
| Spring Security | - | 인증 / 인가 처리 |
| Spring Data JPA | - | ORM 기반 DB 접근 |
| Spring Validation | - | 입력값 유효성 검사 |
| Thymeleaf | - | 서버사이드 템플릿 렌더링 |
| Swagger (springdoc) | 2.6.0 | API 문서 자동화 |
| Lombok | - | 보일러플레이트 코드 제거 |
| Commons Lang3 | 3.18.0 | 유틸리티 라이브러리 |

### Infrastructure

| 기술 | 용도 |
| :--- | :--- |
| Google Cloud SQL (MySQL 8) | 프로덕션 데이터베이스 |
| Google Cloud Storage | 프로필 이미지 / 게시글 이미지 파일 저장 |
| Google Auth OAuth2 | GCS 인증 처리 |
| MySQL (로컬) | 로컬 개발 환경 DB |

### Frontend

| 기술 | 용도 |
| :--- | :--- |
| Thymeleaf | HTML 템플릿 |
| JavaScript | 클라이언트 인터랙션 |
| CSS | 스타일링 |

---

## ✨ 주요 기능

### 👤 회원 (User)

- Spring Security 기반 회원가입 / 로그인 / 로그아웃
- 프로필 이미지 업로드 (Google Cloud Storage 저장)
- 키워드 기반 회원 닉네임 검색

### 🤝 팔로우 / 언팔로우 (Follow)

- 다른 회원 팔로우 / 언팔로우
- 팔로워 · 팔로잉 목록 조회

### 📝 게시글 (Post)

- 게시글 작성 · 수정 · 삭제
- 게시글 이미지 업로드 (Google Cloud Storage 저장)
- 키워드 기반 게시글 제목 / 본문 검색

### 💬 댓글 · 대댓글 (Comment)

- 게시글에 댓글 작성 · 수정 · 삭제
- 트리 구조(재귀적 부모-자식 관계) 기반 대댓글 지원
- 깊이 제한 없는 다단계 답글 구현

### 🔍 검색 (Search)

- 닉네임 키워드로 회원 검색
- 제목 / 본문 키워드로 게시글 검색

---

## 📂 프로젝트 구조

```
Connect_U/
├── src/
│   ├── main/
│   │   ├── java/connect/connectu/
│   │   │   ├── domain/
│   │   │   │   ├── user/          # 회원 (Controller, Service, Repository, Entity)
│   │   │   │   ├── post/          # 게시글
│   │   │   │   ├── comment/       # 댓글 · 대댓글 (트리 구조)
│   │   │   │   └── follow/        # 팔로우 · 언팔로우
│   │   │   ├── global/
│   │   │   │   ├── auth/          # Spring Security 설정 / @CurrentUsersId 어노테이션
│   │   │   │   └── config/        # GCS, JPA, Security 설정 클래스
│   │   │   └── ConnectUApplication.java
│   │   └── resources/
│   │       ├── templates/         # Thymeleaf 템플릿
│   │       ├── static/            # JS, CSS
│   │       └── application.yml
│   └── test/
├── docs/                          # 문서 (ERD 등)
├── build.gradle
└── README.md
```

---

## 🔖 커스텀 어노테이션

### `@CurrentUsersId`

인증된 사용자의 식별자(`usersId`)를 세션에서 꺼내 컨트롤러 파라미터에 직접 주입하는 커스텀 어노테이션입니다.

**옵션: `required`**

| 값 | 의미 |
| :---: | :--- |
| `true` (기본값) | 인증이 필수 — 비인증 요청 시 예외 발생 |
| `false` | 인증이 선택 — 비인증 사용자도 접근 가능하며 `null` 반환 |

**사용 예시**

```java
// 인증이 필수인 경우 (예: 게시글 작성, 회원 정보 수정)
@PostMapping("/posts")
public ResponseEntity<?> createPost(@CurrentUsersId Integer usersId, ...) {
    ...
}

// 인증이 선택인 경우 (예: 게시글 조회 — 비로그인도 가능하지만, 로그인 시 추가 정보 제공)
@GetMapping("/posts/{postId}")
public ResponseEntity<?> getPost(
        @CurrentUsersId(required = false) Integer usersId,
        @PathVariable Long postId) {
    ...
}
```

---

## 🚀 시작하기

### 사전 요구사항

- Java 17+
- Gradle
- MySQL 8.0 (로컬 개발 시)
- Google Cloud 프로젝트 (GCS · Cloud SQL 사용 시)

### 로컬 실행

**1. 레포지토리 클론**

```bash
git clone https://github.com/CoderGogh/Connect_U.git
cd Connect_U
```

**2. 환경 변수 / 설정 파일 구성**

`src/main/resources/application.yml` (또는 `application-local.yml`)에 아래 항목을 설정합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/connect_u
    username: {DB_USER}
    password: {DB_PASSWORD}

gcs:
  bucket-name: {GCS_BUCKET_NAME}
  credentials-path: {서비스_계정_키_파일_경로}
```

> Google Cloud SQL을 사용하는 경우 `mysql-socket-factory`를 통해 연결됩니다.
> 로컬 개발 시에는 일반 MySQL 연결로 대체할 수 있습니다.

**3. 빌드 및 실행**

```bash
./gradlew build
./gradlew bootRun
```

**4. 접속**

```
http://localhost:8080
```

---

## 📖 API 문서

Swagger UI를 통해 전체 API 명세를 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

> Spring Security가 적용되어 있으므로, 로그인 후 세션 쿠키가 있어야 인증이 필요한 API를 테스트할 수 있습니다.

---

<div align="center">
  <sub>Developed by Ureka Team 9</sub>
</div>
