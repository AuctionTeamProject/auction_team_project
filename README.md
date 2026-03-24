# 🔨 경매 플랫폼 프로젝트

> 한 줄 프로젝트 소개를 여기에 작성하세요.

<br>

## 📌 목차

- [프로젝트 소개](#-프로젝트-소개)
- [팀원 소개](#-팀원-소개)
- [기술 스택](#-기술-스택)
- [주요 기능](#-주요-기능)
- [아키텍처](#-아키텍처)
- [ERD](#-erd)
- [API 명세서](#-api-명세서)
- [프로젝트 구조](#-프로젝트-구조)
- [시작하기](#-시작하기)
- [환경변수 설정](#-환경변수-설정)
- [트러블슈팅](#-트러블슈팅)

<br>

## 📖 프로젝트 소개

> 실시간 경매 시스템을 기반으로 다양한 비즈니스 기능을 확장한 백엔드 프로젝트입니다.
> 
> 본 프로젝트는 실시간 경매 서비스를 중심으로, 사용자들이 경매에 참여하고 입찰하며, 다양한 부가 기능을 이용할 수 있도록 설계된 시스템입니다.

- 개발 기간 : 2026.03.05 ~ 2026.03.25
- 배포 URL :

<br>

## 👥 팀원 소개

| 이름  | 역할      | 담당 기능                   | GitHub |
|-----|---------|-------------------------|--------|
|     |         |                         |  |
| 정인호 | Backend | 이벤트, 쿠폰, 캐싱, Redis Lock | https://github.com/eNoLJ |
|     |         |                         |  |
|     |         |                         |  |
| 이름  | 역할      | 담당 기능  | GitHub                    |
|-----|---------|--------|---------------------------|
|     |         |        |                           |
|     |         |        |                           |
|     |         |        |                           |
|     |         |        |                           |
| 조현희 | Backend | 유저, 인증 | https://github.com/hhjo96 |

<br>

## 🛠 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-59666C?style=flat-square&logo=hibernate&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-0769AD?style=flat-square&logoColor=white)

### Database & Cache
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)

### Infra & DevOps
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazons3&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)

### 인증 & 실시간
![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-EB5424?style=flat-square&logo=auth0&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=flat-square&logoColor=white)
![STOMP](https://img.shields.io/badge/STOMP-010101?style=flat-square&logoColor=white)

<br>

## ✨ 주요 기능

### 🏷 경매 (Auction)
-

### 💰 입찰 (Bid)
-

### 💬 채팅 (Chat)
-

### 🔔 알림 (Alert)
-

### 🎫 쿠폰 (Coupon)
- 선착순 쿠폰 발급(Redis 분산 락 적용)
- 쿠폰 사용 및 리워드 적용

### 🎉 이벤트 (Event)
- 기간 기반 이벤트 생성 및 상태 관리
- Redis 캐싱을 통한 목록 조회 성능 개선

### 💎 멤버십 (Membership)
- Enum 기반 멤버십 상태 관리 설계

### 🔐 인증 / 소셜 로그인 (Auth)
- JWT 기반 인증 (Access Token + Refresh Token)
- Spring Security를 통한 인증/인가
- OAuth2 기반 소셜 로그인 (Google / Kakao / Naver)
- 토큰 블랙리스트 처리(redis)

<br>

## 🏗 아키텍처

> 아키텍처 다이어그램을 여기에 첨부하세요.

```
(아키텍처 이미지 또는 다이어그램)
```

<br>

## 📊 ERD


<img width="1969" height="1044" alt="Image" src="https://github.com/user-attachments/assets/bac0b569-f1a7-4ab6-b64d-62f6bc25f406" />


<br>

## 📋 API 명세서

> 🔒 = 인증 필요 (Header: `Authorization: Bearer {accessToken}`)
>
> 모든 응답은 아래 공통 포맷을 따릅니다.
> ```json
> { "success": true, "code": "200", "message": "성공 메시지", "data": { } }
> ```

---

### 🔐 인증 (Auth) `/api/auth`

| Method | URI | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/auth/signup` | 회원가입 | ✗ |
| `POST` | `/api/auth/login` | 로그인 | ✗ |
| `POST` | `/api/auth/logout` | 로그아웃 | 🔒 |
| `POST` | `/api/auth/refresh` | 토큰 재발급 | ✗ |
| `PATCH` | `/api/auth/oauth2/me/google` | Google 소셜 추가정보 입력 | 🔒 |
| `PATCH` | `/api/auth/oauth2/me/kakao` | Kakao 소셜 추가정보 입력 | 🔒 |

<details>
<summary>요청 / 응답 예시 보기</summary>

#### POST `/api/auth/signup` — 회원가입

**Request Body**
```json
{
  "nickname": "홍길동",
  "name": "홍길동",
  "email": "user@example.com",
  "password": "Password1!",
  "phone": "01012345678",
  "userRole": "ROLE_USER",
  "membershipGrade": "NORMAL"
}
```
> - `userRole`: `ROLE_USER` | `ROLE_ADMIN`
> - `membershipGrade`: `NORMAL` | `SELLER`

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "회원가입 성공",
  "data": {
    "nickname": "홍길동",
    "name": "홍길동",
    "email": "user@example.com"
  }
}
```

---

#### POST `/api/auth/login` — 로그인

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "Password1!"
}
```

**Response** `200 OK`
> Refresh Token은 쿠키로 전달됩니다.
```json
{
  "success": true,
  "code": "200",
  "message": "로그인 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

#### POST `/api/auth/logout` — 로그아웃

**Request Header**
```
Authorization: Bearer {accessToken}
```

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "로그아웃 성공",
  "data": null
}
```

---

#### POST `/api/auth/refresh` — 토큰 재발급

**Request Cookie**
```
refreshToken={refreshToken}
```

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "토큰 갱신 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

#### PATCH `/api/auth/oauth2/me/google` — Google 소셜 추가정보 입력

**Request Body**
```json
{
  "phone": "01012345678"
}
```

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "추가정보 입력 완료",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "nickname": "홍길동",
    "phone": "01012345678",
    "email": "user@gmail.com"
  }
}
```

---

#### PATCH `/api/auth/oauth2/me/kakao` — Kakao 소셜 추가정보 입력

**Request Body**
```json
{
  "phone": "01012345678",
  "email": "user@example.com"
}
```

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "추가정보 입력 완료",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "nickname": "홍길동",
    "phone": "01012345678",
    "email": "user@example.com"
  }
}
```

</details>

---

### 👤 사용자 (User) `/api/users`

| Method | URI | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/users/me` | 내 프로필 조회 | 🔒 |
| `PATCH` | `/api/users/me` | 닉네임 변경 | 🔒 |
| `PATCH` | `/api/users/me/password` | 비밀번호 변경 | 🔒 |
| `GET` | `/api/users/me/auctions` | 내가 등록한 경매 목록 | 🔒 |
| `GET` | `/api/users/me/bids` | 내 입찰 내역 | 🔒 |
| `POST` | `/api/users/{userId}/ratings` | 유저 평점 등록 | 🔒 |
| `GET` | `/api/users/me/ratings` | 내 평점 조회 | 🔒 |

<details>
<summary>요청 / 응답 예시 보기</summary>

#### GET `/api/users/me` — 내 프로필 조회

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "마이페이지 조회 성공",
  "data": {
    "nickname": "홍길동",
    "name": "홍길동",
    "email": "user@example.com",
    "phone": "01012345678",
    "point": 10000,
    "membership": {
      "grade": "NORMAL",
      "expiredAt": "2026-12-31T23:59:59"
    },
    "userRole": "ROLE_USER"
  }
}
```
> - `membership.grade`: `NORMAL` | `SELLER`

---

#### PATCH `/api/users/me` — 닉네임 변경

**Request Body**
```json
{
  "newNickname": "새닉네임"
}
```

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "닉네임 변경 성공",
  "data": null
}
```

---

#### PATCH `/api/users/me/password` — 비밀번호 변경

**Request Body**
```json
{
  "oldPassword": "OldPassword1!",
  "newPassword": "NewPassword1!"
}
```

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "비밀번호 변경 성공",
  "data": null
}
```

---

#### GET `/api/users/me/auctions` — 내가 등록한 경매 목록

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "내 경매 상품 내역 조회 성공",
  "data": [
    {
      "auctionId": 1,
      "productName": "아이폰 15 Pro",
      "imageURL": "https://s3.amazonaws.com/...",
      "startPrice": 500000,
      "status": "ACTIVE",
      "createdAt": "2026-03-01T10:00:00"
    }
  ]
}
```
> - `status`: `PENDING` | `CANCEL` | `READY` | `ACTIVE` | `DONE` | `NO_BID`

---

#### GET `/api/users/me/bids` — 내 입찰 내역

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "내 입찰 내역 조회 성공",
  "data": [
    {
      "bidId": 10,
      "auctionId": 1,
      "price": 550000,
      "status": "SUCCEEDED",
      "createdAt": "2026-03-15T14:30:00"
    }
  ]
}
```
> - `status`: `SUCCEEDED` | `FAILED`

---

#### POST `/api/users/{userId}/ratings` — 유저 평점 등록

**Request Body**
```json
{
  "score": 5
}
```
> - `score`: 1 ~ 5 (정수)

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "셀러 평점 등록 성공",
  "data": {
    "reviewerId": 2,
    "sellerId": 1,
    "score": 5
  }
}
```

---

#### GET `/api/users/me/ratings` — 내 평점 조회

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "내 점수 확인 성공",
  "data": {
    "userId": 1,
    "ratings": 4.5
  }
}
```

</details>

---

### 🏷 경매 (Auction) `/api/auctions`

| Method | URI | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/auctions` | 경매 등록 | 🔒 |
| `PATCH` | `/api/auctions/{auctionId}` | 경매 수정 | 🔒 |
| `DELETE` | `/api/auctions/{auctionId}` | 경매 삭제 | 🔒 |
| `GET` | `/api/auctions/{auctionId}` | 경매 단건 조회 | ✗ |
| `GET` | `/api/auctions/v1` | 경매 목록 조회 (v1) | ✗ |
| `GET` | `/api/auctions/v2` | 경매 목록 조회 (v2, 캐싱) | ✗ |
| `GET` | `/api/auctions/top5` | 인기 경매 Top 5 | ✗ |
| `POST` | `/api/auctions/files/upload` | 경매 이미지 업로드 (S3) | 🔒 |
| `GET` | `/api/auctions/files/download-url` | 이미지 다운로드 URL 조회 | 🔒 |
| `PATCH` | `/admin/auctions/{auctionId}/approve` | 경매 승인 (관리자) | 🔒 |

<details>
<summary>요청 / 응답 예시 보기</summary>

#### POST `/api/auctions` — 경매 등록

**Request Body**
```json
{
  "productName": "아이폰 15 Pro",
  "imageUrl": "https://s3.amazonaws.com/bucket/image.jpg",
  "category": "ELECTRONICS",
  "startPrice": 500000,
  "minimumBid": 10000,
  "startAt": "2026-04-01T10:00:00",
  "endAt": "2026-04-03T10:00:00"
}
```
> - `category`: `CLOTHES` | `ELECTRONICS` | `FOOD`

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "경매가 등록되었습니다.",
  "data": {
    "auctionId": 1
  }
}
```

---

#### PATCH `/api/auctions/{auctionId}` — 경매 수정

**Request Body**
```json
{
  "productName": "아이폰 15 Pro (수정)",
  "imageUrl": "https://s3.amazonaws.com/bucket/image2.jpg",
  "category": "ELECTRONICS",
  "startPrice": 450000,
  "minimumBid": 5000,
  "startAt": "2026-04-02T10:00:00",
  "endAt": "2026-04-04T10:00:00"
}
```

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "경매가 수정되었습니다.",
  "data": {
    "auctionId": 1
  }
}
```

---

#### DELETE `/api/auctions/{auctionId}` — 경매 삭제

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "경매가 삭제되었습니다.",
  "data": {
    "auctionId": 1
  }
}
```

---

#### GET `/api/auctions/{auctionId}` — 경매 단건 조회

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "경매 조회에 성공했습니다.",
  "data": {
    "auctionId": 1,
    "sellerNickname": "홍길동",
    "productName": "아이폰 15 Pro",
    "imageUrl": "https://s3.amazonaws.com/bucket/image.jpg",
    "category": "ELECTRONICS",
    "status": "ACTIVE",
    "startPrice": 500000,
    "minimumBid": 10000,
    "viewCount": 128,
    "startAt": "2026-04-01T10:00:00",
    "endAt": "2026-04-03T10:00:00"
  }
}
```
> - `status`: `PENDING` | `CANCEL` | `READY` | `ACTIVE` | `DONE` | `NO_BID`

---

#### GET `/api/auctions/v1` — 경매 목록 조회 (v1)

**Query Parameters**
| Parameter | Type | 필수 | 설명 |
|-----------|------|:----:|------|
| `page` | int | ✗ | 페이지 번호 (기본값: 0) |
| `size` | int | ✗ | 페이지 크기 (기본값: 10) |
| `category` | String | ✗ | 카테고리 필터 (`CLOTHES`, `ELECTRONICS`, `FOOD`) |

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "경매 목록 조회에 성공했습니다.",
  "data": {
    "content": [
      {
        "auctionId": 1,
        "sellerNickname": "홍길동",
        "productName": "아이폰 15 Pro",
        "imageUrl": "https://s3.amazonaws.com/bucket/image.jpg",
        "category": "ELECTRONICS",
        "startPrice": 500000,
        "status": "ACTIVE",
        "startAt": "2026-04-01T10:00:00",
        "endAt": "2026-04-03T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "currentPage": 0,
    "size": 10
  }
}
```

---

#### GET `/api/auctions/top5` — 인기 경매 Top 5

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "인기 경매 조회에 성공했습니다.",
  "data": [
    {
      "auctionId": 3,
      "sellerNickname": "김판매",
      "productName": "맥북 프로 M3",
      "imageUrl": "https://s3.amazonaws.com/bucket/image.jpg",
      "category": "ELECTRONICS",
      "startPrice": 1000000,
      "status": "ACTIVE",
      "startAt": "2026-04-01T10:00:00",
      "endAt": "2026-04-05T10:00:00"
    }
  ]
}
```

---

#### PATCH `/admin/auctions/{auctionId}/approve` — 경매 승인 (관리자)

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "경매가 승인되었습니다.",
  "data": {
    "auctionId": 1,
    "status": "READY"
  }
}
```

</details>

---

### 💰 입찰 (Bid) `/api/bids`

| Method | URI | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/bids/{auctionId}` | 입찰 (기본) | 🔒 |
| `POST` | `/api/bids/{auctionId}/lock/pessimistic` | 입찰 (비관적 락) | 🔒 |
| `POST` | `/api/bids/{auctionId}/lock/optimistic` | 입찰 (낙관적 락) | 🔒 |
| `POST` | `/api/bids/{auctionId}/lock/redisson` | 입찰 (Redisson 분산락) | 🔒 |
| `POST` | `/api/bids/{auctionId}/auto` | 자동 입찰 등록 | 🔒 |
| `GET` | `/api/bids/me` | 내 입찰 목록 조회 | 🔒 |
| `GET` | `/api/bids/{auctionId}` | 특정 경매 입찰 목록 조회 | 🔒 |

<details>
<summary>요청 / 응답 예시 보기</summary>

#### POST `/api/bids/{auctionId}` — 입찰

**Request Body**
```json
{
  "price": 550000
}
```

**Response** `200 OK` (일반 입찰)
```json
{
  "success": true,
  "code": "200",
  "message": "입찰이 완료되었습니다.",
  "data": {
    "bidId": 10,
    "auctionId": 1,
    "nickname": "홍길동",
    "price": 550000,
    "message": null,
    "createdAt": "2026-03-23"
  }
}
```

**Response** `200 OK` (종료 5분 전 — Blind 입찰, 금액 비공개)
```json
{
  "success": true,
  "code": "200",
  "message": "입찰 시도가 완료되었습니다.",
  "data": {
    "bidId": 11,
    "auctionId": 1,
    "nickname": "홍길동",
    "price": null,
    "message": "입찰 시도가 완료되었습니다.",
    "createdAt": "2026-03-23"
  }
}
```

---

#### GET `/api/bids/me` — 내 입찰 목록 조회

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "입찰 목록 조회에 성공했습니다.",
  "data": [
    {
      "bidId": 10,
      "auctionId": 1,
      "price": 550000,
      "status": "SUCCEEDED",
      "createdAt": "2026-03-15T14:30:00"
    }
  ]
}
```
> - `status`: `SUCCEEDED` | `FAILED`

---

#### GET `/api/bids/{auctionId}` — 특정 경매 입찰 목록 조회

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "입찰 목록 조회에 성공했습니다.",
  "data": [
    {
      "bidId": 10,
      "auctionId": 1,
      "price": 550000,
      "status": "SUCCEEDED",
      "createdAt": "2026-03-15T14:30:00"
    },
    {
      "bidId": 9,
      "auctionId": 1,
      "price": 520000,
      "status": "FAILED",
      "createdAt": "2026-03-15T14:00:00"
    }
  ]
}
```

</details>

---

### 💬 채팅 (Chat & ChatRoom)

| Method | URI | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/chat/rooms` | 채팅방 생성 | 🔒 |
| `GET` | `/api/chat/rooms` | 채팅방 목록 조회 | 🔒 |
| `DELETE` | `/api/chat/rooms/{roomId}` | 채팅방 삭제 | 🔒 |
| `GET` | `/api/messages/before/{roomId}` | 특정 시점 이전 메시지 조회 | 🔒 |
| `GET` | `/api/rooms/{roomId}/messages` | 채팅방 메시지 전체 조회 | 🔒 |
| `SEND` | `/pub/chat/{roomId}` | 메시지 전송 (STOMP) | 🔒 |
| `SUBSCRIBE` | `/sub/chat/{roomId}` | 채팅방 구독 (STOMP) | 🔒 |

<details>
<summary>요청 / 응답 예시 보기</summary>

#### POST `/api/chat/rooms` — 채팅방 생성

**Request Body**
```json
{
  "name": "아이폰 15 Pro 문의방"
}
```

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "채팅방이 생성되었습니다.",
  "data": {
    "id": 1,
    "name": "아이폰 15 Pro 문의방",
    "createdAt": "2026-03-23T10:00:00"
  }
}
```

---

#### GET `/api/chat/rooms` — 채팅방 목록 조회

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "채팅방 목록 조회에 성공했습니다.",
  "data": [
    {
      "id": 1,
      "name": "아이폰 15 Pro 문의방",
      "createdAt": "2026-03-23T10:00:00"
    }
  ]
}
```

---

#### DELETE `/api/chat/rooms/{roomId}` — 채팅방 삭제

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "채팅방이 삭제되었습니다.",
  "data": null
}
```

---

#### GET `/api/rooms/{roomId}/messages` — 채팅방 메시지 전체 조회

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "메시지 조회에 성공했습니다.",
  "data": [
    {
      "id": 1,
      "message": "안녕하세요, 직거래 가능한가요?",
      "roomId": 1,
      "userId": 2,
      "userName": "홍길동",
      "createdAt": "2026-03-23T10:05:00"
    }
  ]
}
```

---

#### SEND `/pub/chat/{roomId}` — 메시지 전송 (STOMP)

**STOMP Message Body**
```json
{
  "roomId": 1,
  "message": "안녕하세요, 직거래 가능한가요?"
}
```

**STOMP Broadcast** (`/sub/chat/{roomId}`)
```json
{
  "id": 1,
  "message": "안녕하세요, 직거래 가능한가요?",
  "roomId": 1,
  "userId": 2,
  "userName": "홍길동",
  "createdAt": "2026-03-23T10:05:00"
}
```

</details>

---

### 🔔 알림 (Alert) `/api/alerts`

| Method | URI | 설명 | 인증 |
|--------|-----|------|:----:|
| `GET` | `/api/alerts` | 내 알림 목록 조회 | 🔒 |
| `PATCH` | `/api/alerts/{alertId}/read` | 알림 읽음 처리 | 🔒 |

<details>
<summary>요청 / 응답 예시 보기</summary>

#### GET `/api/alerts` — 내 알림 목록 조회

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "알림 목록 조회에 성공했습니다.",
  "data": [
    {
      "alertId": 1,
      "auctionId": 1,
      "userId": 2,
      "alertType": "NEW_BID",
      "message": "새로운 입찰",
      "isRead": false,
      "createdAt": "2026-03-23T10:10:00"
    }
  ]
}
```
> - `alertType`: `NEW_BID` | `OUT_BID` | `AUCTION_END_SOON` | `AUCTION_END` | `AUCTION_WIN` | `SUPPORT_REQUEST`

---

#### PATCH `/api/alerts/{alertId}/read` — 알림 읽음 처리

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "알림이 읽음 처리 되었습니다.",
  "data": null
}
```

</details>

---

### 🎉 이벤트 (Event) `/api/events`

| Method | URI | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/events` | 이벤트 생성 | 🔒 |
| `GET` | `/api/events` | 이벤트 목록 조회 | ✗ |
| `GET` | `/api/events/{eventId}` | 이벤트 단건 조회 | ✗ |
| `PATCH` | `/api/events/{eventId}` | 이벤트 수정 | 🔒 |
| `DELETE` | `/api/events/{eventId}` | 이벤트 삭제 | 🔒 |

<details>
<summary>요청 / 응답 예시 보기</summary>

#### POST `/api/events` — 이벤트 생성

**Request Body**
```json
{
  "eventName": "신규 가입 환영 이벤트",
  "eventDescription": "신규 가입 시 포인트 쿠폰 지급",
  "totalQuantity": 100,
  "rewardType": "POINT",
  "startAt": "2026-04-01T00:00:00",
  "endAt": "2026-04-30T23:59:59"
}
```
> - `rewardType`: `POINT` | `MEMBERSHIP`

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "이벤트가 생성되었습니다.",
  "data": {
    "id": 1,
    "eventName": "신규 가입 환영 이벤트",
    "eventDescription": "신규 가입 시 포인트 쿠폰 지급",
    "totalQuantity": 100,
    "issuedQuantity": 0,
    "rewardType": "POINT",
    "startAt": "2026-04-01T00:00:00",
    "endAt": "2026-04-30T23:59:59",
    "status": "OPEN",
    "adminId": 1
  }
}
```

---

#### GET `/api/events` — 이벤트 목록 조회

**Query Parameters**
| Parameter | Type | 필수 | 설명 |
|-----------|------|:----:|------|
| `page` | int | ✗ | 페이지 번호 (기본값: 0) |
| `size` | int | ✗ | 페이지 크기 (기본값: 10) |

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "이벤트 목록 조회에 성공했습니다.",
  "data": {
    "events": [
      {
        "eventId": 1,
        "eventName": "신규 가입 환영 이벤트",
        "eventDescription": "신규 가입 시 포인트 쿠폰 지급",
        "totalQuantity": 100,
        "issuedQuantity": 20,
        "remainingQuantity": 80,
        "rewardType": "POINT",
        "startAt": "2026-04-01T00:00:00",
        "endAt": "2026-04-30T23:59:59",
        "status": "OPEN"
      }
    ],
    "currentPage": 0,
    "size": 10,
    "totalPages": 1,
    "totalElements": 1,
    "last": true
  }
}
```

---

#### GET `/api/events/{eventId}` — 이벤트 단건 조회

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "이벤트 조회에 성공했습니다.",
  "data": {
    "id": 1,
    "eventName": "신규 가입 환영 이벤트",
    "eventDescription": "신규 가입 시 포인트 쿠폰 지급",
    "totalQuantity": 100,
    "issuedQuantity": 20,
    "rewardType": "POINT",
    "startAt": "2026-04-01T00:00:00",
    "endAt": "2026-04-30T23:59:59",
    "status": "OPEN",
    "adminId": 1
  }
}
```

---

#### PATCH `/api/events/{eventId}` — 이벤트 수정

**Request Body**
```json
{
  "eventName": "신규 가입 환영 이벤트 (수정)",
  "eventDescription": "포인트 및 멤버십 쿠폰 지급",
  "totalQuantity": 200,
  "rewardType": "MEMBERSHIP",
  "startAt": "2026-04-01T00:00:00",
  "endAt": "2026-05-31T23:59:59",
  "status": "OPEN"
}
```
> - `status`: `OPEN` | `CLOSED`

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "이벤트가 수정되었습니다.",
  "data": {
    "id": 1,
    "eventName": "신규 가입 환영 이벤트 (수정)",
    "eventDescription": "포인트 및 멤버십 쿠폰 지급",
    "totalQuantity": 200,
    "issuedQuantity": 20,
    "rewardType": "MEMBERSHIP",
    "startAt": "2026-04-01T00:00:00",
    "endAt": "2026-05-31T23:59:59",
    "status": "OPEN",
    "adminId": 1
  }
}
```

---

#### DELETE `/api/events/{eventId}` — 이벤트 삭제

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "이벤트가 삭제되었습니다.",
  "data": null
}
```

</details>

---

### 🎫 쿠폰 (Coupon)

| Method | URI | 설명 | 인증 |
|--------|-----|------|:----:|
| `POST` | `/api/events/{eventId}/coupons` | 쿠폰 발급 | 🔒 |
| `POST` | `/api/coupons/{couponId}/use` | 쿠폰 사용 | 🔒 |

<details>
<summary>요청 / 응답 예시 보기</summary>

#### POST `/api/events/{eventId}/coupons` — 쿠폰 발급

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "쿠폰이 발급되었습니다.",
  "data": {
    "couponId": 5,
    "eventId": 1,
    "userId": 2,
    "status": "UNUSED",
    "issuedAt": "2026-04-10T15:00:00"
  }
}
```
> - `status`: `UNUSED` | `USED`

---

#### POST `/api/coupons/{couponId}/use` — 쿠폰 사용

**Response** `200 OK`
```json
{
  "success": true,
  "code": "200",
  "message": "쿠폰이 사용되었습니다.",
  "data": {
    "couponId": 5,
    "status": "USED",
    "rewardType": "POINT",
    "usedAt": "2026-04-15T10:30:00",
    "userId": 2,
    "eventId": 1
  }
}
```
> - `rewardType`: `POINT` | `MEMBERSHIP`

</details>

---

### ⚠️ 공통 에러 응답

| HTTP Status | 설명 |
|-------------|------|
| `400 Bad Request` | 잘못된 요청 (유효성 검사 실패 등) |
| `401 Unauthorized` | 인증 실패 (토큰 없음 / 만료) |
| `403 Forbidden` | 권한 없음 |
| `404 Not Found` | 리소스 없음 |
| `409 Conflict` | 중복 데이터 |
| `500 Internal Server Error` | 서버 오류 |

```json
{
  "success": false,
  "code": "400",
  "message": "이메일 형식이 올바르지 않습니다.",
  "data": null
}
```

<br>

## 🗂 프로젝트 구조

```
src/main/java/sparta/auction_team_project/
├── common/
│   ├── config/          # JPA, QueryDSL, Redis, WebSocket, Security 설정
│   ├── constants/       # 공통 상수
│   ├── dto/             # 공통 DTO (이벤트 객체 등)
│   ├── entity/          # BaseEntity
│   ├── eventlistener/   # 경매 종료, 입찰, 채팅 이벤트 리스너
│   ├── exception/       # 전역 예외 처리
│   ├── interceptor/     # STOMP 인증 인터셉터
│   ├── jwt/             # JWT 유틸, 리프레시 토큰, 블랙리스트
│   ├── oauth2/          # Google / Kakao / Naver OAuth2 처리
│   ├── redis/           # Redis Pub/Sub, 분산락, 캐시
│   ├── response/        # 공통 응답 형식
│   ├── s3/              # S3 파일 업로드/다운로드
│   ├── security/        # Spring Security 설정, 필터, 소셜 로그인
│   └── util/            # 유틸리티
└── domain/
    ├── alert/           # 알림
    ├── auction/         # 경매
    ├── auth/            # 인증
    ├── bid/             # 입찰
    ├── bidlog/          # 입찰 로그
    ├── chat/            # 채팅 메시지
    ├── chatroom/        # 채팅방
    ├── coupon/          # 쿠폰
    ├── event/           # 이벤트
    ├── memberShip/      # 멤버십
    └── user/            # 사용자
```

<br>

## 🚀 시작하기

### 사전 요구사항

- Java 17 이상
- Docker & Docker Compose
- MySQL
- Redis

### 실행 방법

```bash
# 1. 레포지토리 클론
git clone https://github.com/your-repo/auction-team-project.git
cd auction-team-project

# 2. 환경변수 설정
cp .env.example .env
# .env 파일에 필요한 값 입력

# 3. Docker로 의존 서비스 실행
docker-compose up -d

# 4. 애플리케이션 빌드 & 실행
./gradlew bootRun
```

<br>

## ⚙ 환경변수 설정

```env
# Database
DB_URL=
DB_USERNAME=
DB_PASSWORD=

# Redis
REDIS_HOST=
REDIS_PORT=

# JWT
JWT_SECRET=
JWT_ACCESS_EXPIRATION=
JWT_REFRESH_EXPIRATION=

# OAuth2 - Google
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

# OAuth2 - Kakao
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=

# OAuth2 - Naver
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=

# AWS S3
AWS_ACCESS_KEY=
AWS_SECRET_KEY=
AWS_S3_BUCKET=
AWS_REGION=
```

<br>

## 🔧 트러블슈팅

> 개발하면서 마주친 주요 문제와 해결 과정을 기록하세요.

| 문제 | 원인 | 해결 방법 |
|------|------|----------|
| 선착순 쿠폰 발급 시 초과 발급 발생 | 선착순 쿠폰 발급 시 초과 발급 발생 | Redis 분산 락을 적용하여 eventId 기준으로 임계 구역을 설정하고 동시성 제어 |
| 이벤트 목록 조회 성능 저하 | 동일 데이터 반복 조회로 DB 부하 증가 | Redis Cache-Aside 전략 적용 및 TTL 설정으로 조회 성능 개선 |
|  |  |  | |
| 소셜 로그인 후 필수값 누락 | 소셜 로그인 제공자별로 정보제공 범위가 다름 | 제공자별 추가 정보 입력 api 분리 |