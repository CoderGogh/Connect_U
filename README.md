# Connect_U-

## @CurrentUsersId 어노테이션
- 용도: 인증된 사용자의 식별자(usersId)를 세션에서 가져오는 기능을 수행
- 사용법: 메서드의 `Integer 타입 파라미터`에 적용

### 옵션: required
- 의미: **‘인증 필수 여부’** 를 의미하는 설정 값 *(기본값: true)*
- 인증이 필수가 아닌 API의 경우 해당 옵션을 비활성화
    - ex) 게시글 조회 시 usersId가 필요하나 필수는 아닌 경우 등

### 사용 방법

인증 정보가 **필수**인 경우<br>
```java
// ex) '회원 정보 수정'과 같이 인증 정보가 필수인 경우
@GetMapping("/test-api")
public ResponseEntity<?> m(@CurrentUsersId Integer usersId) {
  ...
}
```

인증 정보가 **필수가 아닌** 경우
```java
// ex) '게시글 조회'처럼 인증 정보를 활용하기는 하나, 필수는 아닌 경우
@GetMapping("/test-api")
public ResponseEntity<?> m(@CurrentUsersId(required = false) Integer usersId) {
  ...
}
```