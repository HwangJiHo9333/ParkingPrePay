# ParkingPrePay
한국어 버전
# 🅿️ 주차장 요금 사전정산 시스템 (Parking Pre-Pay System)

이 프로젝트는 **웹 기반 Kiosk형 주차장 요금 사전 정산 시스템**입니다.  
차량 번호 입력 → 입차 기록 조회 → 이용시간 계산 → 요금 산정 → 결제 → 영수증 발행의 흐름으로 동작합니다.

---

## 🚀 주요 기능

| 기능 | 설명 |
|----|----|
| 번호 입력 UI (STEP1) | 차량 번호 키패드 입력 화면 |
| 후보 차량 선택 (STEP2) | 입력 번호와 일치하는 차량 목록 표시 |
| 이용시간 & 요금 계산 (STEP3) | 입출차 시간 기반 자동 계산 |
| 결제 수단 선택 (STEP4) | 현금 / 카드 / 전액 무료 적용 |
| 결제 완료 + 영수증 표시 (STEP5) | 영수증 출력 및 출차 안내 |
| 다국어 지원 | 🇯🇵 일본어 / 🇺🇸 영어 전환 가능 |
| 데이터 저장 | Oracle DB 연동, 이용 기록 보관 |

---

## 🛠 기술 스택

| 영역 | 기술 |
|-----|-----|
| Backend | Spring Boot / Spring MVC / MyBatis |
| Frontend | Thymeleaf / HTML / CSS / JavaScript |
| Database | Oracle Database |
| Build Tool | Maven |

---

## 🔗 실행 방법

```bash
# 1. 프로젝트 클론
git clone https://github.com/your-id/your-repo.git
cd your-repo

# 2. DB 연결 정보 수정
src/main/resources/application.properties 수정

# 3. 서버 실행
mvn spring-boot:run

일본어 버전
# 🅿️ 事前精算システム (Parking Pre-Pay System)

本プロジェクトは、**駐車場の事前精算キオスク型システム**です。  
車両番号入力 → 入庫記録照会 → 利用時間計算 → 料金算出 → 支払い → 領収書発行という流れで動作します。

---

## 🚀 主な機能

| 機能 | 説明 |
|------|------|
| 番号入力 UI (STEP1) | 車両番号入力キーパッド画面 |
| 候補車両選択 (STEP2) | 入力番号と一致する車両一覧表示 |
| 利用時間 & 料金計算 (STEP3) | 入出庫時刻に基づく自動計算 |
| 支払い方法選択 (STEP4) | 現金 / カード / 全額無料 |
| 精算完了 & 領収書表示 (STEP5) | 領収書の表示と出庫案内 |
| 多言語対応 | 🇯🇵 日本語 / 🇺🇸 英語切替可能 |
| データ保存 | Oracle DB に利用記録を保存 |

---

## 🛠 技術構成

| レイヤー | 技術 |
|--------|------|
| Backend | Spring Boot / Spring MVC / MyBatis |
| Frontend | Thymeleaf / HTML / CSS / JavaScript |
| Database | Oracle Database |
| Build | Maven |

---

## 🔗 実行方法

```bash
git clone https://github.com/your-id/your-repo.git
cd your-repo

# DB 接続設定
src/main/resources/application.properties を編集

# 起動
mvn spring-boot:run
