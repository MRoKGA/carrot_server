# 🥕당근마켓🥕

![readme_mockup2](https://github.com/MRoKGA/image/blob/main/%EC%A0%9C%EB%AA%A9%EC%9D%84%20%EC%9E%85%EB%A0%A5%ED%95%B4%EC%A3%BC%EC%84%B8%EC%9A%94..png?raw=true)

- 배포 URL : https://d1elknx4d22bup.cloudfront.net (점검 중)
- 배포 서버 : http://3.35.219.116:8080 (점검 중)

<br>

## 프로젝트 소개

- “지역 기반 거래 플랫폼을 실제 서비스 아키텍처로 구현한 당근마켓 클론 프로젝트입니다.”
- “실제 서비스 수준의 로그인·상품·채팅·예약·이미지 업로드 기능을 갖춘 풀스택 프로젝트입니다.”
- "서비스 전체 흐름을 경험하기 위해 진행한 클론코딩입니다.”

<br>

## 팀원 구성

<div align="center">

| **이동국** | **홍재호** | **이의준** |
| :------: |  :------: | :------: |
| [<img src="https://avatars.githubusercontent.com/u/39736916?v=4" height=150 width=150> <br/> @LeeDongGuk](https://github.com/leedongguk) | [<img src="https://avatars.githubusercontent.com/u/51096442?v=4" height=150 width=150> <br/> @ariana9rande](https://github.com/ariana9rande) | [<img src="https://avatars.githubusercontent.com/u/136044552?v=4" height=150 width=150> <br/> @euijunlee98](https://github.com/euijunlee98) |

</div>

<br>

## 1. 개발 환경

- Front : HTML, React, styled-components
- Back-end : Spring-Boot
- 버전 및 이슈관리 : Github, Github Issues, Github Project
- 협업 툴 : Discord, Notion, Github Wiki
- 서비스 배포 환경 : AWS
- 사용 기술 및 API: COOL SMS API, KAKAO MAP, KAKAO PAY, SWAGGER, CI/CD, CHATGPT 
- 디자인 : Figma
<br>

## 2. 채택한 개발 기술과 브랜치 전략

### React, styled-component

- React
    - 컴포넌트화를 통해 추후 유지보수와 재사용성을 고려했습니다.
    - 유저 배너, 상단과 하단 배너 등 중복되어 사용되는 부분이 많아 컴포넌트화를 통해 리소스 절약이 가능했습니다.

- AWS 기반 배포(EC2, S3, RDS)
  - EC2를 활용해 React 프론트엔드와 Spring Boot 백엔드를 실시간으로 운영 가능한 형태로 배포했습니다.
  - S3를 이용해 상품 이미지 업로드 및 정적 파일 관리를 진행하며 저장 비용과 안정성을 확보했습니다.
  - RDS(MySQL)를 도입하여 데이터의 안정적인 저장 및 백업/관리 환경을 구축했습니다.
  - CI/CD 파이프라인을 구성해 GitHub Actions 기반 자동 배포 환경을 구축함으로써 코드를 push하는 즉시 자동 빌드·배포가 이루어지는 지속적 배포 환경을 완성했습니다.
 
 - COOL SMS API (문자 본인인증)
  - 회원가입 과정에서 문자 인증 번호 발송 기능을 구현해 사용자 신뢰성과 보안 레벨을 강화했습니다.
  - 인증 절차를 REST API와 연동하여, 유저 경험(UX) 손상 없이 빠르게 인증 절차를 완료할 수 있는 구조를 만들었습니다.

 - Kakao Map API
  - 사용자 위치 기반으로 상품을 조회하거나 등록할 수 있도록 Kakao Map을 이용해 지도 기반 UI 기능을 구현했습니다.
  - 주소 검색, 좌표 변환, 마커 커스터마이징 등 지도 서비스의 핵심 기능을 연동해 동네 기반 플랫폼으로서의 특성을 완성했습니다.

 - Swagger를 활용한 API 문서화
  - Swagger UI를 통해 백엔드 API 전체를 문서화하여, 팀원 간의 API 소통 비용을 크게 줄이고 개발 효율성을 향상시켰습니다.
  - API 스펙 변경 시 자동 문서 업데이트가 가능해 유지보수 과정에서도 높은 생산성을 확보했습니다.

 - ChatGPT를 활용한 에러 로그 자동 분석
  - 프론트·백엔드에서 발생하는 오류 로그를 ChatGPT API로 전달하여 자동으로 에러 원인을 분석하고 해결 방법을 제안받는 시스템을 구축했습니다.
  - 이를 통해 디버깅 시간을 크게 단축시키고, 개발 생산성과 안정성을 향상했습니다.

<br>

## 노션관리

![readme_mockup2](https://github.com/MRoKGA/image/blob/main/screencapture-notion-so-245269b368be803a9bbbecb07a83a157-2025-11-27-18_08_22.png?raw=true)

<br>

## 3. 역할 분담

### 🍊이동국

- **FULL-STACK**

<br>
    
### 👻홍재호

- **BACK-END**

<br>

### 😎이의준

- **BACK-END**

<br>

## 4. 개발 기간 및 작업 관리

### 개발 기간

- 전체 개발 기간 : 2025-09-01 ~ 2025-11-27
- UI 구현 : 2025-09-01 ~ 2025-09-08
- 기능 구현 : 2025-09-09 ~ 2025-11-05
- 테스트 : 2025-11-05 ~ 2025-11-27

<br>

### 작업 관리

- GitHub 및 NOTION을 통해 진행 상황을 공유했습니다.
- 주간회의를 진행하며 작업 순서와 방향성에 대한 고민을 나누고 NOTION에 회의 내용을 기록했습니다.

<br>

## 5. 신경 쓴 부분

- ChatGPT를 활용한 에러 로그 자동 분석
![readme_mockup2](https://github.com/MRoKGA/image/blob/main/%EC%97%90%EB%9F%AC%EA%B4%80%EB%A6%AC.png?raw=true)

- SWAGGER를 활용한 API 문서
![readme_mockup2](https://github.com/MRoKGA/image/blob/main/%EC%8A%A4%EC%9B%A8%EA%B1%B0.png?raw=true)

<br>

## 6. 페이지별 기능

### [초기화면]
- 기능 설명
    - 서비스 접속 시 가장 먼저 나타나는 화면
    - 시작하기(회원가입)
    - 로그인(핸드폰 인증기반 로그인)

| 초기화면 |
|----------|
|![splash](https://github.com/MRoKGA/image/blob/main/1.png?raw=true)|

<br>

### [회원가입(동네설정)]
- 입력창에 지역을 입력하면 지역이 검색됩니다.
- 현재위치로 찾기로 클릭 시 GPS기반으로 동네설정이 가능합니다.

| 회원가입(동네설정) |
|----------|
|![join](https://github.com/MRoKGA/image/blob/main/1-2.png?raw=true)|

<br>

### [회원가입(핸드폰번호 입력)]
- 핸드폰 번호를 입력하면 핸드폰 인증번호를 받을 수 있습니다.
- 핸드폰 번호를 입력하지 않으면 다음 화면으로 넘어 갈 수 업습니다.

| 회원가입(핸드폰번호 입력) |
|----------|
|![join](https://github.com/MRoKGA/image/blob/main/1-3.png?raw=true)|

<br>

### [회원가입(핸드폰번호 인증)]
- 사용자가 입력한 휴대폰 번호로 CoolSMS API를 통해 인증번호(6자리)를 발송하고,
  이 인증번호를 Redis 서버에 임시 저장(5분 TTL)한 뒤 사용자가 입력한 인증번호와 Redis에 저장된 인증번호를 비교하여 본인인증을 완료하는 구조입니다.
- 기술 흐름<br>
  1.사용자가 휴대폰 번호 입력 → 인증번호 요청<br>
  2.서버는 랜덤 6자리 인증번호 생성<br>
  3.CoolSMS API로 해당 번호로 인증번호 발송<br>
  4.인증번호를 REDIS에 저장<br>
  5. 사용자가 인증번호 입력<br>
  6. 서버는 REDIS에 저장된 값과 비교<br>
  7. 일치 -> 본인인증 자동 성공<br>
     불일치/만료 -> 인증 실패 처리<br>

| 회원가입(핸드폰번호 인증) |
|----------|
|![setProfile](https://github.com/MRoKGA/image/blob/main/1-4.png?raw=true)|

<br>

### [프로필 설정]
- 카메라 아이콘 클릭 시 이미지 선택
- 선택된 이미지 파일은 프론트에서 FormData로 백엔드 전송
- 백엔드는 파일을 AWS S3 버킷에 업로드
- S3에 저장된 파일의 URL을 DB에 저장하여 프로필 이미지로 사용
- 이미지를 바꾸면 자동으로 “완료” 버튼이 활성화됨
- 입력창에 새 닉네임을 입력하면 중복확인 버튼 활성화
- 중복확인 API 호출 → 사용 가능 여부 반환
- 사용 가능: 초록색 메시지, 저장 가능
- 중복됨: 빨간색 메시지, 저장 불가
- 실제 서비스와 동일하게 “닉네임 고유성”을 확보

| 프로필 설정 |
|----------|
|![login](https://github.com/MRoKGA/image/blob/main/1-5.png?raw=true)|

<br>

### [홈 화면]
- 상단 카테고리 필터
- 지역(동네) 기반 상품 리스트
- 상품 썸네일 + 정보 표시
- 하단 Floating Button (상품 등록 버튼)
- 검색 버튼

| 홈 화면 |
|----------|
|![login](https://github.com/MRoKGA/image/blob/main/2.png?raw=true)|

<br>

### [상품 상세]
- 상품 이미지 슬라이드
- 판매자 정보 표시
- 좋아요 기능
- 조회수 기능
- 채팅하기 기능
- 선호 거래 지역(지도 표시)

| 상품 상세 |
|----------|
|![login](https://github.com/MRoKGA/image/blob/main/2-1(%EC%98%81%EC%83%81).gif?raw=true)|

<br>

### [상품 검색]
- 상품 검색

| 상품 검색 |
|----------|
|![login](https://github.com/MRoKGA/image/blob/main/2-2(%EC%98%81%EC%83%81).gif?raw=true)|

<br>

### [내 물건 팔기]
- 상품 이미지 업로드 (0/5)
- 실제 이미지는 백엔드로 전송되어 AWS S3 버킷에 저장
- 카테고리 선택
- 거래 방식 선택 (판매하기 / 나눔하기)
- 역제안 받기(가격 제안) 옵션
- 거래 희망 장소 설정 (Kakao Map API 팝업)


| 내 물건 팔기 | 위치 찾기 |
|----------|----------|
|![login](https://github.com/MRoKGA/image/blob/main/2-3.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/2-4.png?raw=true)|

<br>

### [동네생활 피드]
- 동네 모임/주제 카테고리 노출
- 전체 / 카테고리 필터
- 게시글 리스트
- 인기 게시글(“맛집 추천합니다!” 등)
- 좋아요 기능
- 조회수 증가
- 게시글 본문 + 이미지 표시
- 지도 기반 위치 표시
- 댓글 기능 (CRUD)
- 본인 글 수정/삭제 메뉴


| 동네생활 피드 | 동네생활 게시물 |
|----------|----------|
|![login](https://github.com/MRoKGA/image/blob/main/3-1.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/3-2.png?raw=true)|

<br>

### [동네생활 글쓰기]
- 게시글 주제 선택 Ex)동네정보, 이웃과함께, 소식
- 질문글 여부 체크박스
- 주제 클릭 시 즉시 선택 & 팝업 닫힘
- 선택된 주제는 글쓰기 상단에 표시


| 동네생활 글쓰기 | 카테고리 팝업 |
|----------|----------|
|![login](https://github.com/MRoKGA/image/blob/main/3-3.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/3-4.png?raw=true)|

<br>

### [모임 만들기]
- 대표 이미지 업로드
- 대표 동네 표시
- 모임 이름 입력
- 공개범위 설정(PUBLIC, PRIVATE)
- 가입정책 선택(OPEN, APPROVAL, CLOSED)


| 모임 만들기 | 모임 썸네일 |
|----------|----------|
|![login](https://github.com/MRoKGA/image/blob/main/3-5.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/3-6.png?raw=true)|

<br>



### [동네모임 상세 화면]
- 모임 대표 정보
- 관리기능
- 모임 탈퇴 기능
- 이벤트 리스트 표시
- 가입정책 선택(OPEN, APPROVAL, CLOSED)
- 모임 만들기
- 지도에서 위치 선택 기능


| 모임 만들기 | 모임 썸네일 | 모임 썸네일 |
|----------|----------|----------|
|![login](https://github.com/MRoKGA/image/blob/main/3-7.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/3-8.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/3-9.png?raw=true)|

<br>

### [거래 채팅]
- 실시간 메시지 전송
- 판매자 매너온도 표시
- 상품 정보 고정 영역
- 약속잡기 기능(핵심)
- 약속잡기 입력 폼 기능
- 약속 제안 메시지 자동 생성
- 약속 취소 기능
- 당근페이(카카오페이 연동)


| 채팅 | 약속잡기 | 모임 썸네일 |
|----------|----------|----------|
|![login](https://github.com/MRoKGA/image/blob/main/5-9.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/%EC%95%BD%EC%86%8D%EC%9E%A1%EA%B8%B0.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/5-8.png?raw=true)|

| 거래1 | 거래2 |
|----------|----------|
|![login](https://github.com/MRoKGA/image/blob/main/5-6.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/5-7.png?raw=true)|![login]

| 거래3 | 거래4 |
|----------|----------|
|![login](https://github.com/MRoKGA/image/blob/main/%EC%B9%B4%EC%B9%B4%EC%98%A4%ED%8E%98%EC%9D%B4%EA%B2%B0%EC%A0%9C.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/%EA%B2%B0%EC%A0%9C%20%EC%B9%B4%EC%B9%B4%EC%98%A4%ED%86%A1.png?raw=true)|![login]

| 거래5 |
|----------|
|![login](https://github.com/MRoKGA/image/blob/main/5-10.png?raw=true)|

<br>

### [동네지도]
- 동네지도 추천 서비스
- KAKAO MAP 사용

| 동네지도 |
|----------|
|![login](https://github.com/MRoKGA/image/blob/main/3-11.png?raw=true)|

<br>

### [프로필 화면]
- 프로필 이미지 / 닉네임 / 계정정보 표시
- 매너온도 그래프 시각화

| 프로필 화면 |
|----------|
|![login](https://github.com/MRoKGA/image/blob/main/5-1.png?raw=true)|

<br>

### [나의 판매내역]
- 판매중 / 예약중 / 거래완료 탭 구분
- 상품 수정 / 삭제 / 거래완료 처리 버튼 제공
- 채팅수·관심수·조회수 표시

| 나의 판매내역 |
|----------|
|![login](https://github.com/MRoKGA/image/blob/main/5-2.png?raw=true)|

<br>

### [나의 구매내역]
- 내가 구매한 상품 리스트 표시
- 리뷰 작성 버튼 제공
- 리뷰 작성 시 별점 + 후기 입력 가능

| 나의 구매내역 | 나의 구매내역 후기작성 | 나의 구매내역 후기완료 |
|----------|----------|----------|
|![login](https://github.com/MRoKGA/image/blob/main/5-3.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/5-3-1.png?raw=true)|![login](https://github.com/MRoKGA/image/blob/main/5-3-2.png?raw=true)|

<br>

### [나의 관심목록]
- 관심 등록한 상품 목록 표시
- 상품 상세 페이지로 이동 가능
- 시간 정보(예: 78일 전) 표시

| 나의 관심목록 |
|----------|
|![login](https://github.com/MRoKGA/image/blob/main/5-4.png?raw=true)|

<br>

### [나의 약속화면]
- 캘린더 기반 일정 확인
- 약속 상태별 필터(전체 / 제안됨 / 수락됨 / 거절됨 / 취소됨)
- 약속 상세 정보 제공

| 나의 약속화면 |
|----------|
|![login](https://github.com/MRoKGA/image/blob/main/5-5.png?raw=true)|

<br>

## 어려웠던점

### 🍊이동국

### 문제상황
- Chrome, Android에서는 위치 권한 요청이 잘 동작함
- 하지만 iPhone Safari에서는 현재위치로 찾기 버튼을 눌러도 아무 반응 없음
- 콘솔엔 "GPS 접근이 거부되었습니다.", "주소 정보를 찾을 수 없습니다."만 출력됨
- Kakao API에서 401 Unauthorized 오류 발생

### 원인
 - iOS Safari는 HTTPS 환경이 아닌 경우 navigator.geolocation 접근을 차단합니다.

 
### 해결방법
 - LocalTunnel 사용하기

 ### 🍊홍재호
- 
 ### 🍊이의준
- 

## 소감

### 🍊이동국
- ㅇㅇㅇ

### 🍊이의준
- ㅇㅇㅇ

### 🍊홍재호
- ㅇㅇㅇ
