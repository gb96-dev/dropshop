# 기여 가이드

## 코드 스타일 설정 (1차)

클론 후 아래 명령을 한 번 실행해 저장소 훅을 활성화하세요.

```bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit
```

## 로컬 실행 명령

```bash
./gradlew --no-daemon --console=plain spotlessApply
./gradlew --no-daemon --console=plain spotlessCheck
./gradlew --no-daemon --console=plain checkstyleMain checkstyleTest
```

- `spotlessApply`: Java 코드를 자동 포맷하고 가능한 경우 사용하지 않는 import를 정리합니다.
- `spotlessCheck`: 파일을 수정하지 않고 포맷 준수 여부만 검사합니다.
- `checkstyleMain/checkstyleTest`: 포매터로 자동 수정되지 않는 스타일 위반을 리포트합니다.


